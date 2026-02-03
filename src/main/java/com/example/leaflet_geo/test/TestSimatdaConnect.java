package com.example.leaflet_geo.test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;

public class TestSimatdaConnect {
    public static void main(String[] args) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://192.178.10.112:3306/simpatda_lumajang?useSSL=false&serverTimezone=UTC");
        dataSource.setUsername("polinema");
        dataSource.setPassword("P0l1n3m4@bprd");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String[] testIds = {"4607", "7923", "2744", "4544"};
        
        System.out.println("Testing connection and query...");

        for (String id : testIds) {
            String sql = "SELECT COUNT(*) FROM t_transaksi WHERE t_idwpobjek = ?";
            try {
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
                System.out.println("ID: " + id + " -> Transactions: " + count);
                
                if (count > 0) {
                     String detailSql = "SELECT YEAR(t_tglpembayaran) as thn, SUM(t_jmlhpembayaran) as total FROM t_transaksi WHERE t_idwpobjek = ? GROUP BY YEAR(t_tglpembayaran)";
                     List<Map<String, Object>> rows = jdbcTemplate.queryForList(detailSql, id);
                     for(Map<String, Object> r : rows) {
                         System.out.println("   Year: " + r.get("thn") + ", Total: " + r.get("total"));
                     }
                }
            } catch (Exception e) {
                System.err.println("Error for ID " + id + ": " + e.getMessage());
            }
        }
        
        // Test with raw query to check content of t_transaksi locally
        String sampleLimit = "SELECT t_idwpobjek, t_idobjek FROM view_wpobjek LIMIT 5";
        // Note: t_transaksi uses t_idwpobjek which refers to t_idobjek in view_wpobjek? 
        // In seeder: FROM t_transaksi a LEFT JOIN view_wpobjek b ON a.t_idwpobjek = b.t_idobjek
        // So a.t_idwpobjek IS the key.
    }
}
