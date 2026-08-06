package com.example.leaflet_geo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MultipleDatabaseConfig {

    // ============ PostgreSQL Primary (sig) ============
    @Value("${spring.datasource.url}")
    private String postgresUrl;

    @Value("${spring.datasource.username}")
    private String postgresUsername;

    @Value("${spring.datasource.password}")
    private String postgresPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String postgresDriver;

    // ============ MySQL SIMATDA ============
    @Value("${spring.datasource.mysql.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.mysql.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.mysql.password}")
    private String mysqlPassword;

    @Value("${spring.datasource.mysql.driver-class-name}")
    private String mysqlDriver;

    // ============ PostgreSQL BPHTB ============
    @Value("${spring.datasource.bphtb.url}")
    private String bphtbUrl;

    @Value("${spring.datasource.bphtb.username}")
    private String bphtbUsername;

    @Value("${spring.datasource.bphtb.password}")
    private String bphtbPassword;

    @Value("${spring.datasource.bphtb.driver-class-name}")
    private String bphtbDriver;

    // ============ Oracle SISMIOP ============
    @Value("${spring.datasource.sismiop.url}")
    private String sismiopUrl;

    @Value("${spring.datasource.sismiop.username}")
    private String sismiopUsername;

    @Value("${spring.datasource.sismiop.password}")
    private String sismiopPassword;

    @Value("${spring.datasource.sismiop.driver-class-name}")
    private String sismiopDriver;

    // ============ PostgreSQL E-PASIR ============
    @Value("${spring.datasource.epasir.url}")
    private String epasirUrl;

    @Value("${spring.datasource.epasir.username}")
    private String epasirUsername;

    @Value("${spring.datasource.epasir.password}")
    private String epasirPassword;

    @Value("${spring.datasource.epasir.driver-class-name}")
    private String epasirDriver;

    // ============ PostgreSQL PBJT Assessment ============
    @Value("${spring.datasource.pbjt.url}")
    private String pbjtUrl;

    @Value("${spring.datasource.pbjt.username}")
    private String pbjtUsername;

    @Value("${spring.datasource.pbjt.password}")
    private String pbjtPassword;

    @Value("${spring.datasource.pbjt.driver-class-name}")
    private String pbjtDriver;

    // ============ DataSource Beans ============

    @Primary
    @Bean(name = "postgresDataSource")
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create()
                .url(postgresUrl)
                .username(postgresUsername)
                .password(postgresPassword)
                .driverClassName(postgresDriver)
                .build();
    }

    @Bean(name = "oracleDataSource")
    public DataSource oracleDataSource() {
        return DataSourceBuilder.create()
                .url(sismiopUrl)
                .username(sismiopUsername)
                .password(sismiopPassword)
                .driverClassName(sismiopDriver)
                .build();
    }

    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create()
                .url(mysqlUrl)
                .username(mysqlUsername)
                .password(mysqlPassword)
                .driverClassName(mysqlDriver)
                .build();
    }

    @Bean(name = "bphtbDataSource")
    public DataSource bphtbDataSource() {
        return DataSourceBuilder.create()
                .url(bphtbUrl)
                .username(bphtbUsername)
                .password(bphtbPassword)
                .driverClassName(bphtbDriver)
                .build();
    }

    @Bean(name = "sismiopDataSource")
    public DataSource sismiopDataSource() {
        return DataSourceBuilder.create()
                .url(sismiopUrl)
                .username(sismiopUsername)
                .password(sismiopPassword)
                .driverClassName(sismiopDriver)
                .build();
    }

    @Bean(name = "epasirDataSource")
    public DataSource epasirDataSource() {
        return DataSourceBuilder.create()
                .url(epasirUrl)
                .username(epasirUsername)
                .password(epasirPassword)
                .driverClassName(epasirDriver)
                .build();
    }

    @Bean(name = "pbjtDataSource")
    public DataSource pbjtDataSource() {
        return DataSourceBuilder.create()
                .url(pbjtUrl)
                .username(pbjtUsername)
                .password(pbjtPassword)
                .driverClassName(pbjtDriver)
                .build();
    }

    // ============ JdbcTemplate Beans ============

    @Primary
    @Bean(name = "postgresJdbcTemplate")
    public JdbcTemplate postgresJdbcTemplate(@Qualifier("postgresDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "oracleJdbcTemplate")
    public JdbcTemplate oracleJdbcTemplate(@Qualifier("oracleDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "bphtbJdbcTemplate")
    public JdbcTemplate bphtbJdbcTemplate(@Qualifier("bphtbDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "sismiopJdbcTemplate")
    public JdbcTemplate sismiopJdbcTemplate(@Qualifier("sismiopDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "epasirJdbcTemplate")
    public JdbcTemplate epasirJdbcTemplate(@Qualifier("epasirDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "pbjtJdbcTemplate")
    public JdbcTemplate pbjtJdbcTemplate(@Qualifier("pbjtDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
