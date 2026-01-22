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

        // Mocks
        private final com.maverick.feature.repository.DeploymentRepository deploymentRepository = org.mockito.Mockito
                        .mock(com.maverick.feature.repository.DeploymentRepository.class);
        private final com.maverick.feature.service.llm.GeminiClient geminiClient = org.mockito.Mockito
                        .mock(com.maverick.feature.service.llm.GeminiClient.class);
        private final com.maverick.feature.service.llm.PromptEngineeringService promptService = org.mockito.Mockito
                        .mock(com.maverick.feature.service.llm.PromptEngineeringService.class);

        private final OrchestrationService orchestrationService = new OrchestrationService(objectMapper,
                        deploymentRepository, geminiClient, promptService);

        @Test
        public void testGetSystemPrompt() {
                // Setup mock
                org.mockito.Mockito
                                .when(promptService.buildSystemPrompt(org.mockito.ArgumentMatchers.anyString(),
                                                org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn("Mock System Prompt");

                String prompt = orchestrationService.getSystemPrompt();
                Assertions.assertNotNull(prompt);
                Assertions.assertEquals("Mock System Prompt", prompt);
        }

        @Test
        public void testOrchestrateReactive() {
                // Setup mocks
                org.mockito.Mockito
                                .when(promptService.buildSystemPrompt(org.mockito.ArgumentMatchers.anyString(),
                                                org.mockito.ArgumentMatchers.anyMap()))
                                .thenReturn("Mock System Prompt");
                org.mockito.Mockito
                                .when(geminiClient.generate(org.mockito.ArgumentMatchers.anyString(),
                                                org.mockito.ArgumentMatchers.anyString()))
                                .thenReturn(reactor.core.publisher.Mono.just("{\"a2uiVersion\":\"1.2\"}"));

                UiResponse response = orchestrationService.orchestrate("hello").block();
                Assertions.assertNotNull(response);
                Assertions.assertEquals("1.2", response.getA2uiVersion());
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

                String json = "";
                try {
                        json = objectMapper.writeValueAsString(response);
                } catch (Exception e) {
                        Assertions.fail(e.getMessage());
                }
                Assertions.assertNotNull(json);
                Assertions.assertTrue(json.contains("\"a2uiVersion\":\"1.2\""));
                Assertions.assertTrue(json.contains("\"type\":\"a2ui.v1.Page\""));
        }
}
