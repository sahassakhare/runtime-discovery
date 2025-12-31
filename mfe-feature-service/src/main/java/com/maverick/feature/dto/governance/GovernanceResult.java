package com.maverick.feature.dto.governance;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GovernanceResult {
    private boolean allowed;
    private String rejectionReason;
    private String decisionId; // For auditing
    private List<String> violatedPolicies;
    private String fallbackVersion; // Optional suggestion
}
