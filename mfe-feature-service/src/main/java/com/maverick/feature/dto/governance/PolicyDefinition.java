package com.maverick.feature.dto.governance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PolicyDefinition {
    private String id;
    private String name;
    private String category; // Discovery, Routing, Security, Compatibility, Operational
    private String description;
    private String enforcementLevel; // BLOCK, WARNING, LOG, ALLOW
    private String type; // Strategy Key
}
