package com.maverick.feature.service.impl;

import com.maverick.feature.domain.MfeConsumedRemote;
import com.maverick.feature.domain.RuntimeInstance;
import com.maverick.feature.dto.RegisterInstanceRequest;
import com.maverick.feature.repository.DeploymentRepository;
import com.maverick.feature.repository.MfeConsumedRemoteRepository;
import com.maverick.feature.repository.RuntimeInstanceRepository;
import com.maverick.feature.service.RegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistryServiceImpl implements RegistryService {

    private final RuntimeInstanceRepository runtimeRepository;
    private final DeploymentRepository deploymentRepository;
    private final MfeConsumedRemoteRepository consumedRemoteRepository;

    @Override
    @Transactional
    public RuntimeInstance registerInstance(RegisterInstanceRequest request) {
        log.info("Registering instance: {} at {}", request.getAppName(), request.getUrl());

        com.maverick.feature.domain.Environment env = com.maverick.feature.domain.Environment.PRODUCTION;
        try {
            if (request.getEnvironment() != null) {
                env = com.maverick.feature.domain.Environment.valueOf(request.getEnvironment().toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid environment '{}', defaulting to PRODUCTION", request.getEnvironment());
        }

        RuntimeInstance instance = runtimeRepository.findByAppNameAndEnvironmentAndUrl(
                request.getAppName(), env, request.getUrl())
                .orElse(RuntimeInstance.builder()
                        .appName(request.getAppName())
                        .environment(env)
                        .url(request.getUrl())
                        .build());

        // Update heartbeat/timestamp
        instance.setLastHeartbeat(LocalDateTime.now());

        return runtimeRepository.save(instance);
    }

    @Override
    @Transactional
    public String reportConsumption(java.util.Map<String, Object> report) {
        String consumer = (String) report.get("consumer");
        String remote = (String) report.get("remote");
        String modules = (String) report.get("modules");
        String env = (String) report.get("environment");

        log.info("Runtime Consumption Report: {} consuming {} (modules: {}) in {}", consumer, remote, modules, env);

        // Find active version for consumer
        Optional<com.maverick.feature.domain.Deployment> dep = deploymentRepository
                .findActiveGlobal(consumer, com.maverick.feature.domain.Environment.PRODUCTION);

        if (dep.isPresent()) {
            // Check for existing record to verify idempotency
            Optional<MfeConsumedRemote> existing = consumedRemoteRepository
                    .findByConsumerVersionIdAndRemoteName(dep.get().getVersion().getId(), remote);

            if (existing.isPresent()) {
                return "Already Reported";
            }

            MfeConsumedRemote consumed = new MfeConsumedRemote();
            consumed.setConsumerVersion(dep.get().getVersion());
            consumed.setRemoteName(remote);
            consumed.setUsedModules(modules);
            consumed.setDynamic(true);
            consumed.setEnvironment(env);
            consumedRemoteRepository.save(consumed);
            return "Reported";
        }

        log.warn("Telemetry ignored: No active deployment found for consumer '{}' in env '{}'", consumer,
                com.maverick.feature.domain.Environment.PRODUCTION);
        return "Ignored - Consumer Unknown";
    }
}
