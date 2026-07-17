package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.DashboardSummaryDTO;
import com.example.leaflet_geo.dto.RekeningDetailDTO;
import com.example.leaflet_geo.dto.TargetRealisasiDTO;
import com.example.leaflet_geo.dto.TopKontributorDTO;
import com.example.leaflet_geo.dto.TrendBulananDTO;
import com.example.leaflet_geo.dto.PajakDataDTO;
import com.example.leaflet_geo.dto.ProyeksiSummaryDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class PendapatanService {

    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate postgresJdbcTemplate;
    private final BphtbService bphtbService;
    private final SismiopService sismiopService;
    private final EpasirService epasirService;
    private final TaxProbabilityCalculationService taxProbabilityCalculationService;

    public PendapatanService(
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate,
            @Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbcTemplate,
            BphtbService bphtbService,
            SismiopService sismiopService,
            EpasirService epasirService,
            TaxProbabilityCalculationService taxProbabilityCalculationService) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.postgresJdbcTemplate = postgresJdbcTemplate;
        this.bphtbService = bphtbService;
        this.sismiopService = sismiopService;
        this.epasirService = epasirService;
        this.taxProbabilityCalculationService = taxProbabilityCalculationService;
    }

    /**
     * Target hardcode untuk setiap jenis pajak tahun 2026 berdasarkan tabel SKPD
     */
    private static final Map<Integer, Long> TARGET_HARDCODE = Map.of(
            4, 2_350_000_000L, // Pajak Reklame (4.1.01.09)
            8, 1_200_000_000L, // Pajak Air Tanah (4.1.01.12)
            6, 29_000_000_000L, // Pajak Mineral Bukan Logam dan Batuan (4.1.01.14)
            // PBB-P2 (4.1.01.15) -> 24_000_000_000 (tidak ada di map ini krn datanya dari
            // SISMIOP oracle)
            // BPHTB (4.1.01.16) -> 22_100_000_000 (tidak ada di map ini - dihandle
            // PostgreSQL BPHTB/hardcode bawah)
            2, 5_150_000_000L, // PBJT-Makanan dan Minuman (4.1.01.19.01) [Pajak Restoran]
            5, 45_000_000_000L, // PBJT-Tenaga Listrik (4.1.01.19.02) [Pajak Penerangan Jalan]
            1, 2_000_000_000L, // PBJT-Jasa Perhotelan (4.1.01.19.03) [Pajak Hotel]
            7, 550_000_000L, // PBJT-Jasa Parkir (4.1.01.19.04) [Pajak Parkir]
            3, 1_000_000_000L // PBJT-Jasa Kesenian dan Hiburan (4.1.01.19.05) [Pajak Hiburan]
    // Opsen PKB & Opsen BBNKB tidak ada ID jenis pajaknya di SIMATDA (1-10)
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
                    .get(15, TimeUnit.SECONDS); // 15 seconds timeout per call
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
                     WHERE YEAR(t_tglpembayaran) = ? AND t_jenispajak != 6) AS total_realisasi,

                    (SELECT COALESCE(SUM(s_targetjumlah), 0) FROM s_targetdetail td
                     JOIN s_target t ON td.s_idtargetheader = t.s_idtarget
                     WHERE t.s_tahuntarget = ?) AS total_target_db,

                    (SELECT COUNT(DISTINCT obj.t_idwp) FROM t_transaksi t
                     JOIN t_wpobjek obj ON t.t_idwpobjek = obj.t_idobjek
                     WHERE YEAR(t.t_tglpembayaran) = ?) AS total_wp,

                    (SELECT COUNT(DISTINCT t.t_idwpobjek) FROM t_transaksi t
                     WHERE YEAR(t.t_tglpembayaran) = ?) AS total_objek,

                    (SELECT COUNT(*) FROM t_transaksi
                     WHERE YEAR(t_tglpembayaran) = ?) AS total_transaksi,

                    (SELECT COUNT(DISTINCT t_jenispajak) FROM t_transaksi
                     WHERE YEAR(t_tglpembayaran) = ?) AS jenis_pajak_aktif
                """;

        // Wrap MySQL call
        Map<String, Object> result = executeSafely(
                () -> mysqlJdbcTemplate.queryForMap(sql, tahun, tahun, tahun, tahun, tahun, tahun),
                Map.of(
                        "total_realisasi", 0,
                        "total_target_db", 0,
                        "total_wp", 0,
                        "total_objek", 0,
                        "total_transaksi", 0,
                        "jenis_pajak_aktif", 0),
                "MySQL Dashboard Summary");

        // Ambil total target dari tabel system.anggaran PostgreSQL
        BigDecimal totalTarget = executeSafely(() -> postgresJdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(nilai_anggaran), 0) FROM system.anggaran WHERE tahun_anggaran = ?",
                BigDecimal.class,
                tahun), BigDecimal.ZERO, "PostgreSQL Total Anggaran");

        BigDecimal totalRealisasi = new BigDecimal(result.get("total_realisasi").toString());

        // Tambahkan realisasi BPHTB dari database
        Long realisasiBphtb = executeSafely(() -> bphtbService.getRealisasiTahunan(tahun), 0L, "BPHTB Realisasi");

        totalRealisasi = totalRealisasi.add(new BigDecimal(realisasiBphtb));

        // Tambahkan realisasi PBB P2 dari SISMIOP
        Long realisasiPbb = executeSafely(() -> sismiopService.getRealisasiPbbTahunan(tahun.toString()), 0L,
                "PBB Realisasi");

        totalRealisasi = totalRealisasi.add(new BigDecimal(realisasiPbb));

        // Tambahkan realisasi Minerba dari E-PASIR
        Long realisasiEpasir = executeSafely(() -> epasirService.getRealisasiTotalTahunan(tahun), 0L,
                "E-PASIR Realisasi");
        totalRealisasi = totalRealisasi.add(new BigDecimal(realisasiEpasir));

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
        // Ambil mapping anggaran (target) dari tabel system.anggaran di PostgreSQL
        Map<String, BigDecimal> mapAnggaran = new HashMap<>();
        executeSafely(() -> {
            postgresJdbcTemplate.query(
                    "SELECT jenis_pajak, nilai_anggaran FROM system.anggaran WHERE tahun_anggaran = ?",
                    rs -> {
                        String jp = rs.getString("jenis_pajak");
                        if (jp != null) {
                            mapAnggaran.put(jp.trim().toLowerCase(), rs.getBigDecimal("nilai_anggaran"));
                        }
                    },
                    tahun);
            return null;
        }, null, "Load PostgreSQL Anggaran");
        String sql = """
                SELECT
                    j.s_idjenis AS id_jenis,
                    j.s_namajenis AS jenis_pajak,
                    COALESCE(r.total_realisasi, 0) AS realisasi,
                    COALESCE(tg.total_target, 0) AS target_db
                FROM s_jenisobjek j
                LEFT JOIN (
                    SELECT
                        t_jenispajak,
                        SUM(t_jmlhpembayaran) AS total_realisasi
                    FROM t_transaksi
                    WHERE YEAR(t_tglpembayaran) = ?
                    GROUP BY t_jenispajak
                ) r ON r.t_jenispajak = j.s_idjenis
                LEFT JOIN (
                    SELECT
                        rek.s_jenisobjek,
                        SUM(td.s_targetjumlah) AS total_target
                    FROM s_targetdetail td
                    JOIN s_target t ON t.s_idtarget = td.s_idtargetheader
                    JOIN s_rekening rek ON rek.s_idkorek = td.s_targetrekening
                    WHERE t.s_tahuntarget = ?
                    GROUP BY rek.s_jenisobjek
                ) tg ON tg.s_jenisobjek = j.s_idjenis
                """;

        List<TargetRealisasiDTO> results = executeSafely(() -> mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
            TargetRealisasiDTO dto = new TargetRealisasiDTO();
            Integer idJenis = rs.getInt("id_jenis");

            // Map name according to user definition
            String jenisPajakRaw = rs.getString("jenis_pajak");
            if (jenisPajakRaw != null) {
                jenisPajakRaw = jenisPajakRaw.trim();
            }
            String mappedName = KATEGORI_MAPPING.getOrDefault(jenisPajakRaw, jenisPajakRaw);
            dto.setJenisPajak(mappedName);

            // Gunakan table system.anggaran PostgreSQL - direct lookup dengan mapped name
            // lowercase
            String lookupKey = mappedName != null ? mappedName.toLowerCase() : "";
            BigDecimal target = mapAnggaran.getOrDefault(lookupKey, BigDecimal.ZERO);

            BigDecimal realisasi = rs.getBigDecimal("realisasi");

            // Jika Minerba, override realisasi dari E-Pasir
            if (idJenis == 6) { // 6 adalah ID untuk Minerba di s_jenisobjek
                Long realisasiEpasir = executeSafely(() -> epasirService.getRealisasiTotalTahunan(tahun), 0L,
                        "E-PASIR Realisasi List");
                realisasi = new BigDecimal(realisasiEpasir);
            }

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
            dto.setDetails(getRekeningDetailByJenis(idJenis, tahun));

            return dto;
        }, tahun, tahun), new ArrayList<>(), "MySQL Target Realisasi");

        // Tambahkan data BPHTB dari database PostgreSQL
        Long realisasiBphtb = executeSafely(() -> bphtbService.getRealisasiTahunan(tahun), 0L, "BPHTB Realisasi List");
        BigDecimal targetBphtb = mapAnggaran
                .getOrDefault("Bea Perolehan Hak Atas Tanah dan Bangunan (BPHTB)".toLowerCase(), BigDecimal.ZERO);

        if (true) { // Always add BPHTB row even if 0
            TargetRealisasiDTO bphtbDto = new TargetRealisasiDTO();
            bphtbDto.setJenisPajak("Bea Perolehan Hak Atas Tanah dan Bangunan (BPHTB)");
            BigDecimal target = targetBphtb;
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
        BigDecimal targetPbb = mapAnggaran
                .getOrDefault("Pajak Bumi dan Bangunan Perdesaan dan Perkotaan (PBBP2)".toLowerCase(), BigDecimal.ZERO);

        if (true) {
            TargetRealisasiDTO pbbDto = new TargetRealisasiDTO();
            pbbDto.setJenisPajak("Pajak Bumi dan Bangunan Perdesaan dan Perkotaan (PBBP2)");

            BigDecimal target = targetPbb;
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

        // Tambahkan Opsen PKB
        TargetRealisasiDTO pkbDto = new TargetRealisasiDTO();
        pkbDto.setJenisPajak("Opsen PKB");

        BigDecimal targetPkb = mapAnggaran.getOrDefault("Opsen PKB".toLowerCase(), BigDecimal.ZERO);
        BigDecimal realisasiPkb = BigDecimal.ZERO;
        BigDecimal selisihPkb = targetPkb.subtract(realisasiPkb);

        pkbDto.setTarget(targetPkb);
        pkbDto.setRealisasi(realisasiPkb);
        pkbDto.setSelisih(selisihPkb);
        pkbDto.setPersentasePencapaian(0.0);
        pkbDto.setDetails(List.of());
        results.add(pkbDto);

        // Tambahkan Opsen BBNKB
        TargetRealisasiDTO bbnkbDto = new TargetRealisasiDTO();
        bbnkbDto.setJenisPajak("Opsen BBNKB");

        BigDecimal targetBbnkb = mapAnggaran.getOrDefault("Opsen BBNKB".toLowerCase(), BigDecimal.ZERO);
        BigDecimal realisasiBbnkb = BigDecimal.ZERO;
        BigDecimal selisihBbnkb = targetBbnkb.subtract(realisasiBbnkb);

        bbnkbDto.setTarget(targetBbnkb);
        bbnkbDto.setRealisasi(realisasiBbnkb);
        bbnkbDto.setSelisih(selisihBbnkb);
        bbnkbDto.setPersentasePencapaian(0.0);
        bbnkbDto.setDetails(List.of());
        results.add(bbnkbDto);

        // Hapus Pajak Sarang Burung Walet dan assign urutan secara manual
        results.removeIf(dto -> dto.getJenisPajak() != null && dto.getJenisPajak().contains("Walet"));

        // Sortir sesuai urutan mapping and format urutannya ke parameter Urutan yang
        // diklik dashboard
        for (TargetRealisasiDTO dto : results) {
            dto.setUrutan(KATEGORI_URUTAN.getOrDefault(dto.getJenisPajak(), 99));
            taxProbabilityCalculationService.enrichWithProbability(dto, tahun);
        }
        results.sort((a, b) -> Integer.compare(a.getUrutan(), b.getUrutan()));

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
                WHERE YEAR(t_tglpembayaran) = ? AND t_jenispajak != 6
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

        // Merge E-PASIR Data ke Trend Bulanan
        try {
            List<Map<String, Object>> epasirData = epasirService.getRealisasiBulanan(tahun);
            if (epasirData != null) {
                for (Map<String, Object> row : epasirData) {
                    int bulanNum = ((Number) row.get("bulan")).intValue();
                    BigDecimal realisasiEpasir = new BigDecimal(row.get("realisasi").toString());

                    // Find existing month or create new
                    TrendBulananDTO existing = trends.stream().filter(t -> t.getBulan() == bulanNum).findFirst()
                            .orElse(null);
                    if (existing != null) {
                        existing.setRealisasiBulan(existing.getRealisasiBulan().add(realisasiEpasir));
                    } else {
                        TrendBulananDTO dto = new TrendBulananDTO();
                        dto.setBulan(bulanNum);
                        dto.setNamaBulan(getNamaBulan(bulanNum));
                        dto.setRealisasiBulan(realisasiEpasir);
                        trends.add(dto);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not merge E-PASIR to Trend Bulanan: " + e.getMessage());
        }

        // Sort just in case we appended new months
        trends.sort((t1, t2) -> Integer.compare(t1.getBulan(), t2.getBulan()));

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
                WHERE YEAR(t.t_tglpembayaran) = ? AND t.t_jenispajak != 6
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
        // Jika jenisPajakId == 6 (Minerba), langung ke E-Pasir (karena tidak ada di
        // MySQL)
        if ("6".equals(jenisPajakId)) {
            List<Map<String, Object>> result = new ArrayList<>();
            try {
                List<Map<String, Object>> epasirData = epasirService.getRealisasiBulanan(tahun);
                if (epasirData != null) {
                    for (Map<String, Object> row : epasirData) {
                        result.add(Map.of(
                                "bulan", row.get("bulan"),
                                "jenis_pajak", "Pajak Mineral Bukan Logam dan Batuan",
                                "jumlah_transaksi", 0, // Belum ada count transaksi dari Epasir
                                "total_realisasi", row.get("realisasi")));
                    }
                }
            } catch (Exception e) {
            }
            return result;
        }

        String sql = """
                SELECT
                    MONTH(t.t_tglpembayaran) AS bulan,
                    j.s_namajenis AS jenis_pajak,
                    COUNT(t.t_idtransaksi) AS jumlah_transaksi,
                    SUM(t.t_jmlhpembayaran) AS total_realisasi
                FROM t_transaksi t
                LEFT JOIN s_jenisobjek j ON t.t_jenispajak = j.s_idjenis
                WHERE YEAR(t.t_tglpembayaran) = ? AND t.t_jenispajak != 6
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
     */
    private static final Map<String, String> KATEGORI_MAPPING = Map.of(
            "Pajak Reklame", "Pajak Reklame",
            "Pajak Air Tanah", "Pajak Air Tanah",
            "Pajak Hotel", "PBJT-Jasa Perhotelan",
            "Pajak Restoran", "PBJT-Makanan dan/atau Minuman",
            "Pajak Hiburan", "PBJT-Jasa Kesenian dan Hiburan",
            "Pajak Penerangan Jalan", "PBJT-Tenaga Listrik",
            "Pajak Parkir", "PBJT-Jasa Parkir");

    /**
     * Urutan khusus kategori
     */
    private static final Map<String, Integer> KATEGORI_URUTAN = Map.ofEntries(
            Map.entry("Pajak Reklame", 1),
            Map.entry("Pajak Air Tanah", 2),
            Map.entry("Pajak Mineral Bukan Logam dan Batuan", 3),
            Map.entry("Pajak Bumi dan Bangunan Perdesaan dan Perkotaan (PBBP2)", 4),
            Map.entry("Bea Perolehan Hak Atas Tanah dan Bangunan (BPHTB)", 5),
            Map.entry("PBJT-Makanan dan/atau Minuman", 6),
            Map.entry("PBJT-Tenaga Listrik", 7),
            Map.entry("PBJT-Jasa Perhotelan", 8),
            Map.entry("PBJT-Jasa Parkir", 9),
            Map.entry("PBJT-Jasa Kesenian dan Hiburan", 10),
            Map.entry("Opsen PKB", 11),
            Map.entry("Opsen BBNKB", 12));

    /**
     * Get Realisasi Bulanan per Kategori Pajak untuk Dashboard Pajak
     * Returns data in the same format as master-pajak.json
     */
    public List<PajakDataDTO> getRealisasiBulananByKategori(Integer tahun) {
        String sql = """
                SELECT
                    j.s_namajenis AS jenis_pajak,
                    MONTH(t.t_tglpembayaran) AS bulan,
                    COALESCE(SUM(t.t_jmlhpembayaran), 0) AS total_realisasi
                FROM t_transaksi t
                JOIN s_jenisobjek j ON t.t_jenispajak = j.s_idjenis
                WHERE YEAR(t.t_tglpembayaran) = ? AND j.s_namajenis != 'Pajak Mineral Bukan Logam dan Batuan'
                GROUP BY j.s_namajenis, MONTH(t.t_tglpembayaran)
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
                    dto.setKategori("Bea Perolehan Hak Atas Tanah dan Bangunan (BPHTB)");
                    dto.setTahun(tahun);
                    int bulanNum = ((Number) row.get("bulan")).intValue();
                    dto.setBulan(getNamaBulan(bulanNum));
                    dto.setValue(new BigDecimal(row.get("realisasi").toString()));
                    results.add(dto);
                }
            }
        } catch (Exception e) {
        }

        // Add Minerba (E-PASIR) monthly data from PostgreSQL
        try {
            List<Map<String, Object>> epasirData = epasirService.getRealisasiBulanan(tahun);
            if (epasirData != null) {
                for (Map<String, Object> row : epasirData) {
                    PajakDataDTO dto = new PajakDataDTO();
                    dto.setKategori("Pajak Mineral Bukan Logam dan Batuan");
                    dto.setTahun(tahun);
                    int bulanNum = ((Number) row.get("bulan")).intValue();
                    dto.setBulan(getNamaBulan(bulanNum));
                    dto.setValue(new BigDecimal(row.get("realisasi").toString()));
                    results.add(dto);
                }
            }
        } catch (Exception e) {
        }

        // Add PBB P2 monthly data from Oracle SISMIOP
        try {
            List<Map<String, Object>> pbbData = sismiopService.getRealisasiPbbBulanan(tahun.toString());
            if (pbbData != null) {
                for (Map<String, Object> row : pbbData) {
                    PajakDataDTO dto = new PajakDataDTO();
                    dto.setKategori("Pajak Bumi dan Bangunan Perdesaan dan Perkotaan (PBBP2)");
                    dto.setTahun(tahun);
                    int bulanNum = ((Number) row.get("BULAN")).intValue();
                    dto.setBulan(getNamaBulan(bulanNum));
                    dto.setValue(new BigDecimal(row.get("REALISASI").toString()));
                    results.add(dto);
                }
            }
        } catch (Exception e) {
        }

        // Urutkan List berdasarkan nama kategori yang sudah dipetakan
        results.sort((a, b) -> {
            Integer urutanA = KATEGORI_URUTAN.getOrDefault(a.getKategori(), 99);
            Integer urutanB = KATEGORI_URUTAN.getOrDefault(b.getKategori(), 99);
            if (urutanA.equals(urutanB)) {
                // Return mapping based on month if category is same
                int bulanA = java.util.Arrays.asList("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli",
                        "Agustus", "September", "Oktober", "November", "Desember").indexOf(a.getBulan());
                int bulanB = java.util.Arrays.asList("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli",
                        "Agustus", "September", "Oktober", "November", "Desember").indexOf(b.getBulan());
                return Integer.compare(bulanA, bulanB);
            }
            return urutanA.compareTo(urutanB);
        });

        return results;
    }

    /**
     * Get Data Proyeksi IIPA AI untuk Tahun 2026
     */
    public List<Map<String, Object>> getProyeksiIipa(Integer tahun) {
        List<Map<String, Object>> results = new ArrayList<>();
        String jsonPath = "D:/BPRD/leaflet-geo/python_bridge/proyeksi_iipa_2026.json";
        try {
            java.io.File file = new java.io.File(jsonPath);
            if (file.exists()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                results = mapper.readValue(file, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            System.err.println("Error reading proyeksi JSON: " + e.getMessage());
        }
        return results;
    }

    /**
     * Get Realtime Status Proyeksi untuk UI Stepper Progress
     */
    public Map<String, Object> getProyeksiStatus() {
        Map<String, Object> status = new HashMap<>();
        String statusPath = "D:/BPRD/leaflet-geo/python_bridge/proyeksi_status.json";
        try {
            java.io.File file = new java.io.File(statusPath);
            if (file.exists()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                status = mapper.readValue(file, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } else {
                status.put("stage", 0);
                status.put("percent", 0);
                status.put("message", "Sistem siap untuk analisis");
                status.put("isRunning", false);
                status.put("isError", false);
            }
        } catch (Exception e) {
            status.put("stage", 0);
            status.put("percent", 0);
            status.put("message", "Ready");
            status.put("isRunning", false);
            status.put("isError", false);
        }
        return status;
    }

    /**
     * Trigger Background Process Execution untuk AI Proyeksi
     */
    public Map<String, Object> triggerProyeksiExecution() {
        Map<String, Object> response = new HashMap<>();
        try {
            String statusPath = "D:/BPRD/leaflet-geo/python_bridge/proyeksi_status.json";
            Map<String, Object> resetStatus = new HashMap<>();
            resetStatus.put("stage", 1);
            resetStatus.put("percent", 15);
            resetStatus.put("message", "STAGE 1: Structural Break Detection & ITSA segmented regression (Bai-Perron)...");
            resetStatus.put("isRunning", true);
            resetStatus.put("isError", false);
            resetStatus.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.writeValue(new java.io.File(statusPath), resetStatus);
            } catch (Exception ex) {
                System.err.println("Could not reset status file: " + ex.getMessage());
            }

            String scriptPath = "D:/BPRD/leaflet-geo/python_bridge/extract_and_train_iipa.py";
            String pythonExec = "C:/Users/Zephyrus/AppData/Local/Programs/Python/Python312/python.exe";
            
            ProcessBuilder pb = new ProcessBuilder(pythonExec, scriptPath);
            pb.directory(new java.io.File("D:/BPRD/leaflet-geo/python_bridge"));
            pb.start(); // Runs asynchronously in background
            
            response.put("success", true);
            response.put("message", "Analisis Adaptif IIPA Machine Learning berhasil dijalankan di background server!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal memicu analisis: " + e.getMessage());
        }
        return response;
    }
}
