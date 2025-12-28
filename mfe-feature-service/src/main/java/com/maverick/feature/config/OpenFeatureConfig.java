package com.maverick.feature.config;

import io.openfeature.sdk.OpenFeatureAPI;
import org.ff4j.FF4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenFeatureConfig {

    @Bean
    public OpenFeatureAPI openFeatureAPI(FF4j ff4j) {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new FF4jOpenFeatureProvider(ff4j));
        return api;
    }
}
