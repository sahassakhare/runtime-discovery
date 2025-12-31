package com.maverick.feature.service.policy.strategies;

import com.maverick.feature.dto.governance.PolicyInput;
import com.maverick.feature.service.policy.GovernancePolicy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class CompatibilityPolicy implements GovernancePolicy {

    @Override
    public String getType() {
        return "COMPATIBILITY";
    }

    @Override
    public Optional<String> evaluate(PolicyInput input, Map<String, Object> config) {
        String version = input.getVersion();
        if (version == null)
            return Optional.empty();

        // Configurable Deprecated Prefix
        // e.g. {"blocked_prefixes": ["0."]}
        Object blockedPrefixesObj = config.get("blocked_prefixes");
        if (blockedPrefixesObj instanceof java.util.List) {
            java.util.List<String> prefixes = (java.util.List<String>) blockedPrefixesObj;
            for (String prefix : prefixes) {
                if (version.startsWith(prefix)) {
                    return Optional.of("Lifecycle Violation: Version starting with " + prefix + " is deprecated.");
                }
            }
        }
        return Optional.empty();
    }
}
