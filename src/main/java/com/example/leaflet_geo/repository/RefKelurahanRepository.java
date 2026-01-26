package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.model.RefKelurahan;
import com.example.leaflet_geo.entity.RefKelurahan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RefKelurahanRepository {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private static final RowMapper<RefKelurahan> ROW_MAPPER = new RowMapper<RefKelurahan>() {
        @Override
        public RefKelurahan mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new RefKelurahan(
                rs.getString("KD_PROPINSI"),
                rs.getString("KD_DATI2"),
                rs.getString("KD_KECAMATAN"),
                rs.getString("KD_KELURAHAN"),
                rs.getString("KD_SEKTOR"),
                rs.getString("NM_KELURAHAN"),
                rs.getObject("NO_KELURAHAN", Integer.class),
                rs.getString("KD_POS_KELURAHAN")
            );
        }
    };

    public List<RefKelurahan> findAll() {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<RefKelurahan> findByKdPropinsi(String kdPropinsi) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ? ORDER BY KD_DATI2, KD_KECAMATAN, KD_KELURAHAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi);
    }

    public List<RefKelurahan> findByKdPropinsiAndKdDati2(String kdPropinsi, String kdDati2) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ? AND KD_DATI2 = ? ORDER BY KD_KECAMATAN, KD_KELURAHAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi, kdDati2);
    }

    public List<RefKelurahan> findByKdPropinsiAndKdDati2AndKdKecamatan(String kdPropinsi, String kdDati2, String kdKecamatan) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? ORDER BY KD_KELURAHAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi, kdDati2, kdKecamatan);
    }

    public RefKelurahan findByPrimaryKey(String kdPropinsi, String kdDati2, String kdKecamatan, String kdKelurahan) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? AND KD_KELURAHAN = ?";
        List<RefKelurahan> results = oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi, kdDati2, kdKecamatan, kdKelurahan);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<RefKelurahan> findByNmKelurahanContaining(String namaKelurahan) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE UPPER(NM_KELURAHAN) LIKE UPPER(?) ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, "%" + namaKelurahan + "%");
    }

    public List<RefKelurahan> findByKdSektor(String kdSektor) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE KD_SEKTOR = ? ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdSektor);
    }

    public List<RefKelurahan> findByKdPosKelurahan(String kdPosKelurahan) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE KD_POS_KELURAHAN = ? ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPosKelurahan);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM SYSTEM.REF_KELURAHAN";
        return oracleJdbcTemplate.queryForObject(sql, Long.class);
    }

    public long countByKdPropinsi(String kdPropinsi) {
        String sql = "SELECT COUNT(*) FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ?";
        return oracleJdbcTemplate.queryForObject(sql, Long.class, kdPropinsi);
    }

    public long countByKdPropinsiAndKdDati2(String kdPropinsi, String kdDati2) {
        String sql = "SELECT COUNT(*) FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ? AND KD_DATI2 = ?";
        return oracleJdbcTemplate.queryForObject(sql, Long.class, kdPropinsi, kdDati2);
    }

    public long countByKdPropinsiAndKdDati2AndKdKecamatan(String kdPropinsi, String kdDati2, String kdKecamatan) {
        String sql = "SELECT COUNT(*) FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ?";
        return oracleJdbcTemplate.queryForObject(sql, Long.class, kdPropinsi, kdDati2, kdKecamatan);
    }

    public List<RefKelurahan> findAllWithPagination(int offset, int limit) {
        String sql = "SELECT * FROM (SELECT ROWNUM rn, KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN) WHERE rn > ? AND rn <= ?";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, offset, offset + limit);
    }

    public List<RefKelurahan> findByKdPropinsiWithPagination(String kdPropinsi, int offset, int limit) {
        String sql = "SELECT * FROM (SELECT ROWNUM rn, KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ? ORDER BY KD_DATI2, KD_KECAMATAN, KD_KELURAHAN) WHERE rn > ? AND rn <= ?";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi, offset, offset + limit);
    }

    public List<RefKelurahan> findByKdPropinsiAndKdDati2WithPagination(String kdPropinsi, String kdDati2, int offset, int limit) {
        String sql = "SELECT * FROM (SELECT ROWNUM rn, KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ? AND KD_DATI2 = ? ORDER BY KD_KECAMATAN, KD_KELURAHAN) WHERE rn > ? AND rn <= ?";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi, kdDati2, offset, offset + limit);
    }

    public List<RefKelurahan> findByKdPropinsiAndKdDati2AndKdKecamatanWithPagination(String kdPropinsi, String kdDati2, String kdKecamatan, int offset, int limit) {
        String sql = "SELECT * FROM (SELECT ROWNUM rn, KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_SEKTOR, NM_KELURAHAN, NO_KELURAHAN, KD_POS_KELURAHAN FROM SYSTEM.REF_KELURAHAN WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ? ORDER BY KD_KELURAHAN) WHERE rn > ? AND rn <= ?";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi, kdDati2, kdKecamatan, offset, offset + limit);
    }
}
