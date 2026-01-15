package com.maverick.feature.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maverick.feature.domain.orchestrator.UiResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class OrchestrationService {

    private final ObjectMapper objectMapper;

    public OrchestrationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getSystemPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/orchestrator-agent.md");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Orchestration Agent prompt", e);
        }
    }

    public String serializeResponse(UiResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize UI Response", e);
        }
    }
}
