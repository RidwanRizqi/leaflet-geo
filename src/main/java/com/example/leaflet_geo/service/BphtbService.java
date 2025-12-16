package com.example.leaflet_geo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BphtbService {

    private final JdbcTemplate bphtbJdbcTemplate;

    public BphtbService(@Qualifier("bphtbJdbcTemplate") JdbcTemplate bphtbJdbcTemplate) {
        this.bphtbJdbcTemplate = bphtbJdbcTemplate;
    }

    /**
     * Test koneksi ke database BPHTB
     */
    public String testConnection() {
        try {
            String result = bphtbJdbcTemplate.queryForObject("SELECT version()", String.class);
            return "BPHTB Connection Success! PostgreSQL Version: " + result;
        } catch (Exception e) {
            return "BPHTB Connection Failed: " + e.getMessage();
        }
    }

    /**
     * Get list of all tables in public schema
     */
    public List<Map<String, Object>> getAllTables() {
        String sql = """
            SELECT 
                table_name,
                (SELECT COUNT(*) FROM information_schema.columns 
                 WHERE table_schema = t.table_schema AND table_name = t.table_name) as column_count
            FROM information_schema.tables t
            WHERE table_schema = 'public' 
            AND table_type = 'BASE TABLE'
            ORDER BY table_name
            """;
        return bphtbJdbcTemplate.queryForList(sql);
    }

    /**
     * Get table structure (columns) for a specific table
     */
    public List<Map<String, Object>> getTableStructure(String tableName) {
        String sql = """
            SELECT 
                column_name,
                data_type,
                character_maximum_length,
                is_nullable,
                column_default
            FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = ?
            ORDER BY ordinal_position
            """;
        return bphtbJdbcTemplate.queryForList(sql, tableName);
    }

    /**
     * Get row count for a specific table
     */
    public Long getTableRowCount(String tableName) {
        String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
        return bphtbJdbcTemplate.queryForObject(sql, Long.class);
    }

    /**
     * Get sample data from a table
     */
    public List<Map<String, Object>> getSampleData(String tableName, int limit) {
        String sql = String.format("SELECT * FROM %s LIMIT %d", tableName, limit);
        return bphtbJdbcTemplate.queryForList(sql);
    }

    /**
     * Get realisasi BPHTB per bulan untuk tahun tertentu
     */
    public List<Map<String, Object>> getRealisasiBulanan(Integer tahun) {
        String sql = """
            SELECT 
                EXTRACT(MONTH FROM t_tglpembayaran_pokok) AS bulan,
                COALESCE(SUM(t_jmlhbayar_total), 0) AS realisasi
            FROM t_pembayaran_bphtb
            WHERE EXTRACT(YEAR FROM t_tglpembayaran_pokok) = ?
            GROUP BY EXTRACT(MONTH FROM t_tglpembayaran_pokok)
            ORDER BY bulan
            """;
        return bphtbJdbcTemplate.queryForList(sql, tahun);
    }

    /**
     * Get total realisasi BPHTB untuk tahun tertentu
     */
    public Long getRealisasiTahunan(Integer tahun) {
        String sql = """
            SELECT COALESCE(SUM(t_jmlhbayar_total), 0) AS total_realisasi
            FROM t_pembayaran_bphtb
            WHERE EXTRACT(YEAR FROM t_tglpembayaran_pokok) = ?
            """;
        Map<String, Object> result = bphtbJdbcTemplate.queryForMap(sql, tahun);
        return ((Number) result.get("total_realisasi")).longValue();
    }

    /**
     * Get target BPHTB untuk tahun tertentu dari table s_target_bphtb
     */
    public Long getTargetTahunan(Integer tahun) {
        try {
            String sql = """
                SELECT COALESCE(s_nilai_target, 0) AS target
                FROM s_target_bphtb
                WHERE s_tahun_target = ?
                AND s_id_target_status = 1
                ORDER BY created_at DESC
                LIMIT 1
                """;
            Map<String, Object> result = bphtbJdbcTemplate.queryForMap(sql, tahun);
            return ((Number) result.get("target")).longValue();
        } catch (Exception e) {
            // Jika tidak ada data target di database, return 0 atau default value
            System.err.println("Warning: No BPHTB target found for year " + tahun);
            return 15_000_000_000L; // Default fallback 15 Miliar
        }
    }
}
