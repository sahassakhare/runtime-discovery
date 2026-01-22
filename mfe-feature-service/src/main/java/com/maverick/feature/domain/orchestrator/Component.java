package com.maverick.feature.domain.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Component {
    private String id;
    private String type;
    private String title;
    private String name;
    private String remote;
    private String exposedModule;
    private Map<String, Object> props;
    private List<String> children;
}
