package com.maverick.feature.service.policy.strategies;

import com.maverick.feature.dto.governance.PolicyInput;
import com.maverick.feature.service.policy.GovernancePolicy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SecurityPolicy implements GovernancePolicy {

    @Override
    public String getType() {
        return "SECURITY_ABAC";
    }

    @Override
    public Optional<String> evaluate(PolicyInput input, Map<String, Object> config) {
        String mfeName = input.getMfeName();

        // Configurable Role Requirements
        // e.g. {"target_mfe": "remote-audit", "required_role": "ADMIN"}
        String targetMfe = (String) config.get("target_mfe");
        String requiredRole = (String) config.get("required_role");

        if (targetMfe != null && targetMfe.equals(mfeName)) {
            List<String> userRoles = input.getUser().getRoles();
            if (userRoles == null || !userRoles.contains(requiredRole)) {
                return Optional.of("Security Violation: Access to " + mfeName + " requires " + requiredRole + " role.");
            }
        }
        return Optional.empty();
    }
}
