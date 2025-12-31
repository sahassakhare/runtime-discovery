package com.maverick.feature.service.policy;

import com.maverick.feature.dto.governance.PolicyInput;
import java.util.Map;
import java.util.Optional;

public interface GovernancePolicy {
    /**
     * Unique key identifying this strategy (e.g., ENV_INTEGRITY).
     * Matches the 'type' field in the Policy entity.
     */
    String getType();

    /**
     * Evaluates the policy against the input using the provided configuration.
     * 
     * @param input  Contextual data (User, Env, Version, etc.)
     * @param config Configuration map derived from Policy entity JSON
     * @return Optional violation message if policy is violated, empty otherwise.
     */
    Optional<String> evaluate(PolicyInput input, Map<String, Object> config);
}
