package com.maverick.feature.controller;

import com.maverick.feature.domain.orchestrator.UiResponse;
import com.maverick.feature.service.OrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orchestrate")
@CrossOrigin(origins = "*")
public class OrchestrationController {

    private final com.maverick.feature.service.llm.PromptEngineeringService promptService;
    private final OrchestrationService orchestrationService;

    public OrchestrationController(OrchestrationService orchestrationService,
            com.maverick.feature.service.llm.PromptEngineeringService promptService) {
        this.orchestrationService = orchestrationService;
        this.promptService = promptService;
    }

    @GetMapping("/config")
    public Mono<ResponseEntity<Map<String, Object>>> getOrchestratorConfig() {
        return Mono.fromCallable(() -> {
            Map<String, Object> config = Map.of(
                    "template", promptService.getBasePrompt(),
                    "registry", promptService.getDiscoveryList());
            return ResponseEntity.ok(config);
        });
    }

    @PostMapping
    public Mono<ResponseEntity<UiResponse>> orchestrate(@RequestBody Map<String, String> payload) {
        String intent = payload.get("intent");
        return orchestrationService.orchestrate(intent)
                .map(ResponseEntity::ok);
    }
}
