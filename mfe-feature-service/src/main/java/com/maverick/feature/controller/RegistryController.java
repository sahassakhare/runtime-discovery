package com.maverick.feature.controller;

import com.maverick.feature.domain.RuntimeInstance;
import com.maverick.feature.dto.RegisterInstanceRequest;
import com.maverick.feature.repository.RuntimeInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/registry")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RegistryController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegistryController.class);

    private final RuntimeInstanceRepository repository;

    @PostMapping("/instances")
    public ResponseEntity<RuntimeInstance> registerInstance(@RequestBody RegisterInstanceRequest request) {
        log.info("Registering instance: {} at {}", request.getAppName(), request.getUrl());

        com.maverick.feature.domain.Environment env = com.maverick.feature.domain.Environment.PRODUCTION;
        try {
            if (request.getEnvironment() != null) {
                env = com.maverick.feature.domain.Environment.valueOf(request.getEnvironment().toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid environment '{}', defaulting to PRODUCTION", request.getEnvironment());
        }

        RuntimeInstance instance = repository.findByAppNameAndEnvironmentAndUrl(
                request.getAppName(), env, request.getUrl())
                .orElse(RuntimeInstance.builder()
                        .appName(request.getAppName())
                        .environment(env)
                        .url(request.getUrl())
                        .build());

        // Update heartbeat/timestamp
        instance.setLastHeartbeat(LocalDateTime.now());

        RuntimeInstance saved = repository.save(instance);
        return ResponseEntity.ok(saved);
    }
}
