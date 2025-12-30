package com.maverick.feature.config;

import org.ff4j.FF4j;
import org.ff4j.core.Feature;
import org.ff4j.strategy.PonderationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class FF4jConfig {

    @Bean
    @org.springframework.context.annotation.Primary
    public FF4j ff4j(javax.sql.DataSource dataSource) {
        FF4j ff4j = new FF4j();
        System.out.println(">>> INITIALIZING FF4j Bean: " + System.identityHashCode(ff4j) + " <<<");

        // Use JDBC Store for persistence
        ff4j.setFeatureStore(new org.ff4j.store.JdbcFeatureStore(dataSource));
        ff4j.setPropertiesStore(new org.ff4j.property.store.JdbcPropertyStore(dataSource));
        ff4j.setEventRepository(new org.ff4j.audit.repository.JdbcEventRepository(dataSource));

        // Enabling audit
        ff4j.audit(false);

        return ff4j;
    }
}
