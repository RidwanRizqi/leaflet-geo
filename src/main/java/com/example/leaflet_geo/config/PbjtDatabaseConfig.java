package com.example.leaflet_geo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.leaflet_geo.repository.pbjt",
    entityManagerFactoryRef = "pbjtEntityManagerFactory",
    transactionManagerRef = "pbjtTransactionManager"
)
public class PbjtDatabaseConfig {

    @Bean(name = "pbjtEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean pbjtEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("pbjtDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", true);
        
        return builder
                .dataSource(dataSource)
                .packages("com.example.leaflet_geo.entity")
                .persistenceUnit("pbjt")
                .properties(properties)
                .build();
    }

    @Bean(name = "pbjtTransactionManager")
    public PlatformTransactionManager pbjtTransactionManager(
            @Qualifier("pbjtEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
