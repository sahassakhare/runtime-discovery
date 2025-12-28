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
@Slf4j
public class RegistryController {

    private final RuntimeInstanceRepository repository;

    @PostMapping("/instances")
    public ResponseEntity<RuntimeInstance> registerInstance(@RequestBody RegisterInstanceRequest request) {
        log.info("Registering instance: {} at {}", request.getAppName(), request.getUrl());

        RuntimeInstance instance = repository.findByAppNameAndEnvironmentAndUrl(
                request.getAppName(), request.getEnvironment(), request.getUrl())
                .orElse(RuntimeInstance.builder()
                        .appName(request.getAppName())
                        .environment(request.getEnvironment())
                        .url(request.getUrl())
                        .build());

        // Update heartbeat/timestamp
        instance.setLastHeartbeat(LocalDateTime.now());

        RuntimeInstance saved = repository.save(instance);
        return ResponseEntity.ok(saved);
    }
}
