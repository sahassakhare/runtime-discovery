package com.maverick.feature.service.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${llm.endpoint:https://generativelanguage.googleapis.com/v1beta/models/}")
    private String baseUrl;

    @Value("${llm.model:gemini-1.5-pro}")
    private String model;

    @Value("${llm.api-key:}")
    private String apiKey;

    @jakarta.annotation.PostConstruct
    public void logKeyStatus() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn(">>> GEMINI_API_KEY is NOT configured. Orchestration will fallback.");
        } else {
            log.info(">>> GEMINI_API_KEY is configured (Length: {}). Orchestration active.", apiKey.length());
        }
    }

    public Mono<String> generate(String systemPrompt, String userIntent) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.error(new IllegalStateException("GEMINI_API_KEY is not configured."));
        }

        String url = baseUrl + model + ":generateContent?key=" + apiKey;
        String fullPrompt = systemPrompt + "\n\nUser Request: " + userIntent;

        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .role("user")
                                .parts(List.of(GeminiRequest.Part.builder().text(fullPrompt).build()))
                                .build()))
                .build();

        log.info("Sending request to Gemini (Reactive): Model={}, Url={}", model, url);

        return webClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(response -> {
                    if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                        String rawText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                        return cleanResponse(rawText);
                    }
                    return "";
                })
                .onErrorResume(e -> {
                    log.error("Failed to call Gemini API", e);
                    return Mono.error(new RuntimeException("LLM Orchestration failed", e));
                });
    }

    private String cleanResponse(String raw) {
        // Remove markdown code blocks if present
        String result = raw.trim();
        if (result.startsWith("```json")) {
            result = result.substring(7);
        } else if (result.startsWith("```")) {
            result = result.substring(3);
        }
        if (result.endsWith("```")) {
            result = result.substring(0, result.length() - 3);
        }
        return result.trim();
    }
}
