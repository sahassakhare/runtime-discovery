package com.maverick.feature.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maverick.feature.domain.orchestrator.UiResponse;
import com.maverick.feature.domain.orchestrator.SurfaceUpdate;
import com.maverick.feature.domain.orchestrator.Component;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import reactor.core.publisher.Mono;

import com.maverick.feature.repository.DeploymentRepository;
import com.maverick.feature.domain.Deployment;
import com.maverick.feature.domain.Environment;

@Service
public class OrchestrationService {

    private final ObjectMapper objectMapper;
    private final DeploymentRepository deploymentRepository;
    private final com.maverick.feature.service.llm.GeminiClient geminiClient;
    private final com.maverick.feature.service.llm.PromptEngineeringService promptService;

    public OrchestrationService(ObjectMapper objectMapper,
            DeploymentRepository deploymentRepository,
            com.maverick.feature.service.llm.GeminiClient geminiClient,
            com.maverick.feature.service.llm.PromptEngineeringService promptService) {
        this.objectMapper = objectMapper;
        this.deploymentRepository = deploymentRepository;
        this.geminiClient = geminiClient;
        this.promptService = promptService;
    }

    public Mono<UiResponse> orchestrate(String intent) {
        return Mono.defer(() -> {
            // 1. Build Context-Aware Prompt (Synchronous logic, but let's keep it in the
            // chain)
            String systemPrompt = promptService.buildSystemPrompt(intent, Map.of());

            // 2. Call LLM (Reactive)
            return geminiClient.generate(systemPrompt, intent)
                    .flatMap(jsonResponse -> {
                        try {
                            // 3. Parse & Validate
                            return Mono.just(objectMapper.readValue(jsonResponse, UiResponse.class));
                        } catch (Exception e) {
                            return Mono.error(e);
                        }
                    })
                    .onErrorResume(e -> {
                        System.err.println("LLM Orchestration failed (falling back to static): " + e.getMessage());
                        return Mono.fromCallable(() -> fallbackOrchestration(intent))
                                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
                    });
        });
    }

    private UiResponse fallbackOrchestration(String intent) {
        String lowerIntent = intent.toLowerCase();

        // Root container
        Component root = Component.builder()
                .id("main-surface")
                .type("a2ui.v1.Page")
                .title("Orchestrated Response (Fallback)")
                .children(List.of("welcome-msg")) // Default children
                .build();

        // Response Message
        String messageText = "I processed your request using the fallback engine. ";

        // Simple routing logic
        boolean showProfile = lowerIntent.contains("profile") || lowerIntent.contains("user")
                || lowerIntent.contains("account");

        if (showProfile) {
            messageText += "Here is your profile:";
            // Add profile container to children
            root.setChildren(List.of("welcome-msg", "profile-mfe-container"));
        } else {
            messageText += "However, I only support 'profile' related requests in this mode directly. Please try 'show my profile'.";
        }

        Component message = Component.builder()
                .id("welcome-msg")
                .type("a2ui.v1.Text")
                .props(Map.of("text", messageText))
                .build();

        // Always build the MFE definition, but only include in tree if needed
        // Dynamic Lookup for 'remote-profile'
        // NOTE: This call to deploymentRepository is BLOCKING (JPA)
        Optional<Deployment> deploymentOpt = deploymentRepository.findActiveGlobal("remote-profile",
                Environment.PRODUCTION);
        String remoteName = "remote-profile";

        Component mfe = Component.builder()
                .id("profile-mfe-container")
                .type("a2ui.v1.Mfe")
                .remote(remoteName)
                .exposedModule("./ProfileComponent")
                .props(Map.of("userId", "12345"))
                .build();

        SurfaceUpdate surface = SurfaceUpdate.builder()
                .root("main-surface")
                .components(List.of(root, message, mfe))
                .build();

        return UiResponse.builder()
                .a2uiVersion("1.2")
                .surfaceUpdate(surface)
                .build();
    }

    // Helper to view prompt if needed for debugging
    public String getSystemPrompt() {
        return promptService.buildSystemPrompt("", Map.of());
    }
}
