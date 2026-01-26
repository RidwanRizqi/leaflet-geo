package com.example.leaflet_geo.repository;

import com.example.leaflet_geo.model.RefKecamatan;
import com.example.leaflet_geo.entity.RefKecamatan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RefKecamatanRepository {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    private static final RowMapper<RefKecamatan> ROW_MAPPER = new RowMapper<RefKecamatan>() {
        @Override
        public RefKecamatan mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new RefKecamatan(
                rs.getString("KD_PROPINSI"),
                rs.getString("KD_DATI2"),
                rs.getString("KD_KECAMATAN"),
                rs.getString("NM_KECAMATAN")
            );
        }
    };

    public List<RefKecamatan> findAll() {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, NM_KECAMATAN FROM SYSTEM.REF_KECAMATAN ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<RefKecamatan> findByKdPropinsi(String kdPropinsi) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, NM_KECAMATAN FROM SYSTEM.REF_KECAMATAN WHERE KD_PROPINSI = ? ORDER BY KD_DATI2, KD_KECAMATAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi);
    }

    public List<RefKecamatan> findByKdPropinsiAndKdDati2(String kdPropinsi, String kdDati2) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, NM_KECAMATAN FROM SYSTEM.REF_KECAMATAN WHERE KD_PROPINSI = ? AND KD_DATI2 = ? ORDER BY KD_KECAMATAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi, kdDati2);
    }

    public RefKecamatan findByPrimaryKey(String kdPropinsi, String kdDati2, String kdKecamatan) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, NM_KECAMATAN FROM SYSTEM.REF_KECAMATAN WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND KD_KECAMATAN = ?";
        List<RefKecamatan> results = oracleJdbcTemplate.query(sql, ROW_MAPPER, kdPropinsi, kdDati2, kdKecamatan);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<RefKecamatan> findByNmKecamatanContaining(String namaKecamatan) {
        String sql = "SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, NM_KECAMATAN FROM SYSTEM.REF_KECAMATAN WHERE UPPER(NM_KECAMATAN) LIKE UPPER(?) ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, "%" + namaKecamatan + "%");
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM SYSTEM.REF_KECAMATAN";
        return oracleJdbcTemplate.queryForObject(sql, Long.class);
    }

    public List<RefKecamatan> findAllWithPagination(int offset, int limit) {
        String sql = "SELECT * FROM (SELECT ROWNUM rn, KD_PROPINSI, KD_DATI2, KD_KECAMATAN, NM_KECAMATAN FROM SYSTEM.REF_KECAMATAN ORDER BY KD_PROPINSI, KD_DATI2, KD_KECAMATAN) WHERE rn > ? AND rn <= ?";
        return oracleJdbcTemplate.query(sql, ROW_MAPPER, offset, offset + limit);
    }
}
