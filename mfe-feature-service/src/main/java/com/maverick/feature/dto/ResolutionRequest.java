package com.maverick.feature.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ResolutionRequest {
    private String remoteName;
    private String flagName;
    private Map<String, Object> context;
}
