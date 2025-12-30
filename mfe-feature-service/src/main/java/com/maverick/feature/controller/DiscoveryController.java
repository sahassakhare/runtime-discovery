package com.maverick.feature.controller;

import com.maverick.feature.domain.Microfrontend;
import com.maverick.feature.domain.Version;
import com.maverick.feature.dto.ResolutionRequest;
import com.maverick.feature.dto.ResolutionResponse;
import com.maverick.feature.repository.MicrofrontendRepository;
import com.maverick.feature.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ff4j.FF4j;
import io.openfeature.sdk.MutableContext;
import io.openfeature.sdk.OpenFeatureAPI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DiscoveryController {

    private final MicrofrontendRepository mfeRepository;
    private final com.maverick.feature.repository.DeploymentRepository deploymentRepository; // Injected
    private final FF4j ff4j;
    private final OpenFeatureAPI openFeatureAPI;

    private final List<ClientEmitter> emitters = new CopyOnWriteArrayList<>();

    // Simple state tracking to detect changes
    private String lastStateSignature = "";

    @lombok.Data
    @lombok.AllArgsConstructor
    class ClientEmitter {
        private SseEmitter emitter;
        private String appName;
        private String env;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        System.out
                .println(">>> INJECTED FF4j Bean into DiscoveryController: " + System.identityHashCode(ff4j) + " <<<");
    }

    @GetMapping("/stream")
    public SseEmitter streamUpdates(
            @RequestParam String appName,
            @RequestParam String env) {

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        ClientEmitter client = new ClientEmitter(emitter, appName, env);
        this.emitters.add(client);

        emitter.onCompletion(() -> this.emitters.remove(client));
        emitter.onTimeout(() -> this.emitters.remove(client));

        try {
            emitter.send(SseEmitter.event().name("connected").data("CONNECTED"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Scheduled(fixedRate = 2000)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void detectStateChange() {
        // High-performance dirty check on active deployments
        StringBuilder signature = new StringBuilder();

        // Check ALL active deployments to detect state changes globally
        // In a real optimized system, we could query max(updated_at)
        List<com.maverick.feature.domain.Deployment> activeDeployments = deploymentRepository.findAll();

        activeDeployments.stream().filter(com.maverick.feature.domain.Deployment::isActive).forEach(d -> {
            signature.append(d.getId())
                    .append(d.getEnvironment())
                    .append(d.isActive());
        });

        // Check ALL feature flags to support generic hot-swapping
        try {
            // DEBUG: Verify Store Type
            // log.info("FeatureStore Type: " +
            // ff4j.getFeatureStore().getClass().getName());

            Map<String, org.ff4j.core.Feature> features = ff4j.getFeatureStore().readAll();
            features.forEach((uid, f) -> {
                signature.append(uid).append("=").append(f.isEnable()).append("|");
            });
            log.info("DEBUG STATE SIGNATURE: " + signature.toString());
        } catch (Exception e) {
            log.trace("Feature store check failed silently", e);
        }

        String newState = signature.toString();
        if (!newState.equals(lastStateSignature)) {
            log.info("State Change Detected! Old: '{}' -> New: '{}'", lastStateSignature, newState);
            if (!lastStateSignature.isEmpty()) {
                log.info("Configuration state change detected. Broadcasting update.");
                broadcast("CONFIG_CHANGED", "CONFIG_CHANGED");
            }
            lastStateSignature = newState;
        }
    }

    private void broadcast(String type, String payload) {
        List<ClientEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        this.emitters.forEach(client -> {
            try {
                // Enterprise Filtering: Broadcast to everyone for now as detecting exactly WHAT
                // changed
                // for WHICH tenant/env from a simple hash signature is complex.
                // Optimally, we would pass the "Changed Context" to this method and filter:
                // if (client.getEnv().equals(changedEnv)) ...

                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .name("message")
                        .data(payload);
                client.getEmitter().send(event);
            } catch (IOException e) {
                deadEmitters.add(client);
            }
        });
        this.emitters.removeAll(deadEmitters);
    }

    @PostMapping("/resolve")
    public ResponseEntity<ResolutionResponse> resolve(@RequestBody ResolutionRequest request) {
        String remoteName = request.getRemoteName();
        // Use provided env or default to "production"
        String env = (request.getEnvironment() != null) ? request.getEnvironment() : "production";
        String tenantId = request.getTenantId();

        log.info("Resolving remote: {} for env: {} tenant: {}", remoteName, env, tenantId);

        // 1. Enterprise Resolution Strategy
        // Priority 1: Specific Tenant Deployment
        Optional<com.maverick.feature.domain.Deployment> deploymentOpt = Optional.empty();

        if (tenantId != null) {
            deploymentOpt = deploymentRepository.findActiveByTenant(remoteName, env, tenantId);
        }

        // Priority 2: Global Deployment (Fallthrough)
        if (deploymentOpt.isEmpty()) {
            deploymentOpt = deploymentRepository.findActiveGlobal(remoteName, env);
        }

        if (deploymentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        com.maverick.feature.domain.Deployment deployment = deploymentOpt.get();
        Version selectedVersion = deployment.getVersion();

        // 2. Feature Flags (OpenFeature)
        Map<String, Object> flags = new HashMap<>();
        MutableContext evaluationContext = new MutableContext();
        if (request.getContext() != null) {
            request.getContext().forEach((k, v) -> evaluationContext.add(k, String.valueOf(v)));
        }
        evaluationContext.add("environment", env);
        if (tenantId != null)
            evaluationContext.add("tenantId", tenantId);

        Microfrontend mfe = selectedVersion.getMicrofrontend();
        if (mfe.getFeatureGroupName() != null) {
            try {
                Map<String, org.ff4j.core.Feature> groupFeatures = ff4j.getFeatureStore()
                        .readGroup(mfe.getFeatureGroupName());
                for (org.ff4j.core.Feature f : groupFeatures.values()) {
                    boolean isEnabled = openFeatureAPI.getClient().getBooleanValue(f.getUid(), false,
                            evaluationContext);
                    flags.put(f.getUid(), isEnabled);
                }
            } catch (Exception e) {
                log.warn("Failed to resolve features for group '{}': {}", mfe.getFeatureGroupName(), e.getMessage());
            }
        }

        // 3. Build Response
        ResolutionResponse.ResolutionResponseBuilder responseBuilder = ResolutionResponse.builder()
                .remoteName(remoteName)
                .selected(toRemoteVersion(selectedVersion))
                .cacheTtlSeconds(60);

        ResolutionResponse.ResolutionContext.ResolutionContextBuilder contextBuilder = ResolutionResponse.ResolutionContext
                .builder()
                .flags(flags);

        responseBuilder.resolutionContext(contextBuilder.build());
        return ResponseEntity.ok(responseBuilder.build());
    }

    private ResolutionResponse.RemoteVersion toRemoteVersion(Version version) {
        return ResolutionResponse.RemoteVersion.builder()
                .version(version.getVersion())
                .remoteEntry(version.getRemoteEntry())
                .integrity(version.getIntegrity())
                .build();
    }
}
