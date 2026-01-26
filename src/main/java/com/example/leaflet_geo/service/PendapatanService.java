package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.DashboardSummaryDTO;
import com.example.leaflet_geo.dto.RekeningDetailDTO;
import com.example.leaflet_geo.dto.TargetRealisasiDTO;
import com.example.leaflet_geo.dto.TopKontributorDTO;
import com.example.leaflet_geo.dto.TrendBulananDTO;
import com.example.leaflet_geo.dto.PajakDataDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class PendapatanService {

    private final JdbcTemplate mysqlJdbcTemplate;
    private final BphtbService bphtbService;
    private final SismiopService sismiopService;
    private final EpasirService epasirService;

    public PendapatanService(
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate,
            BphtbService bphtbService,
            SismiopService sismiopService,
            EpasirService epasirService) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.bphtbService = bphtbService;
        this.sismiopService = sismiopService;
        this.epasirService = epasirService;
    }

    /**
     * Target hardcode untuk setiap jenis pajak
     */
    private static final Map<Integer, Long> TARGET_HARDCODE = Map.of(
            1, 2_000_000_000L, // Pajak Hotel
            2, 5_150_000_000L, // Pajak Restoran
            3, 1_000_000_000L, // Pajak Hiburan
            5, 45_000_000_000L, // Pajak Penerangan Jalan
            7, 550_000_000L, // Pajak Parkir
            4, 2_350_000_000L, // Pajak Reklame
            6, 29_000_000_000L, // Pajak Mineral Bukan Logam dan Batuan
            8, 1_200_000_000L, // Pajak Air Tanah
            10, 15_000_000_000L // BPHTB (Bea Perolehan Hak atas Tanah dan Bangunan)
    // Pajak Sarang Burung Walet (ID 9) tidak ada target
    );

    private long getTotalTargetHardcode() {
        return TARGET_HARDCODE.values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    /**
     * Helper to execute tasks with timeout (prevents hanging if DB is unreachable)
     */
    private <T> T executeSafely(Supplier<T> supplier, T defaultValue, String taskName) {
        try {
            return CompletableFuture.supplyAsync(supplier)
                    .get(3, TimeUnit.SECONDS); // 3 seconds timeout per call
        } catch (Exception e) {
            System.err.println("⚠️ Timeout/Error in " + taskName + ": " + e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Get Dashboard Summary
     */
    public DashboardSummaryDTO getDashboardSummary(Integer tahun) {
        String sql = """
                SELECT
                    (SELECT COALESCE(SUM(t_jmlhpembayaran), 0) FROM t_transaksi
                     WHERE YEAR(t_tglpembayaran) = ?) AS total_realisasi,

                    (SELECT COUNT(*) FROM t_wp) AS total_wp,

                    (SELECT COUNT(*) FROM t_wpobjek) AS total_objek,

                    (SELECT COUNT(*) FROM t_transaksi
                     WHERE YEAR(t_tglpembayaran) = ?) AS total_transaksi,

                    (SELECT COUNT(DISTINCT t_jenispajak) FROM t_transaksi
                     WHERE YEAR(t_tglpembayaran) = ?) AS jenis_pajak_aktif
                """;

        // Wrap MySQL call
        Map<String, Object> result = executeSafely(() -> mysqlJdbcTemplate.queryForMap(sql, tahun, tahun, tahun),
                Map.of(
                        "total_realisasi", 0,
                        "total_wp", 0,
                        "total_objek", 0,
                        "total_transaksi", 0,
                        "jenis_pajak_aktif", 0),
                "MySQL Dashboard Summary");

        // Gunakan target hardcode
        BigDecimal totalTarget = new BigDecimal(getTotalTargetHardcode());
        BigDecimal totalRealisasi = new BigDecimal(result.get("total_realisasi").toString());

        // Tambahkan realisasi dan target BPHTB dari database
        Long realisasiBphtb = executeSafely(() -> bphtbService.getRealisasiTahunan(tahun), 0L, "BPHTB Realisasi");
        Long targetBphtb = executeSafely(() -> bphtbService.getTargetTahunan(tahun), 0L, "BPHTB Target");

        totalRealisasi = totalRealisasi.add(new BigDecimal(realisasiBphtb));
        totalTarget = totalTarget.add(new BigDecimal(targetBphtb));

        // Tambahkan realisasi dan target PBB P2 dari SISMIOP
        Long realisasiPbb = executeSafely(() -> sismiopService.getRealisasiPbbTahunan(tahun.toString()), 0L,
                "PBB Realisasi");
        Long targetPbb = executeSafely(() -> sismiopService.getTargetPbbTahunan(tahun.toString()), 0L, "PBB Target");

        totalRealisasi = totalRealisasi.add(new BigDecimal(realisasiPbb));
        totalTarget = totalTarget.add(new BigDecimal(targetPbb));

        // Override realisasi Pajak Mineral dengan data dari E-PASIR
        Long realisasiMineralSimpatda = executeSafely(() -> mysqlJdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(t_jmlhpembayaran), 0) FROM t_transaksi WHERE t_jenispajak = 6 AND YEAR(t_tglpembayaran) = ?",
                Long.class, tahun), 0L, "Mineral SIMPATDA");

        Long realisasiMineralEpasir = executeSafely(() -> epasirService.getRealisasiMineralTahunan(tahun), 0L,
                "Mineral E-PASIR");

        if (realisasiMineralEpasir > 0) {
            // Ganti realisasi mineral SIMPATDA dengan E-PASIR
            totalRealisasi = totalRealisasi.subtract(new BigDecimal(realisasiMineralSimpatda));
            totalRealisasi = totalRealisasi.add(new BigDecimal(realisasiMineralEpasir));
            System.out.println("✅ Dashboard: Mineral SIMPATDA replaced with E-PASIR");
        }

        BigDecimal selisih = totalTarget.subtract(totalRealisasi);

        Double persentase = 0.0;
        if (totalTarget.compareTo(BigDecimal.ZERO) > 0) {
            persentase = totalRealisasi.divide(totalTarget, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalTarget(totalTarget);
        summary.setTotalRealisasi(totalRealisasi);
        summary.setTotalWp(((Number) result.get("total_wp")).longValue());
        summary.setTotalObjek(((Number) result.get("total_objek")).longValue());
        summary.setTotalTransaksi(((Number) result.get("total_transaksi")).longValue());
        summary.setJenisPajakAktif(((Number) result.get("jenis_pajak_aktif")).longValue() + 2); // +2 untuk BPHTB dan
                                                                                                // PBB
        summary.setPersentasePencapaian(persentase);
        summary.setSelisih(selisih);

        return summary;
    }

    /**
     * Get Target vs Realisasi per Jenis Pajak (Target Hardcode)
     */
    public List<TargetRealisasiDTO> getTargetRealisasiPerJenis(Integer tahun) {
        String sql = """
                SELECT
                    j.s_idjenis AS id_jenis,
                    j.s_namajenis AS jenis_pajak,
                    j.s_order AS urutan,
                    COALESCE(r.total_realisasi, 0) AS realisasi
                FROM s_jenisobjek j
                LEFT JOIN (
                    SELECT
                        t_jenispajak,
                        SUM(t_jmlhpembayaran) AS total_realisasi
                    FROM t_transaksi
                    WHERE YEAR(t_tglpembayaran) = ?
                    GROUP BY t_jenispajak
                ) r ON r.t_jenispajak = j.s_idjenis
                ORDER BY j.s_order
                """;

        List<TargetRealisasiDTO> results = executeSafely(() -> mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
            TargetRealisasiDTO dto = new TargetRealisasiDTO();
            Integer idJenis = rs.getInt("id_jenis");
            dto.setJenisPajak(rs.getString("jenis_pajak"));
            dto.setUrutan(rs.getInt("urutan"));

            // Gunakan target hardcode
            Long targetHardcode = TARGET_HARDCODE.getOrDefault(idJenis, 0L);
            BigDecimal target = new BigDecimal(targetHardcode);
            BigDecimal realisasi = rs.getBigDecimal("realisasi");
            BigDecimal selisih = target.subtract(realisasi);

            Double persentase = 0.0;
            if (target.compareTo(BigDecimal.ZERO) > 0) {
                persentase = realisasi.divide(target, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            }

            dto.setTarget(target);
            dto.setRealisasi(realisasi);
            dto.setSelisih(selisih);
            dto.setPersentasePencapaian(persentase);

            // Get breakdown detail per rekening
            // Note: This is also external call, wrapping it might be too granular, but
            // usually fast if connection open
            // We'll trust existing connection if 'results' query succeeded
            dto.setDetails(getRekeningDetailByJenis(idJenis, tahun));

            return dto;
        }, tahun), new ArrayList<>(), "MySQL Target Realisasi");

        // Tambahkan data BPHTB dari database PostgreSQL
        Long realisasiBphtb = executeSafely(() -> bphtbService.getRealisasiTahunan(tahun), 0L, "BPHTB Realisasi List");
        Long targetBphtb = executeSafely(() -> bphtbService.getTargetTahunan(tahun), 0L, "BPHTB Target List");

        if (true) { // Always add BPHTB row even if 0
            TargetRealisasiDTO bphtbDto = new TargetRealisasiDTO();
            bphtbDto.setJenisPajak("BPHTB");
            bphtbDto.setUrutan(10);

            BigDecimal target = new BigDecimal(targetBphtb);
            BigDecimal realisasi = new BigDecimal(realisasiBphtb);
            BigDecimal selisih = target.subtract(realisasi);

            Double persentase = 0.0;
            if (target.compareTo(BigDecimal.ZERO) > 0) {
                persentase = realisasi.divide(target, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            }

            bphtbDto.setTarget(target);
            bphtbDto.setRealisasi(realisasi);
            bphtbDto.setSelisih(selisih);
            bphtbDto.setPersentasePencapaian(persentase);
            bphtbDto.setDetails(List.of());

            results.add(bphtbDto);
        }

        // Tambahkan data PBB P2 dari database SISMIOP Oracle
        Long realisasiPbb = executeSafely(() -> sismiopService.getRealisasiPbbTahunan(tahun.toString()), 0L,
                "PBB Realisasi List");
        Long targetPbb = executeSafely(() -> sismiopService.getTargetPbbTahunan(tahun.toString()), 0L,
                "PBB Target List");

        if (true) {
            TargetRealisasiDTO pbbDto = new TargetRealisasiDTO();
            pbbDto.setJenisPajak("PBB P2");
            pbbDto.setUrutan(11);

            BigDecimal target = new BigDecimal(targetPbb);
            BigDecimal realisasi = new BigDecimal(realisasiPbb);
            BigDecimal selisih = target.subtract(realisasi);

            Double persentase = 0.0;
            if (target.compareTo(BigDecimal.ZERO) > 0) {
                persentase = realisasi.divide(target, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            }

            pbbDto.setTarget(target);
            pbbDto.setRealisasi(realisasi);
            pbbDto.setSelisih(selisih);
            pbbDto.setPersentasePencapaian(persentase);
            pbbDto.setDetails(List.of());

            results.add(pbbDto);
        }

        // Override data Pajak Mineral (urutan 6) dengan data dari E-PASIR
        Long realisasiMineralEpasir = executeSafely(() -> epasirService.getRealisasiMineralTahunan(tahun), 0L,
                "Mineral E-PASIR List");

        if (realisasiMineralEpasir > 0) {
            results.stream()
                    .filter(dto -> dto.getUrutan() == 6)
                    .findFirst()
                    .ifPresent(mineralDto -> {
                        BigDecimal realisasiEpasir = new BigDecimal(realisasiMineralEpasir);
                        BigDecimal target = mineralDto.getTarget();
                        BigDecimal selisih = target.subtract(realisasiEpasir);

                        Double persentase = 0.0;
                        if (target.compareTo(BigDecimal.ZERO) > 0) {
                            persentase = realisasiEpasir.divide(target, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal("100"))
                                    .doubleValue();
                        }

                        mineralDto.setRealisasi(realisasiEpasir);
                        mineralDto.setSelisih(selisih);
                        mineralDto.setPersentasePencapaian(persentase);
                        mineralDto.setJenisPajak("Pajak Mineral (E-PASIR)");
                    });
        }

        // Remove Pajak Sarang Burung Walet (urutan 9) - tidak ada target
        results.removeIf(dto -> dto.getUrutan() != null && dto.getUrutan() == 9);

        return results;
    }

    /**
     * Get breakdown detail per rekening untuk jenis pajak tertentu
     */
    private List<RekeningDetailDTO> getRekeningDetailByJenis(Integer idJenis, Integer tahun) {
        String sql = """
                SELECT
                    r.s_namakorek AS nama_rekening,
                    r.s_idkorek AS id_rekening,
                    CONCAT(r.s_tipekorek, '.', r.s_kelompokkorek, '.', r.s_jeniskorek, '.', r.s_objekkorek) AS kode_rekening,
                    COALESCE(SUM(t.t_jmlhpembayaran), 0) AS realisasi
                FROM s_rekening r
                LEFT JOIN t_transaksi t ON t.t_idkorek = r.s_idkorek
                    AND YEAR(t.t_tglpembayaran) = ?
                WHERE r.s_jenisobjek = ? AND r.s_golbunga IS NULL
                GROUP BY r.s_idkorek, r.s_namakorek, r.s_tipekorek, r.s_kelompokkorek, r.s_jeniskorek, r.s_objekkorek
                HAVING realisasi > 0
                ORDER BY realisasi DESC
                """;

        return executeSafely(() -> mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
            RekeningDetailDTO detail = new RekeningDetailDTO();
            detail.setNamaRekening(rs.getString("nama_rekening"));
            detail.setIdRekening(rs.getInt("id_rekening"));
            detail.setKodeRekening(rs.getString("kode_rekening"));
            detail.setRealisasi(rs.getBigDecimal("realisasi"));
            return detail;
        }, tahun, idJenis), new ArrayList<>(), "MySQL Rekening Detail");
    }

    /**
     * Get Trend Bulanan (Kumulatif)
     */
    public List<TrendBulananDTO> getTrendBulanan(Integer tahun) {
        String sql = """
                SELECT
                    MONTH(t_tglpembayaran) AS bulan,
                    SUM(t_jmlhpembayaran) AS realisasi_bulan
                FROM t_transaksi
                WHERE YEAR(t_tglpembayaran) = ?
                GROUP BY MONTH(t_tglpembayaran)
                ORDER BY bulan
                """;

        List<TrendBulananDTO> trends = executeSafely(() -> mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
            TrendBulananDTO dto = new TrendBulananDTO();
            dto.setBulan(rs.getInt("bulan"));
            dto.setNamaBulan(getNamaBulan(rs.getInt("bulan")));
            dto.setRealisasiBulan(rs.getBigDecimal("realisasi_bulan"));
            return dto;
        }, tahun), new ArrayList<>(), "MySQL Trend Bulanan");

        // Calculate kumulatif
        BigDecimal kumulatif = BigDecimal.ZERO;
        for (TrendBulananDTO trend : trends) {
            kumulatif = kumulatif.add(trend.getRealisasiBulan());
            trend.setRealisasiKumulatif(kumulatif);
        }

        return trends;
    }

    /**
     * Get Top 10 Kontributor
     */
    public List<TopKontributorDTO> getTopKontributor(Integer tahun, Integer limit) {
        String sql = """
                SELECT
                    COALESCE(wp.t_npwpd_lama, CAST(wp.t_idwp AS CHAR)) AS npwpd,
                    wp.t_nama AS nama_wp,
                    j.s_namajenis AS jenis_pajak,
                    SUM(t.t_jmlhpembayaran) AS total_pembayaran,
                    COUNT(t.t_idtransaksi) AS jumlah_transaksi
                FROM t_transaksi t
                JOIN t_wpobjek obj ON t.t_idwpobjek = obj.t_idobjek
                JOIN t_wp wp ON obj.t_idwp = wp.t_idwp
                LEFT JOIN s_jenisobjek j ON t.t_jenispajak = j.s_idjenis
                WHERE YEAR(t.t_tglpembayaran) = ?
                GROUP BY wp.t_idwp, wp.t_npwpd_lama, wp.t_nama, j.s_namajenis
                ORDER BY total_pembayaran DESC
                LIMIT ?
                """;

        return executeSafely(() -> mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
            TopKontributorDTO dto = new TopKontributorDTO();
            dto.setNpwpd(rs.getString("npwpd"));
            dto.setNamaWp(rs.getString("nama_wp"));
            dto.setJenisPajak(rs.getString("jenis_pajak"));
            dto.setTotalPembayaran(rs.getBigDecimal("total_pembayaran"));
            dto.setJumlahTransaksi(rs.getLong("jumlah_transaksi"));
            return dto;
        }, tahun, limit), new ArrayList<>(), "MySQL Top Kontributor");
    }

    /**
     * Get Realisasi Detail by Jenis Pajak
     */
    public List<Map<String, Object>> getRealisasiByJenisPajak(Integer tahun, String jenisPajakId) {
        String sql = """
                SELECT
                    MONTH(t.t_tglpembayaran) AS bulan,
                    j.s_namajenis AS jenis_pajak,
                    COUNT(t.t_idtransaksi) AS jumlah_transaksi,
                    SUM(t.t_jmlhpembayaran) AS total_realisasi
                FROM t_transaksi t
                LEFT JOIN s_jenisobjek j ON t.t_jenispajak = j.s_idjenis
                WHERE YEAR(t.t_tglpembayaran) = ?
                AND (? IS NULL OR t.t_jenispajak = ?)
                GROUP BY MONTH(t.t_tglpembayaran), j.s_namajenis
                ORDER BY bulan
                """;

        return executeSafely(() -> mysqlJdbcTemplate.queryForList(sql, tahun, jenisPajakId, jenisPajakId),
                new ArrayList<>(), "MySQL Realisasi By Jenis");
    }

    /**
     * Helper method untuk nama bulan
     */
    private String getNamaBulan(int bulan) {
        String[] namaBulan = {
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        return namaBulan[bulan - 1];
    }

    /**
     * Mapping dari nama jenis pajak di database ke nama kategori di frontend
     * Names must match exactly what's in master-pajak.json
     */
    private static final Map<String, String> KATEGORI_MAPPING = Map.of(
            "Pajak Hotel", "Perhotelan",
            "Pajak Restoran", "Restoran",
            "Pajak Hiburan", "Kesenian dan Hiburan",
            "Pajak Reklame", "Reklame",
            "Pajak Penerangan Jalan", "Tenaga Listrik",
            "Pajak Parkir", "Parkir",
            "Pajak Air Tanah", "Air Tanah",
            "Pajak Mineral Bukan Logam dan Batuan", "Minerba");

    /**
     * Get Realisasi Bulanan per Kategori Pajak untuk Dashboard Pajak
     * Returns data in the same format as master-pajak.json
     */
    public List<PajakDataDTO> getRealisasiBulananByKategori(Integer tahun) {
        String sql = """
                SELECT
                    j.s_namajenis AS jenis_pajak,
                    j.s_order AS urutan,
                    MONTH(t.t_tglpembayaran) AS bulan,
                    COALESCE(SUM(t.t_jmlhpembayaran), 0) AS total_realisasi
                FROM t_transaksi t
                JOIN s_jenisobjek j ON t.t_jenispajak = j.s_idjenis
                WHERE YEAR(t.t_tglpembayaran) = ?
                GROUP BY j.s_namajenis, j.s_order, MONTH(t.t_tglpembayaran)
                ORDER BY j.s_order, bulan
                """;

        List<PajakDataDTO> results = executeSafely(() -> mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
            PajakDataDTO dto = new PajakDataDTO();

            // Map database jenis pajak name to frontend kategori name
            String jenisPajak = rs.getString("jenis_pajak");
            String kategori = KATEGORI_MAPPING.getOrDefault(jenisPajak, jenisPajak);

            dto.setKategori(kategori);
            dto.setTahun(tahun);
            dto.setBulan(getNamaBulan(rs.getInt("bulan")));
            dto.setValue(rs.getBigDecimal("total_realisasi"));
            return dto;
        }, tahun), new ArrayList<>(), "MySQL Pajak Bulanan");

        System.out.println("✅ MySQL Pajak Bulanan returned " + results.size() + " records for year " + tahun);

        // Add BPHTB monthly data from PostgreSQL
        try {
            List<Map<String, Object>> bphtbData = bphtbService.getRealisasiBulanan(tahun);
            if (bphtbData != null) {
                for (Map<String, Object> row : bphtbData) {
                    PajakDataDTO dto = new PajakDataDTO();
                    dto.setKategori("BPHTB");
                    dto.setTahun(tahun);
                    int bulanNum = ((Number) row.get("bulan")).intValue();
                    dto.setBulan(getNamaBulan(bulanNum));
                    dto.setValue(new BigDecimal(row.get("realisasi").toString()));
                    results.add(dto);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not fetch BPHTB monthly data: " + e.getMessage());
        }

        // Add PBB P2 monthly data from Oracle SISMIOP
        try {
            List<Map<String, Object>> pbbData = sismiopService.getRealisasiPbbBulanan(tahun.toString());
            if (pbbData != null) {
                for (Map<String, Object> row : pbbData) {
                    PajakDataDTO dto = new PajakDataDTO();
                    dto.setKategori("PBB-P2");
                    dto.setTahun(tahun);
                    int bulanNum = ((Number) row.get("BULAN")).intValue();
                    dto.setBulan(getNamaBulan(bulanNum));
                    dto.setValue(new BigDecimal(row.get("REALISASI").toString()));
                    results.add(dto);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not fetch PBB P2 monthly data: " + e.getMessage());
        }

        return results;
    }
}
