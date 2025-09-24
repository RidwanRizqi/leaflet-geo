package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.entity.DatSubjekPajak;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DatSubjekPajakRepository {

    private final JdbcTemplate oracleJdbcTemplate;

    public DatSubjekPajakRepository(@Qualifier("oracleJdbcTemplate") JdbcTemplate oracleJdbcTemplate) {
        this.oracleJdbcTemplate = oracleJdbcTemplate;
    }

    private DatSubjekPajak mapRowToDatSubjekPajak(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        DatSubjekPajak subjekPajak = new DatSubjekPajak();
        subjekPajak.setSubjekPajakId(rs.getString("SUBJEK_PAJAK_ID"));
        subjekPajak.setNmWp(rs.getString("NM_WP"));
        subjekPajak.setJalanWp(rs.getString("JALAN_WP"));
        subjekPajak.setBlokKavNoWp(rs.getString("BLOK_KAV_NO_WP"));
        subjekPajak.setRwWp(rs.getString("RW_WP"));
        subjekPajak.setRtWp(rs.getString("RT_WP"));
        subjekPajak.setKelurahanWp(rs.getString("KELURAHAN_WP"));
        subjekPajak.setKotaWp(rs.getString("KOTA_WP"));
        subjekPajak.setKdPosWp(rs.getString("KD_POS_WP"));
        subjekPajak.setTelpWp(rs.getString("TELP_WP"));
        subjekPajak.setNpwp(rs.getString("NPWP"));
        subjekPajak.setStatusPekerjaanWp(rs.getString("STATUS_PEKERJAAN_WP"));
        subjekPajak.setNpwpd(rs.getString("NPWPD"));
        subjekPajak.setEmail(rs.getString("EMAIL"));
        
        // Reference data (nullable)
        subjekPajak.setNmKecamatan(rs.getString("NM_KECAMATAN"));
        subjekPajak.setNmKelurahan(rs.getString("NM_KELURAHAN"));
        subjekPajak.setNmPropinsi(rs.getString("NM_PROPINSI"));
        subjekPajak.setNmDati2(rs.getString("NM_DATI2"));
        
        return subjekPajak;
    }

    public List<DatSubjekPajak> findAll() {
        String sql = "SELECT * FROM SYSTEM.DAT_SUBJEK_PAJAK ORDER BY SUBJEK_PAJAK_ID";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak);
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM SYSTEM.DAT_SUBJEK_PAJAK";
        return oracleJdbcTemplate.queryForObject(sql, Integer.class);
    }

    public Optional<DatSubjekPajak> findById(String subjekPajakId) {
        String sql = "SELECT * FROM SYSTEM.DAT_SUBJEK_PAJAK WHERE SUBJEK_PAJAK_ID = ?";
        List<DatSubjekPajak> result = oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak, subjekPajakId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<DatSubjekPajak> findByNpwp(String npwp) {
        String sql = "SELECT * FROM SYSTEM.DAT_SUBJEK_PAJAK WHERE NPWP = ? ORDER BY SUBJEK_PAJAK_ID";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak, npwp);
    }

    public List<DatSubjekPajak> findByNpwpd(String npwpd) {
        String sql = "SELECT * FROM SYSTEM.DAT_SUBJEK_PAJAK WHERE NPWPD = ? ORDER BY SUBJEK_PAJAK_ID";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak, npwpd);
    }

    public List<DatSubjekPajak> findByNmWpContaining(String nmWp) {
        String sql = "SELECT * FROM SYSTEM.DAT_SUBJEK_PAJAK WHERE UPPER(NM_WP) LIKE UPPER(?) ORDER BY NM_WP";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak, "%" + nmWp + "%");
    }

    public List<DatSubjekPajak> findByEmail(String email) {
        String sql = "SELECT * FROM SYSTEM.DAT_SUBJEK_PAJAK WHERE EMAIL = ? ORDER BY SUBJEK_PAJAK_ID";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak, email);
    }

    public List<DatSubjekPajak> findByKotaWp(String kotaWp) {
        String sql = "SELECT * FROM SYSTEM.DAT_SUBJEK_PAJAK WHERE UPPER(KOTA_WP) LIKE UPPER(?) ORDER BY SUBJEK_PAJAK_ID";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak, "%" + kotaWp + "%");
    }

    public List<DatSubjekPajak> findByKelurahanWp(String kelurahanWp) {
        String sql = "SELECT * FROM SYSTEM.DAT_SUBJEK_PAJAK WHERE UPPER(KELURAHAN_WP) LIKE UPPER(?) ORDER BY SUBJEK_PAJAK_ID";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak, "%" + kelurahanWp + "%");
    }

    public List<DatSubjekPajak> findPaginated(int offset, int pageSize) {
        String sql = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (SELECT * FROM SYSTEM.DAT_SUBJEK_PAJAK ORDER BY SUBJEK_PAJAK_ID) a WHERE ROWNUM <= ?) WHERE rnum > ?";
        return oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak, offset + pageSize, offset);
    }

    /**
     * Find by ID with reference data (kecamatan, kelurahan, propinsi, dati2)
     */
    public Optional<DatSubjekPajak> findByIdWithReferences(String subjekPajakId) {
        String sql = """
            SELECT sp.*, 
                   kec.NM_KECAMATAN,
                   kel.NM_KELURAHAN,
                   prop.NM_PROPINSI,
                   dati2.NM_DATI2
            FROM SYSTEM.DAT_SUBJEK_PAJAK sp
            LEFT JOIN SYSTEM.REF_KECAMATAN kec ON sp.KELURAHAN_WP = kec.NM_KECAMATAN
            LEFT JOIN SYSTEM.REF_KELURAHAN kel ON sp.KELURAHAN_WP = kel.NM_KELURAHAN
            LEFT JOIN SYSTEM.REF_PROPINSI prop ON sp.KOTA_WP = prop.NM_PROPINSI
            LEFT JOIN SYSTEM.REF_DATI2 dati2 ON sp.KOTA_WP = dati2.NM_DATI2
            WHERE sp.SUBJEK_PAJAK_ID = ?
            """;
        
        List<DatSubjekPajak> result = oracleJdbcTemplate.query(sql, this::mapRowToDatSubjekPajak, subjekPajakId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
