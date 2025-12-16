package com.example.leaflet_geo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MultipleDatabaseConfig {

    // PostgreSQL tetap sebagai primary untuk JPA
    @Primary
    @Bean(name = "postgresDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/sig")
                .username("postgres")
                .password("root")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    // Oracle sebagai secondary database
    @Bean(name = "oracleDataSource")
    public DataSource oracleDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:oracle:thin:@//localhost:1521/free")
                .username("system")
                .password("1234")
                .driverClassName("oracle.jdbc.OracleDriver")
                .build();
    }

    // MySQL SIMATDA sebagai database ketiga
    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://192.178.10.112:3306/simpatda_lumajang?useSSL=false&serverTimezone=UTC")
                .username("polinema")
                .password("P0l1n3m4@bprd")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
    
    // PostgreSQL BPHTB sebagai database keempat
    @Bean(name = "bphtbDataSource")
    public DataSource bphtbDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://192.178.10.114:5432/mybphtb")
                .username("postgres")
                .password("BprdLum4j4ng@2025")
                .driverClassName("org.postgresql.Driver")
                .build();
    }
    
    // Oracle SISMIOP sebagai database kelima
    @Bean(name = "sismiopDataSource")
    public DataSource sismiopDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:oracle:thin:@//192.178.10.101:1521/SISMIOP")
                .username("PBB")
                .password("PBB")
                .driverClassName("oracle.jdbc.OracleDriver")
                .build();
    }
    
    // PostgreSQL E-PASIR sebagai database keenam
    @Bean(name = "epasirDataSource")
    public DataSource epasirDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://192.178.10.225:5432/e-pasir-reborn")
                .username("epasir")
                .password("BPRD@2023#")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    // JdbcTemplate untuk PostgreSQL (primary)
    @Primary
    @Bean(name = "postgresJdbcTemplate")
    public JdbcTemplate postgresJdbcTemplate(@Qualifier("postgresDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // JdbcTemplate untuk Oracle (secondary)
    @Bean(name = "oracleJdbcTemplate")
    public JdbcTemplate oracleJdbcTemplate(@Qualifier("oracleDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // JdbcTemplate untuk MySQL SIMATDA
    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    // JdbcTemplate untuk PostgreSQL BPHTB
    @Bean(name = "bphtbJdbcTemplate")
    public JdbcTemplate bphtbJdbcTemplate(@Qualifier("bphtbDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    // JdbcTemplate untuk Oracle SISMIOP
    @Bean(name = "sismiopJdbcTemplate")
    public JdbcTemplate sismiopJdbcTemplate(@Qualifier("sismiopDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    // JdbcTemplate untuk PostgreSQL E-PASIR
    @Bean(name = "epasirJdbcTemplate")
    public JdbcTemplate epasirJdbcTemplate(@Qualifier("epasirDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
