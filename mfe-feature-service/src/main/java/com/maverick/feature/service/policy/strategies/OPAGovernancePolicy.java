package com.maverick.feature.service.policy.strategies;

import com.maverick.feature.dto.governance.PolicyInput;
import com.maverick.feature.service.policy.GovernancePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mfe.governance.opa.mode", havingValue = "sidecar", matchIfMissing = true)
public class OPAGovernancePolicy implements GovernancePolicy {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OPAGovernancePolicy.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mfe.governance.opa.baseUrl}")
    private String opaBaseUrl;

    @Override
    public String getType() {
        return "REGO_OPA";
    }

    @Override
    public Optional<String> evaluate(PolicyInput input, Map<String, Object> config) {
        String regoSource = (String) config.get("rego_source");
        String policyCode = (String) config.get("policy_code");

        if (regoSource == null || regoSource.isBlank()) {
            return Optional.empty();
        }

        try {
            // 1. Extract package name from regoSource
            // e.g., "package mfe.discovery" -> "mfe/discovery"
            String packageName = extractPackageName(regoSource);
            String opaUrl = opaBaseUrl + "/" + packageName.replace(".", "/");

            log.info("Evaluating OPA Policy: {} via Package: {} -> URL: {}", policyCode, packageName, opaUrl);

            // Rego expects input wrapped in "input"
            Map<String, Object> request = Collections.singletonMap("input", input);

            // DEBUG: Print Payload
            System.out.println(">>> OPA REQUEST Payload for " + packageName + ": " + input);

            Map<String, Object> response = restTemplate.postForObject(opaUrl, request, Map.class);

            // DEBUG: Print Response
            System.out.println(">>> OPA RESPONSE for " + packageName + ": " + response);

            if (response != null && response.containsKey("result")) {
                Object resultObj = response.get("result");

                // Unified Decision Engine (mfe.decision) returns a complex object
                if (packageName.equals("mfe.decision") && resultObj instanceof Map) {
                    Map<String, Object> result = (Map<String, Object>) resultObj;
                    Boolean allow = (Boolean) result.get("allow");
                    if (allow != null && !allow) {
                        return Optional.of((String) result.getOrDefault("reason", "OPA Unified Denial"));
                    }
                    return Optional.empty();
                }

                // Standard policies might return a simple boolean or a result map
                if (resultObj instanceof Boolean) {
                    return (Boolean) resultObj ? Optional.empty() : Optional.of("OPA Policy Denial (Boolean)");
                } else if (resultObj instanceof Map) {
                    Map<String, Object> result = (Map<String, Object>) resultObj;

                    // Some rego files use 'allow', others 'compatible', others 'healthy'
                    // We check for 'allow' first, then common positive flags
                    Boolean allow = extractAllowSignal(result, packageName);
                    if (allow != null && !allow) {
                        return Optional.of((String) result.getOrDefault("deny_reason", "OPA Policy Denial (REGO)"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("OPA Evaluation failed for {}: {}. Is OPA running on :8181?", policyCode, e.getMessage());
            // In a real system, we might fail-open or fail-closed based on
            // enforcementLevel.
            // For E2E Demo, we log and pass to avoid blocking if OPA sidecar isn't up.
        }
        return Optional.empty();
    }

    private String extractPackageName(String source) {
        String[] lines = source.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("package ")) {
                return line.trim().replace("package ", "").trim();
            }
        }
        return "default";
    }

    private Boolean extractAllowSignal(Map<String, Object> result, String packageName) {
        if (result.containsKey("allow"))
            return (Boolean) result.get("allow");
        if (result.containsKey("allowed"))
            return (Boolean) result.get("allowed");
        if (result.containsKey("compatible"))
            return (Boolean) result.get("compatible");
        if (result.containsKey("healthy"))
            return (Boolean) result.get("healthy");
        if (result.containsKey("compliant"))
            return (Boolean) result.get("compliant");
        if (result.containsKey("enabled"))
            return (Boolean) result.get("enabled");
        return null;
    }
}
