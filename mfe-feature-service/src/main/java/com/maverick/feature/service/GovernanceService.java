package com.maverick.feature.service;

import com.maverick.feature.dto.governance.GovernanceResult;
import com.maverick.feature.dto.governance.PolicyInput;
import java.util.List;

public interface GovernanceService {
    GovernanceResult evaluate(PolicyInput input);

    List<com.maverick.feature.dto.governance.PolicyDefinition> getCatalog();

    boolean togglePolicy(String code, boolean active);

    void toggleAllPolicies(boolean active);

    boolean updatePolicy(String code, String enforcementLevel, String description, String configuration);

    com.maverick.feature.domain.Policy createPolicy(String name, String type, String category, String enforcementLevel,
            String description, String configuration);
}
