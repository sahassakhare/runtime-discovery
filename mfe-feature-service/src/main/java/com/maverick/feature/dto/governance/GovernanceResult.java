package com.maverick.feature.dto.governance;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
public class GovernanceResult {
    private boolean allowed;
    private String rejectionReason;
    private String decisionId; // For auditing
    private List<String> violatedPolicies;
    private String fallbackVersion; // Optional suggestion
    private boolean shouldBlock; // If true, return 403 instead of Fallback

    public GovernanceResult() {
    }

    public GovernanceResult(boolean allowed, String rejectionReason, String decisionId, List<String> violatedPolicies,
            String fallbackVersion, boolean shouldBlock) {
        this.allowed = allowed;
        this.rejectionReason = rejectionReason;
        this.decisionId = decisionId;
        this.violatedPolicies = violatedPolicies;
        this.fallbackVersion = fallbackVersion;
        this.shouldBlock = shouldBlock;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public List<String> getViolatedPolicies() {
        return violatedPolicies;
    }

    public void setViolatedPolicies(List<String> violatedPolicies) {
        this.violatedPolicies = violatedPolicies;
    }

    public String getFallbackVersion() {
        return fallbackVersion;
    }

    public void setFallbackVersion(String fallbackVersion) {
        this.fallbackVersion = fallbackVersion;
    }

    public boolean isShouldBlock() {
        return shouldBlock;
    }

    public void setShouldBlock(boolean shouldBlock) {
        this.shouldBlock = shouldBlock;
    }

    public static GovernanceResultBuilder builder() {
        return new GovernanceResultBuilder();
    }

    public static class GovernanceResultBuilder {
        private boolean allowed;
        private String rejectionReason;
        private String decisionId;
        private List<String> violatedPolicies;
        private String fallbackVersion;
        private boolean shouldBlock;

        public GovernanceResultBuilder allowed(boolean allowed) {
            this.allowed = allowed;
            return this;
        }

        public GovernanceResultBuilder rejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        public GovernanceResultBuilder decisionId(String decisionId) {
            this.decisionId = decisionId;
            return this;
        }

        public GovernanceResultBuilder violatedPolicies(List<String> violatedPolicies) {
            this.violatedPolicies = violatedPolicies;
            return this;
        }

        public GovernanceResultBuilder fallbackVersion(String fallbackVersion) {
            this.fallbackVersion = fallbackVersion;
            return this;
        }

        public GovernanceResultBuilder shouldBlock(boolean shouldBlock) {
            this.shouldBlock = shouldBlock;
            return this;
        }

        public GovernanceResult build() {
            return new GovernanceResult(allowed, rejectionReason, decisionId, violatedPolicies, fallbackVersion,
                    shouldBlock);
        }
    }
}
