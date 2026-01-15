package com.maverick.feature.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maverick.feature.domain.orchestrator.Component;
import com.maverick.feature.domain.orchestrator.SurfaceUpdate;
import com.maverick.feature.domain.orchestrator.UiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OrchestrationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrchestrationService orchestrationService = new OrchestrationService(objectMapper);

    @Test
    public void testGetSystemPrompt() {
        String prompt = orchestrationService.getSystemPrompt();
        Assertions.assertNotNull(prompt);
        Assertions.assertTrue(prompt.contains("Enterprise UI Orchestration Agent"));
        Assertions.assertTrue(prompt.contains("Adjacency List Model"));
    }

    @Test
    public void testSerializeResponse() {
        Component root = Component.builder()
                .id("root")
                .type("a2ui.v1.Page")
                .children(List.of("child1"))
                .build();

        Component child = Component.builder()
                .id("child1")
                .type("a2ui.v1.Button")
                .props(Map.of("label", "Submit"))
                .build();

        SurfaceUpdate surfaceUpdate = SurfaceUpdate.builder()
                .root("root")
                .components(List.of(root, child))
                .build();

        UiResponse response = UiResponse.builder()
                .a2uiVersion("1.2")
                .surfaceUpdate(surfaceUpdate)
                .build();

        String json = orchestrationService.serializeResponse(response);
        Assertions.assertNotNull(json);
        Assertions.assertTrue(json.contains("\"a2uiVersion\":\"1.2\""));
        Assertions.assertTrue(json.contains("\"type\":\"a2ui.v1.Page\""));
    }
}
