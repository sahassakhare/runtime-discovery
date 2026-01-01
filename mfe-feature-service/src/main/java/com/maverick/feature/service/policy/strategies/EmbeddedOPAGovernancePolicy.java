package com.maverick.feature.service.policy.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maverick.feature.dto.governance.PolicyInput;
import com.maverick.feature.service.policy.GovernancePolicy;
import com.styra.opa.wasm.OpaPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance alternative to Sidecar OPA.
 * This would eventually use a WASM-compiled Rego engine or a Java OPA SDK
 * to evaluate policies in-process (zero network hop).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mfe.governance.opa.mode", havingValue = "embedded")
public class EmbeddedOPAGovernancePolicy implements GovernancePolicy {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmbeddedOPAGovernancePolicy.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, OpaPolicy> moduleCache = new ConcurrentHashMap<>();

    @Value("${mfe.governance.opa.binaryPath}")
    private String opaBinaryPath;

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
            // 1. Get or Create WASM Module
            OpaPolicy module = getModule(policyCode, regoSource);

            // 2. Evaluate using SDK (In-Process, No Binary, No Network)
            String inputJson = objectMapper.writeValueAsString(input);
            log.debug("[EMBEDDED-SDK] Evaluating policy: {}", policyCode);

            String resultJson = module.evaluate(inputJson);

            log.debug("[EMBEDDED-SDK] Result: {}", resultJson);

            // 3. Parse result
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(resultJson);

            // Handle array result from SDK: [{"result": ...}]
            if (root.isArray() && root.size() > 0) {
                com.fasterxml.jackson.databind.JsonNode resultNode = root.get(0).get("result");

                // Case 1: Simple boolean denial (allow = false) -> result: false
                if (resultNode != null && resultNode.isBoolean() && !resultNode.asBoolean()) {
                    return Optional.of("Embedded SDK: Access Denied");
                }

                // Case 2: Complex object denial -> result: {"allow": false, "reason": "..."}
                if (resultNode != null && resultNode.isObject() && resultNode.has("allow")) {
                    if (!resultNode.get("allow").asBoolean()) {
                        return Optional.of(resultNode.has("reason") ? resultNode.get("reason").asText()
                                : "Embedded SDK: Access Denied");
                    }
                }
            }

            // Handle raw boolean false (unlikely from SDK but possible)
            if (root.isBoolean() && !root.asBoolean()) {
                return Optional.of("Embedded SDK: Access Denied");
            }

        } catch (Exception e) {
            log.error("[EMBEDDED-SDK] Evaluation failed: {}", e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private synchronized OpaPolicy getModule(String policyCode, String regoSource) throws Exception {
        return moduleCache.computeIfAbsent(policyCode, code -> {
            try {
                // 1. Dynamic WASM Loading from Classpath (Immutable Artifact)
                String resourcePath = "policies/" + policyCode + ".wasm";

                log.info("[EMBEDDED-SDK] Loading WASM Module from Classpath: {}", resourcePath);

                org.springframework.core.io.Resource wasmResource = new org.springframework.core.io.ClassPathResource(
                        resourcePath);

                if (wasmResource.exists()) {
                    return OpaPolicy.builder().withPolicy(wasmResource.getContentAsByteArray()).build();
                } else {
                    throw new RuntimeException("WASM module not found in classpath at " + resourcePath
                            + ". Please run ./compile_policies.sh and rebuild.");
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to load WASM for " + policyCode, e);
            }
        });
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
}
