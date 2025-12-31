package com.maverick.feature.service.policy.strategies;

import com.maverick.feature.dto.governance.PolicyInput;
import com.maverick.feature.service.policy.GovernancePolicy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class EnvironmentIntegrityPolicy implements GovernancePolicy {

    @Override
    public String getType() {
        return "ENV_INTEGRITY";
    }

    @Override
    public Optional<String> evaluate(PolicyInput input, Map<String, Object> config) {
        String inputEnv = input.getEnvironment().toUpperCase();
        String inputChannel = input.getChannel().toUpperCase();

        // Configurable Rules
        // e.g. {"restricted_env": "PRODUCTION", "restricted_channels": ["CANARY"],
        // "bypass_role": "INTERNAL"}
        String restrictedEnv = (String) config.getOrDefault("restricted_env", "PRODUCTION");
        List<String> restrictedChannels = (List<String>) config.getOrDefault("restricted_channels", List.of("CANARY"));
        boolean isInternal = input.getUser().isInternal();

        if (inputEnv.equals(restrictedEnv) && restrictedChannels.contains(inputChannel)) {
            if (!isInternal) {
                return Optional.of("Environment Integrity Violation: External users cannot access " + inputChannel
                        + " in " + inputEnv);
            }
        }
        return Optional.empty();
    }
}
