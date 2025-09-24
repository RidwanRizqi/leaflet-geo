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
}
