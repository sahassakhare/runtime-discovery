package com.maverick.feature.domain.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UiResponse {
    private String a2uiVersion;
    private Map<String, Object> telemetry;
    private SurfaceUpdate surfaceUpdate;
    private Map<String, Object> dataModelUpdate;
}
