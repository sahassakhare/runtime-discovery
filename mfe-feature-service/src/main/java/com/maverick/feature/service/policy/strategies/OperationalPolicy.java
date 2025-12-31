package com.maverick.feature.service.policy.strategies;

import com.maverick.feature.dto.governance.PolicyInput;
import com.maverick.feature.service.policy.GovernancePolicy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class OperationalPolicy implements GovernancePolicy {

    @Override
    public String getType() {
        return "OPERATIONAL";
    }

    @Override
    public Optional<String> evaluate(PolicyInput input, Map<String, Object> config) {
        // Configurable Flag Check
        // e.g. {"flag_key": "maintenance-mode", "bypass_internal": true}
        String flagKey = (String) config.getOrDefault("flag_key", "maintenance-mode");
        boolean bypassInternal = (Boolean) config.getOrDefault("bypass_internal", true);

        if (Boolean.TRUE.equals(input.getFeatures().get(flagKey))) {
            if (bypassInternal && input.getUser().isInternal()) {
                return Optional.empty();
            }
            return Optional.of("Operational Violation: System is in Maintenance Mode.");
        }
        return Optional.empty();
    }
}
