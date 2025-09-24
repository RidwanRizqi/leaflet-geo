package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.entity.DatObjekPajak;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class DatObjekPajakRepository {

    private final JdbcTemplate oracleJdbcTemplate;

    public DatObjekPajakRepository(@Qualifier("oracleJdbcTemplate") JdbcTemplate oracleJdbcTemplate) {
        this.oracleJdbcTemplate = oracleJdbcTemplate;
    }

    private DatObjekPajak mapRowToDatObjekPajak(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        DatObjekPajak objekPajak = new DatObjekPajak();
        objekPajak.setKdPropinsi(rs.getString("KD_PROPINSI"));
        objekPajak.setKdDati2(rs.getString("KD_DATI2"));
        objekPajak.setKdKecamatan(rs.getString("KD_KECAMATAN"));
        objekPajak.setKdKelurahan(rs.getString("KD_KELURAHAN"));
        objekPajak.setKdBlok(rs.getString("KD_BLOK"));
        objekPajak.setNoUrut(rs.getString("NO_URUT"));
        objekPajak.setKdJnsOp(rs.getString("KD_JNS_OP"));
        objekPajak.setSubjekPajakId(rs.getString("SUBJEK_PAJAK_ID"));
        objekPajak.setNoFormulirSpop(rs.getString("NO_FORMULIR_SPOP"));
        objekPajak.setNoPersil(rs.getString("NO_PERSIL"));
        objekPajak.setJalanOp(rs.getString("JALAN_OP"));
        objekPajak.setBlokKavNoOp(rs.getString("BLOK_KAV_NO_OP"));
        objekPajak.setRwOp(rs.getString("RW_OP"));
        objekPajak.setRtOp(rs.getString("RT_OP"));
        objekPajak.setKdStatusCabang(rs.getObject("KD_STATUS_CABANG", Integer.class));
        objekPajak.setKdStatusWp(rs.getString("KD_STATUS_WP"));
        objekPajak.setTotalLuasBumi(rs.getObject("TOTAL_LUAS_BUMI", BigDecimal.class));
        objekPajak.setTotalLuasBng(rs.getObject("TOTAL_LUAS_BNG", BigDecimal.class));
        objekPajak.setNjopBumi(rs.getObject("NJOP_BUMI", BigDecimal.class));
        objekPajak.setNjopBng(rs.getObject("NJOP_BNG", BigDecimal.class));
        objekPajak.setStatusPetaOp(rs.getObject("STATUS_PETA_OP", Integer.class));
        objekPajak.setJnsTransaksiOp(rs.getString("JNS_TRANSAKSI_OP"));
        objekPajak.setTglPendataanOp(rs.getObject("TGL_PENDATAAN_OP", LocalDate.class));
        objekPajak.setNipPendata(rs.getString("NIP_PENDATA"));
        objekPajak.setTglPemeriksaanOp(rs.getObject("TGL_PEMERIKSAAN_OP", LocalDate.class));
        objekPajak.setNipPemeriksaOp(rs.getString("NIP_PEMERIKSA_OP"));
        objekPajak.setTglPerekamanOp(rs.getObject("TGL_PEREKAMAN_OP", LocalDate.class));
        objekPajak.setNipPerekamOp(rs.getString("NIP_PEREKAM_OP"));
        objekPajak.setNoSertifikat(rs.getString("NO_SERTIFIKAT"));
        objekPajak.setKeteranganOp(rs.getString("KETERANGAN_OP"));
        objekPajak.setKeteranganSpop(rs.getString("KETERANGAN_SPOP"));
        objekPajak.setLatitude(rs.getString("LATITUDE"));
        objekPajak.setLongitude(rs.getString("LONGITUDE"));
        
        // Reference data (nullable)
        objekPajak.setNmKecamatan(rs.getString("NM_KECAMATAN"));
        objekPajak.setNmKelurahan(rs.getString("NM_KELURAHAN"));
        objekPajak.setNmPropinsi(rs.getString("NM_PROPINSI"));
        objekPajak.setNmDati2(rs.getString("NM_DATI2"));
        
        return objekPajak;
    }

    public List<DatObjekPajak> findAll() {
        String sql = "SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_BLOK, NO_URUT, KD_JNS_OP";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak);
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM SYSTEM.DAT_OBJEK_PAJAK";
        return oracleJdbcTemplate.queryForObject(sql, Integer.class);
    }

    public Optional<DatObjekPajak> findById(String kdPropinsi, String kdDati2, String kdKecamatan, String kdKelurahan, String kdBlok, String noUrut, String kdJnsOp) {
        String sql = "SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? AND KD_KELURAHAN = ? AND KD_BLOK = ? AND NO_URUT = ? AND KD_JNS_OP = ?";
        List<DatObjekPajak> result = oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, kdBlok, noUrut, kdJnsOp);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<DatObjekPajak> findByKecamatan(String kdPropinsi, String kdDati2, String kdKecamatan) {
        String sql = "SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? ORDER BY KD_KELURAHAN, KD_BLOK, NO_URUT, KD_JNS_OP";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, kdPropinsi, kdDati2, kdKecamatan);
    }

    public List<DatObjekPajak> findByKelurahan(String kdPropinsi, String kdDati2, String kdKecamatan, String kdKelurahan) {
        String sql = "SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? AND KD_KELURAHAN = ? ORDER BY KD_BLOK, NO_URUT, KD_JNS_OP";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, kdPropinsi, kdDati2, kdKecamatan, kdKelurahan);
    }

    public List<DatObjekPajak> findByKecamatanAndKelurahanAndNoUrut(String kdPropinsi, String kdDati2, String kdKecamatan, String kdKelurahan, String noUrut) {
        String sql = "SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? AND KD_KELURAHAN = ? AND NO_URUT = ? ORDER BY KD_BLOK, KD_JNS_OP";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, noUrut);
    }

    public List<DatObjekPajak> findBySubjekPajakId(String subjekPajakId) {
        String sql = "SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK WHERE SUBJEK_PAJAK_ID = ? ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_BLOK, NO_URUT, KD_JNS_OP";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, subjekPajakId);
    }

    public List<DatObjekPajak> findByNoFormulirSpop(String noFormulirSpop) {
        String sql = "SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK WHERE NO_FORMULIR_SPOP = ? ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_BLOK, NO_URUT, KD_JNS_OP";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, noFormulirSpop);
    }

    public List<DatObjekPajak> findByNpwp(String npwp) {
        String sql = "SELECT op.* FROM SYSTEM.DAT_OBJEK_PAJAK op " +
                    "INNER JOIN SYSTEM.DAT_SUBJEK_PAJAK sp ON op.SUBJEK_PAJAK_ID = sp.SUBJEK_PAJAK_ID " +
                    "WHERE sp.NPWP = ? ORDER BY op.KD_PROPINSI, op.KD_DATI2, op.KD_KECAMATAN, op.KD_KELURAHAN, op.KD_BLOK, op.NO_URUT, op.KD_JNS_OP";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, npwp);
    }

    public List<DatObjekPajak> findPaginated(int offset, int pageSize) {
        String sql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_BLOK, NO_URUT, KD_JNS_OP) a WHERE ROWNUM <= ?) WHERE rnum > ?";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, offset + pageSize, offset);
    }

    public List<DatObjekPajak> findPaginatedByKecamatan(String kdPropinsi, String kdDati2, String kdKecamatan, int offset, int pageSize) {
        String sql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? ORDER BY KD_KELURAHAN, KD_BLOK, NO_URUT, KD_JNS_OP) a WHERE ROWNUM <= ?) WHERE rnum > ?";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, kdPropinsi, kdDati2, kdKecamatan, offset + pageSize, offset);
    }

    public int countByKecamatan(String kdPropinsi, String kdDati2, String kdKecamatan) {
        String sql = "SELECT COUNT(*) FROM SYSTEM.DAT_OBJEK_PAJAK WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ?";
        return oracleJdbcTemplate.queryForObject(sql, Integer.class, kdPropinsi, kdDati2, kdKecamatan);
    }

    public List<DatObjekPajak> findPaginatedByKelurahan(String kdPropinsi, String kdDati2, String kdKecamatan, String kdKelurahan, int offset, int pageSize) {
        String sql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM SYSTEM.DAT_OBJEK_PAJAK WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? AND KD_KELURAHAN = ? ORDER BY KD_BLOK, NO_URUT, KD_JNS_OP) a WHERE ROWNUM <= ?) WHERE rnum > ?";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, offset + pageSize, offset);
    }

    public int countByKelurahan(String kdPropinsi, String kdDati2, String kdKecamatan, String kdKelurahan) {
        String sql = "SELECT COUNT(*) FROM SYSTEM.DAT_OBJEK_PAJAK WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? AND KD_KELURAHAN = ?";
        return oracleJdbcTemplate.queryForObject(sql, Integer.class, kdPropinsi, kdDati2, kdKecamatan, kdKelurahan);
    }

    /**
     * Find by ID with reference data (kecamatan, kelurahan, propinsi, dati2)
     */
    public Optional<DatObjekPajak> findByIdWithReferences(String kdPropinsi, String kdDati2, String kdKecamatan, String kdKelurahan, String kdBlok, String noUrut, String kdJnsOp) {
        String sql = """
            SELECT op.*, 
                   kec.NM_KECAMATAN,
                   kel.NM_KELURAHAN,
                   prop.NM_PROPINSI,
                   dati2.NM_DATI2
            FROM SYSTEM.DAT_OBJEK_PAJAK op
            LEFT JOIN SYSTEM.REF_KECAMATAN kec ON op.KD_PROPINSI = kec.KD_PROPINSI 
                                                AND op.KD_DATI2 = kec.KD_DATI2 
                                                AND op.KD_KECAMATAN = kec.KD_KECAMATAN
            LEFT JOIN SYSTEM.REF_KELURAHAN kel ON op.KD_PROPINSI = kel.KD_PROPINSI 
                                                AND op.KD_DATI2 = kel.KD_DATI2 
                                                AND op.KD_KECAMATAN = kel.KD_KECAMATAN 
                                                AND op.KD_KELURAHAN = kel.KD_KELURAHAN
            LEFT JOIN SYSTEM.REF_PROPINSI prop ON op.KD_PROPINSI = prop.KD_PROPINSI
            LEFT JOIN SYSTEM.REF_DATI2 dati2 ON op.KD_PROPINSI = dati2.KD_PROPINSI 
                                              AND op.KD_DATI2 = dati2.KD_DATI2
            WHERE op.KD_PROPINSI = ? AND op.KD_DATI2 = ? AND op.KD_KECAMATAN = ? 
                  AND op.KD_KELURAHAN = ? AND op.KD_BLOK = ? AND op.NO_URUT = ? AND op.KD_JNS_OP = ?
            """;
        
        List<DatObjekPajak> result = oracleJdbcTemplate.query(sql, this::mapRowToDatObjekPajak, 
            kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, kdBlok, noUrut, kdJnsOp);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
