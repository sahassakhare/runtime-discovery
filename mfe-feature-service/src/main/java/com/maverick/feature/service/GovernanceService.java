package com.maverick.feature.service;

import com.maverick.feature.dto.governance.GovernanceResult;
import com.maverick.feature.dto.governance.PolicyInput;
import com.maverick.feature.repository.PolicyRepository;
import com.maverick.feature.service.policy.GovernancePolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GovernanceService {

    private final PolicyRepository policyRepository;
    private final List<GovernancePolicy> strategies;
    private final ObjectMapper objectMapper;

    /**
     * Evaluates the request against Active Policies from Database.
     */
    public GovernanceResult evaluate(PolicyInput input) {
        List<String> violations = new ArrayList<>();
        List<com.maverick.feature.domain.Policy> activePolicies = policyRepository.findByIsActiveTrue();

        // Map strategies by Type for O(1) lookup
        Map<String, GovernancePolicy> strategyMap = strategies.stream()
                .collect(Collectors.toMap(GovernancePolicy::getType, s -> s));

        for (com.maverick.feature.domain.Policy policy : activePolicies) {
            GovernancePolicy strategy = strategyMap.get(policy.getType());

            if (strategy != null) {
                try {
                    Map<String, Object> config = new java.util.HashMap<>();
                    if (policy.getConfiguration() != null && !policy.getConfiguration().isBlank()) {
                        config = objectMapper.readValue(policy.getConfiguration(),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                                });
                    }

                    Optional<String> result = strategy.evaluate(input, config);
                    result.ifPresent(violations::add);

                } catch (Exception e) {
                    System.err.println("Failed to evaluate policy " + policy.getCode() + ": " + e.getMessage());
                }
            }
        }

        boolean allowed = violations.isEmpty();

        // Check for explicit ALLOW signals from policies.
        // If a policy has level ALLOW and evaluates successfully (no violations), it
        // remains a passive allow.
        // But if it "matches" a specific condition that we want to explicitly permit,
        // we need a way to signal that.
        // For now, the implementation follows a "Grant Override" model:
        // If ANY policy with Level ALLOW is active and doesn't report a violation of
        // its own logic,
        // we can use it to override blocks.

        boolean hasExplicitAllow = activePolicies.stream()
                .filter(p -> "ALLOW".equalsIgnoreCase(p.getEnforcementLevel()))
                .anyMatch(p -> {
                    GovernancePolicy strategy = strategyMap.get(p.getType());
                    if (strategy == null)
                        return false;
                    try {
                        Map<String, Object> config = objectMapper.readValue(p.getConfiguration(), Map.class);
                        return strategy.evaluate(input, config).isEmpty(); // No violation = "Matches Permit Condition"
                    } catch (Exception e) {
                        return false;
                    }
                });

        if (hasExplicitAllow) {
            allowed = true;
        }

        return GovernanceResult.builder()
                .allowed(allowed)
                .violatedPolicies(violations)
                .rejectionReason(allowed ? null : String.join("; ", violations))
                .decisionId(UUID.randomUUID().toString())
                .build();
    }

    public List<com.maverick.feature.dto.governance.PolicyDefinition> getCatalog() {
        return policyRepository.findByIsActiveTrue().stream()
                .map(p -> com.maverick.feature.dto.governance.PolicyDefinition.builder()
                        .id(p.getCode())
                        .name(p.getName())
                        .category(p.getCategory())
                        .description(p.getDescription())
                        .enforcementLevel(p.getEnforcementLevel())
                        .type(p.getType())
                        .build())
                .toList();
    }

    public boolean updatePolicy(String code, String enforcementLevel, String description) {
        Optional<com.maverick.feature.domain.Policy> policyOpt = policyRepository.findByCode(code);
        if (policyOpt.isEmpty())
            return false;

        com.maverick.feature.domain.Policy policy = policyOpt.get();
        if (enforcementLevel != null)
            policy.setEnforcementLevel(enforcementLevel);
        if (description != null)
            policy.setDescription(description);

        policyRepository.save(policy);
        return true;
    }

    public com.maverick.feature.domain.Policy createPolicy(String name, String type, String category,
            String enforcementLevel, String description) {
        String code = "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        com.maverick.feature.domain.Policy policy = com.maverick.feature.domain.Policy.builder()
                .code(code)
                .name(name)
                .type(type)
                .category(category)
                .enforcementLevel(enforcementLevel != null ? enforcementLevel : "LOG")
                .description(description)
                .isActive(true)
                .configuration("{}") // Default empty config
                .build();

        return policyRepository.save(policy);
    }
}
