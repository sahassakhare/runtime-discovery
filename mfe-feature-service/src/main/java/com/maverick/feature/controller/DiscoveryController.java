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
public class DiscoveryController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DiscoveryController.class);

    private final MicrofrontendRepository mfeRepository;
    private final com.maverick.feature.repository.DeploymentRepository deploymentRepository; // Injected
    private final FF4j ff4j;
    private final OpenFeatureAPI openFeatureAPI;
    private final com.maverick.feature.service.GovernanceService governanceService; // Injected
    private final com.maverick.feature.config.MfeProperties mfeProperties; // Injected Configuration

    private final List<ClientEmitter> emitters = new CopyOnWriteArrayList<>();

    // Simple state tracking to detect changes
    private String lastStateSignature = "";

    class ClientEmitter {
        private SseEmitter emitter;
        private String appName;
        private String env;

        public ClientEmitter(SseEmitter emitter, String appName, String env) {
            this.emitter = emitter;
            this.appName = appName;
            this.env = env;
        }

        public SseEmitter getEmitter() {
            return emitter;
        }

        public String getAppName() {
            return appName;
        }

        public String getEnv() {
            return env;
        }
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
        // Use provided env or default to "PRODUCTION"
        com.maverick.feature.domain.Environment env = com.maverick.feature.domain.Environment.PRODUCTION;
        if (request.getEnvironment() != null) {
            try {
                env = com.maverick.feature.domain.Environment.valueOf(request.getEnvironment().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid environment '{}' requested. Defaulting to PRODUCTION.", request.getEnvironment());
            }
        }

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

        log.info("TRACE-LOG: Initial Selection. Env={}, Tenant={}, SelectedVer={}", env, tenantId,
                selectedVersion.getVersion());

        // 1.1 Resolve Fallback (Safest: Global Production)
        // If we are already on Global Production, fallback is self (or null, but let's
        // provide self for safety)
        Optional<com.maverick.feature.domain.Deployment> fallbackOpt = deploymentRepository.findActiveGlobal(remoteName,
                com.maverick.feature.domain.Environment.PRODUCTION);
        Version fallbackVersion = fallbackOpt.map(com.maverick.feature.domain.Deployment::getVersion)
                .orElse(selectedVersion);

        log.info("TRACE-LOG: Fallback. Ver={}", fallbackVersion.getVersion());

        // 2. Feature Flags (OpenFeature + FF4j Metadata)
        Map<String, Object> flags = new HashMap<>();
        ResolutionResponse.Variant activeVariant = null;

        MutableContext evaluationContext = new MutableContext();
        if (request.getContext() != null) {
            request.getContext().forEach((k, v) -> evaluationContext.add(k, String.valueOf(v)));
        }
        evaluationContext.add(com.maverick.feature.domain.FeatureContextKeys.ENVIRONMENT, env.name());
        if (tenantId != null)
            evaluationContext.add(com.maverick.feature.domain.FeatureContextKeys.TENANT_ID, tenantId);

        Microfrontend mfe = selectedVersion.getMicrofrontend();
        if (mfe.getFeatureGroupName() != null) {
            try {
                Map<String, org.ff4j.core.Feature> groupFeatures = ff4j.getFeatureStore()
                        .readGroup(mfe.getFeatureGroupName());

                for (org.ff4j.core.Feature f : groupFeatures.values()) {
                    boolean isEnabled = openFeatureAPI.getClient().getBooleanValue(f.getUid(), false,
                            evaluationContext);
                    flags.put(f.getUid(), isEnabled);

                    // 2.1 Variant Resolution
                    // If feature is enabled and has "trackMapping", it drives the variant.
                    if (isEnabled && f.getCustomProperties() != null
                            && f.getCustomProperties().containsKey("trackMapping")) {
                        String variantName = f.getCustomProperties().get("trackMapping").asString();
                        try {
                            com.maverick.feature.domain.VariantType vType = com.maverick.feature.domain.VariantType
                                    .valueOf(variantName);
                            // Only set if not already set (first match wins, or priority logic)
                            if (activeVariant == null) {
                                activeVariant = ResolutionResponse.Variant.builder()
                                        .name(variantName.toLowerCase()) // e.g. "canary"
                                        .type(vType)
                                        .build();
                            }
                        } catch (Exception e) {
                            log.warn("Invalid variant type in flag {}: {}", f.getUid(), variantName);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to resolve features for group '{}': {}", mfe.getFeatureGroupName(), e.getMessage());
            }
        }

        // 2.2 Variant Swap Logic
        // Support for CANARY (Staging), EXPERIMENT (Development), and STANDARD (No
        // Swap)
        if (activeVariant != null) {
            com.maverick.feature.domain.Environment targetEnv = null;

            switch (activeVariant.getType()) {
                case CANARY:
                    targetEnv = com.maverick.feature.domain.Environment.STAGING;
                    break;
                case EXPERIMENT:
                    targetEnv = com.maverick.feature.domain.Environment.DEVELOPMENT;
                    break;
                case STANDARD:
                    // explicit "Standard" variant means "Stick to current Environment's default"
                    // No swap needed, but we keep the variant object to show "Standard" in response
                    targetEnv = null;
                    break;
            }

            if (targetEnv != null) {
                Optional<com.maverick.feature.domain.Deployment> variantDeploy = deploymentRepository
                        .findActiveGlobal(remoteName, targetEnv);
                if (variantDeploy.isPresent()) {
                    selectedVersion = variantDeploy.get().getVersion();
                    log.info("Variant Swapped: {} -> {} ({})", remoteName, activeVariant.getName(),
                            selectedVersion.getVersion());
                } else {
                    log.warn("Variant active ({}) but no deployment found for env {}", activeVariant.getName(),
                            targetEnv);
                }
            }
        } else {
            activeVariant = ResolutionResponse.Variant.builder()
                    .name("standard")
                    .type(com.maverick.feature.domain.VariantType.STANDARD)
                    .build();
        }

        // 2.3 GOVERNANCE CHECK (Battle-Tested Policy Engine)
        // Construct Policy Input with Rich Telemetry - Dynamic Extraction from
        // Configuration

        com.maverick.feature.config.MfeProperties.ContextConfig ctxConfig = mfeProperties.getContext();

        // 1. User Roles
        java.util.List<String> userRoles = extractRoles(
                request.getContext() != null ? request.getContext() : java.util.Collections.emptyMap(),
                ctxConfig.getKeys().getRoles());

        // Legacy support: add ADMIN if flag is set (optional)
        if (Boolean.TRUE.equals(flags.get("user.admin"))) {
            if (!userRoles.contains("ADMIN"))
                userRoles.add("ADMIN");
        }

        // 2. Department
        String department = extractString(
                request.getContext() != null ? request.getContext() : java.util.Collections.emptyMap(),
                ctxConfig.getKeys().getDepartment(), ctxConfig.getDefaults().getDepartment());

        // 3. Internal User
        boolean isInternalUser = extractBoolean(
                request.getContext() != null ? request.getContext() : java.util.Collections.emptyMap(),
                ctxConfig.getKeys().getInternal(), ctxConfig.getDefaults().isInternal());

        // 4. Authenticated
        boolean isAuthenticated = extractBoolean(
                request.getContext() != null ? request.getContext() : java.util.Collections.emptyMap(),
                ctxConfig.getKeys().getAuthenticated(), ctxConfig.getDefaults().isAuthenticated());

        // 5. User ID
        String userId = extractString(
                request.getContext() != null ? request.getContext() : java.util.Collections.emptyMap(),
                ctxConfig.getKeys().getUserId(), ctxConfig.getDefaults().getUserId());

        // Fetch available versions for discovery checks
        List<String> availableVersions = mfeRepository.findByName(remoteName).map(Microfrontend::getVersions)
                .map(vers -> vers.stream().map(Version::getVersion).toList())
                .orElse(java.util.Collections.emptyList());

        com.maverick.feature.dto.governance.PolicyInput policyInput = com.maverick.feature.dto.governance.PolicyInput
                .builder()
                .mfeName(remoteName)
                .environment(env.name())
                .env(env.name().toLowerCase()) // Match Rego input.env
                .channel(activeVariant != null ? activeVariant.getName().toLowerCase() : "stable")
                .version(selectedVersion.getVersion())
                .selectedVersion(selectedVersion.getVersion())
                .remoteEntry(selectedVersion.getRemoteEntry())
                .exposedModule("./Module") // Default for MFEs
                .fallback(fallbackVersion.getVersion())
                .route(request.getContext() != null ? String.valueOf(request.getContext().getOrDefault("route", "/"))
                        : "/")
                .user(com.maverick.feature.dto.governance.PolicyInput.UserContext.builder()
                        .userId(userId)
                        .roles(userRoles)
                        .isInternal(isInternalUser)
                        .department(department)
                        .authenticated(isAuthenticated)
                        .build())
                .features(convertRef(flags))
                .availableVersions(availableVersions)
                .metrics(Map.of("errorRate", 0.02, "p95LoadMs", 450.0)) // Telemetry usually comes from Monitoring, not
                                                                        // Client Request
                .requires(Map.of("angular", "17.0.0", "rxjs", "7.8.0")) // Simulated Manifest
                .host(Map.of("angular", "17.0.0", "rxjs", "7.8.0")) // Host Runtime
                .designTokens(Map.of("version", "2.1.0"))
                .accessibility(Map.of("wcag", "2.1"))
                .build();

        com.maverick.feature.dto.governance.GovernanceResult governanceResult;
        boolean isEnforcementEnabled = ff4j.check("governance.enforcement");

        if (isEnforcementEnabled) {
            governanceResult = governanceService.evaluate(policyInput);
        } else {
            log.info("GOVERNANCE BYPASS: Enforcement is disabled via Global Toggle.");
            governanceResult = com.maverick.feature.dto.governance.GovernanceResult.builder()
                    .allowed(true)
                    .rejectionReason(null)
                    .shouldBlock(false)
                    .build();
        }

        if (!governanceResult.isAllowed()) {
            log.warn("GOVERNANCE DENIAL: {} - Reason: {}", remoteName, governanceResult.getRejectionReason());

            // "Production Hard Block" check
            if (governanceResult.isShouldBlock()) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "Access Denied: " + governanceResult.getRejectionReason());
            }

            // Enforce Fallback (or 403 if critical)
            // Here we choose safe fallback to Standard/Production version
            selectedVersion = fallbackVersion;

            // Explicitly mark variant as Standard due to policy override
            activeVariant = ResolutionResponse.Variant.builder()
                    .name("standard (fallback)")
                    .type(com.maverick.feature.domain.VariantType.STANDARD)
                    .build();
        } else {
            log.info("Governance Check Passed for {}", remoteName);
        }
        ResolutionResponse.ResolutionResponseBuilder responseBuilder = ResolutionResponse.builder()
                .remoteName(remoteName)
                .environment(env.name())
                .selected(toRemoteVersion(selectedVersion))
                .fallback(toRemoteVersion(fallbackVersion)) // Populated Fallback
                .cacheTtlSeconds(60);

        ResolutionResponse.ResolutionContext.ResolutionContextBuilder contextBuilder = ResolutionResponse.ResolutionContext
                .builder()
                .flags(flags)
                .variant(activeVariant);

        if (!governanceResult.isAllowed()) {
            contextBuilder.governanceReason(governanceResult.getRejectionReason());
        }

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

    private Map<String, Boolean> convertRef(Map<String, Object> input) {
        Map<String, Boolean> res = new HashMap<>();
        input.forEach((k, v) -> {
            if (v instanceof Boolean)
                res.put(k, (Boolean) v);
        });
        return res;
    }

    // --- Context Extraction Helpers (Production Grade) ---

    private String extractString(Map<String, Object> context, String key, String defaultValue) {
        if (!context.containsKey(key))
            return defaultValue;
        return String.valueOf(context.get(key));
    }

    private boolean extractBoolean(Map<String, Object> context, String key, boolean defaultValue) {
        if (!context.containsKey(key))
            return defaultValue;
        Object val = context.get(key);
        if (val instanceof Boolean)
            return (Boolean) val;
        return "true".equalsIgnoreCase(String.valueOf(val));
    }

    private List<String> extractRoles(Map<String, Object> context, String key) {
        List<String> roles = new java.util.ArrayList<>();
        if (context.containsKey(key)) {
            Object obj = context.get(key);
            if (obj instanceof List<?>) {
                for (Object item : (List<?>) obj) {
                    roles.add(String.valueOf(item));
                }
            } else if (obj instanceof String) {
                roles.add((String) obj);
            }
        }
        return roles;
    }
}
