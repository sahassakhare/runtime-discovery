package com.maverick.feature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@org.springframework.scheduling.annotation.EnableScheduling
@org.springframework.boot.context.properties.EnableConfigurationProperties(com.maverick.feature.config.MfeProperties.class)
public class MfeFeatureServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MfeFeatureServiceApplication.class, args);
	}

}
