package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.DashboardSummaryDTO;
import com.example.leaflet_geo.dto.RekeningDetailDTO;
import com.example.leaflet_geo.dto.TargetRealisasiDTO;
import com.example.leaflet_geo.dto.TopKontributorDTO;
import com.example.leaflet_geo.dto.TrendBulananDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class PendapatanService {

    private final JdbcTemplate mysqlJdbcTemplate;

    public PendapatanService(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }

    /**
     * Target hardcode untuk setiap jenis pajak
     */
    private static final Map<Integer, Long> TARGET_HARDCODE = Map.of(
        1, 2_000_000_000L,      // Pajak Hotel
        2, 5_150_000_000L,      // Pajak Restoran
        3, 1_000_000_000L,      // Pajak Hiburan
        5, 45_000_000_000L,     // Pajak Penerangan Jalan
        7, 550_000_000L,        // Pajak Parkir
        4, 2_350_000_000L,      // Pajak Reklame
        6, 29_000_000_000L,     // Pajak Mineral Bukan Logam dan Batuan
        8, 1_200_000_000L       // Pajak Air Tanah
        // Pajak Sarang Burung Walet (ID 9) tidak ada target
    );
    
    private long getTotalTargetHardcode() {
        return TARGET_HARDCODE.values().stream()
            .mapToLong(Long::longValue)
            .sum();
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

        Map<String, Object> result = mysqlJdbcTemplate.queryForMap(sql, tahun, tahun, tahun);

        // Gunakan target hardcode
        BigDecimal totalTarget = new BigDecimal(getTotalTargetHardcode());
        BigDecimal totalRealisasi = new BigDecimal(result.get("total_realisasi").toString());
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
        summary.setJenisPajakAktif(((Number) result.get("jenis_pajak_aktif")).longValue());
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

        return mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
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
            dto.setDetails(getRekeningDetailByJenis(idJenis, tahun));
            
            return dto;
        }, tahun);
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
            
        return mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
            RekeningDetailDTO detail = new RekeningDetailDTO();
            detail.setNamaRekening(rs.getString("nama_rekening"));
            detail.setIdRekening(rs.getInt("id_rekening"));
            detail.setKodeRekening(rs.getString("kode_rekening"));
            detail.setRealisasi(rs.getBigDecimal("realisasi"));
            return detail;
        }, tahun, idJenis);
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

        List<TrendBulananDTO> trends = mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
            TrendBulananDTO dto = new TrendBulananDTO();
            dto.setBulan(rs.getInt("bulan"));
            dto.setNamaBulan(getNamaBulan(rs.getInt("bulan")));
            dto.setRealisasiBulan(rs.getBigDecimal("realisasi_bulan"));
            return dto;
        }, tahun);

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
                wp.t_npwpd AS npwpd,
                wp.t_nama AS nama_wp,
                j.s_namajenis AS jenis_pajak,
                SUM(t.t_jmlhpembayaran) AS total_pembayaran,
                COUNT(t.t_idtransaksi) AS jumlah_transaksi
            FROM t_transaksi t
            JOIN t_wpobjek obj ON t.t_idwpobjek = obj.t_idobjek
            JOIN t_wp wp ON obj.t_idwp = wp.t_idwp
            LEFT JOIN s_jenisobjek j ON t.t_jenispajak = j.s_idjenis
            WHERE YEAR(t.t_tglpembayaran) = ?
            GROUP BY wp.t_idwp, wp.t_npwpd, wp.t_nama, j.s_namajenis
            ORDER BY total_pembayaran DESC
            LIMIT ?
            """;

        return mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
            TopKontributorDTO dto = new TopKontributorDTO();
            dto.setNpwpd(rs.getString("npwpd"));
            dto.setNamaWp(rs.getString("nama_wp"));
            dto.setJenisPajak(rs.getString("jenis_pajak"));
            dto.setTotalPembayaran(rs.getBigDecimal("total_pembayaran"));
            dto.setJumlahTransaksi(rs.getLong("jumlah_transaksi"));
            return dto;
        }, tahun, limit);
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

        return mysqlJdbcTemplate.queryForList(sql, tahun, jenisPajakId, jenisPajakId);
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
}
