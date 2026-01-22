package com.maverick.feature.service.llm;

import com.maverick.feature.domain.Deployment;
import com.maverick.feature.domain.Environment;
import com.maverick.feature.repository.DeploymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class PromptEngineeringService {

    private final DeploymentRepository deploymentRepository;

    public String buildSystemPrompt(String userIntent, Map<String, Object> userContext) {
        String basePrompt = loadBasePrompt();
        String discoveryBlock = buildDiscoveryBlock();
        String identityBlock = buildIdentityBlock(userContext);

        return basePrompt
                .replace("{{USER_INTENT}}", userIntent != null ? userIntent : "")
                .replace("{{DISCOVERY_BLOCK}}", discoveryBlock)
                .replace("{{IDENTITY_BLOCK}}", identityBlock);
    }

    public String getBasePrompt() {
        return loadBasePrompt();
    }

    private String loadBasePrompt() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/orchestrator-agent.md");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Orchestration Agent prompt", e);
        }
    }

    public List<Map<String, Object>> getDiscoveryList() {
        List<Deployment> deployments = deploymentRepository.findByEnvironment(Environment.PRODUCTION);

        return deployments.stream()
                .filter(Deployment::isActive)
                .map(d -> {
                    com.maverick.feature.domain.MfeApplicationVersion version = d.getVersion();
                    String mfeName = version.getApplication().getName();
                    String exposedModule = mfeName.equals("remote-profile") ? "./ProfileComponent" : "MainModule";

                    Map<String, Object> mfe = new java.util.HashMap<>();
                    mfe.put("remoteName", mfeName);
                    mfe.put("exposedModule", exposedModule);
                    mfe.put("capability", "profile.view"); // Simplified capability mapping
                    return mfe;
                })
                .collect(Collectors.toList());
    }

    private String buildDiscoveryBlock() {
        List<Map<String, Object>> mfeList = getDiscoveryList();
        String json = convertToJson(Map.of("registry", Map.of("discoveredMFEs", mfeList)));
        log.info("Prompt Discovery Block: {}", json);
        return json;
    }

    private String buildIdentityBlock(Map<String, Object> context) {
        Map<String, Object> identity = new java.util.HashMap<>();
        if (context != null) {
            identity.putAll(context);
        }

        // Add temporal context for general questions
        identity.put("currentTime", java.time.LocalDateTime.now().toString());

        if (identity.get("roles") == null) {
            identity.put("roles", List.of("USER"));
        }
        if (identity.get("tenantId") == null) {
            identity.put("tenantId", "default");
        }

        return convertToJson(identity);
    }

    private String convertToJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
