package com.example.leaflet_geo.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CustomNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        // Keep the original table name as is, including schema prefix
        return name;
    }
}

