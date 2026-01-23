package com.maverick.feature.service;

import com.maverick.feature.domain.orchestrator.UiResponse;
import reactor.core.publisher.Mono;

public interface OrchestrationService {
    Mono<UiResponse> orchestrate(String intent);

    String getSystemPrompt();
}
