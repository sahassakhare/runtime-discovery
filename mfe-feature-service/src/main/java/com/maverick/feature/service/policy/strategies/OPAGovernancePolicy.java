package com.maverick.feature.service.policy.strategies;

import com.maverick.feature.dto.governance.PolicyInput;
import com.maverick.feature.service.policy.GovernancePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OPAGovernancePolicy implements GovernancePolicy {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPA_URL = "http://localhost:8181/v1/data/mfe/governance";

    @Override
    public String getType() {
        return "REGO_OPA";
    }

    @Override
    public Optional<String> evaluate(PolicyInput input, Map<String, Object> config) {
        try {
            // Rego expects input wrapped in "input"
            Map<String, Object> request = Collections.singletonMap("input", input);

            Map<String, Object> response = restTemplate.postForObject(OPA_URL, request, Map.class);

            if (response != null && response.containsKey("result")) {
                Map<String, Object> result = (Map<String, Object>) response.get("result");

                // Rego returns 'allow' (boolean) and 'reason' (array of strings)
                Boolean allow = (Boolean) result.get("allow");
                if (allow != null && !allow) {
                    Object reason = result.get("reason");
                    if (reason instanceof java.util.List) {
                        java.util.List<String> reasons = (java.util.List<String>) reason;
                        return Optional.of(String.join("; ", reasons));
                    }
                    return Optional.of("OPA Policy Deial (REGO)");
                }
            }
        } catch (Exception e) {
            System.err.println("OPA Evaluation failed: " + e.getMessage());
            // Fail open or closed? Usually for governance we might want to log
        }
        return Optional.empty();
    }
}
