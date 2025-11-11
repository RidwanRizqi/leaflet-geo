package com.example.leaflet_geo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SimatdaService {

    private final JdbcTemplate mysqlJdbcTemplate;

    public SimatdaService(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }

    /**
     * Contoh method untuk mengambil data dari database SIMATDA
     * Sesuaikan dengan struktur tabel yang ada di simpatda_lumajang
     */
    public List<Map<String, Object>> getAllData(String tableName) {
        String sql = "SELECT * FROM " + tableName;
        return mysqlJdbcTemplate.queryForList(sql);
    }

    /**
     * Contoh method untuk test koneksi
     */
    public boolean testConnection() {
        try {
            mysqlJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method untuk mengambil data dengan query custom
     */
    public List<Map<String, Object>> executeQuery(String query) {
        return mysqlJdbcTemplate.queryForList(query);
    }
}
