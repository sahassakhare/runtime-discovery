package com.maverick.feature.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ResolutionRequest {
    private String remoteName;
    private String flagName;
    private String environment; // Production, Staging
    private String tenantId; // Multi-tenancy
    private Map<String, Object> context;
}
