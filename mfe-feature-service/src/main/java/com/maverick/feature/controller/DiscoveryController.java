package com.maverick.feature.controller;

import com.maverick.feature.domain.Microfrontend;
import com.maverick.feature.domain.Version;
import com.maverick.feature.dto.ResolutionRequest;
import com.maverick.feature.dto.ResolutionResponse;
import com.maverick.feature.repository.MicrofrontendRepository;
import com.maverick.feature.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ff4j.FF4j;
import io.openfeature.sdk.MutableContext;
import io.openfeature.sdk.OpenFeatureAPI;
import org.ff4j.FF4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DiscoveryController {

    private final MicrofrontendRepository mfeRepository;
    private final VersionRepository versionRepository;
    private final FF4j ff4j;
    private final OpenFeatureAPI openFeatureAPI;

    @javax.annotation.PostConstruct
    public void init() {
        System.out
                .println(">>> INJECTED FF4j Bean into DiscoveryController: " + System.identityHashCode(ff4j) + " <<<");
    }

    @PostMapping("/resolve")
    public ResponseEntity<ResolutionResponse> resolve(@RequestBody ResolutionRequest request) {
        String remoteName = request.getRemoteName();
        log.info("Resolving remote: {}", remoteName);

        // 1. Lookup MFE and Versions
        Optional<Microfrontend> mfeOpt = mfeRepository.findByName(remoteName);
        if (mfeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Microfrontend mfe = mfeOpt.get();
        List<Version> versions = versionRepository.findByMicrofrontendId(mfe.getId());

        // 2. Determine Strategy via FF4j Metadata (Release Tracks)
        Map<String, Object> flags = new HashMap<>();

        // Build OpenFeature Context
        MutableContext evaluationContext = new MutableContext();
        if (request.getContext() != null) {
            request.getContext().forEach((k, v) -> evaluationContext.add(k, String.valueOf(v)));
        }

        String targetTrack = "STABLE"; // Default
        String fallbackTrack = "STABLE"; // Default fallback
        String groupName = mfe.getFeatureGroupName();

        if (groupName != null && !groupName.isEmpty()) {
            Map<String, org.ff4j.core.Feature> groupFeatures = ff4j.getFeatureStore().readGroup(groupName);
            for (org.ff4j.core.Feature f : groupFeatures.values()) {
                String fid = f.getUid();
                // OpenFeature Evaluation
                boolean isEnabled = openFeatureAPI.getClient().getBooleanValue(fid, false, evaluationContext);
                flags.put(fid, isEnabled);

                // Scalable: Check if this flag maps to a specific release track
                if (isEnabled && f.getCustomProperties().containsKey("trackMapping")) {
                    String mappedTrack = f.getCustomProperties().get("trackMapping").asString();
                    if (isHigherPriority(mappedTrack, targetTrack)) {
                        targetTrack = mappedTrack;

                        // Check for optional fallback override
                        if (f.getCustomProperties().containsKey("fallbackTrack")) {
                            fallbackTrack = f.getCustomProperties().get("fallbackTrack").asString();
                        }
                    }
                }
            }
        }

        // 3. Selection Logic
        final String finalTargetTrack = targetTrack;
        Optional<Version> selectedVersionOpt = versions.stream()
                .filter(v -> v.isActive() && finalTargetTrack.equalsIgnoreCase(v.getReleaseTrack()))
                .findFirst();

        // Fallback to designated fallback track if selected not found
        if (selectedVersionOpt.isEmpty() && !fallbackTrack.equalsIgnoreCase(targetTrack)) {
            String finalFallbackTrack = fallbackTrack;
            selectedVersionOpt = versions.stream()
                    .filter(v -> v.isActive() && finalFallbackTrack.equalsIgnoreCase(v.getReleaseTrack()))
                    .findFirst();
            targetTrack = fallbackTrack;
        }

        if (selectedVersionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Version selected = selectedVersionOpt.get();

        // 4. Build Response
        ResolutionResponse.ResolutionResponseBuilder responseBuilder = ResolutionResponse.builder()
                .remoteName(remoteName)
                .selected(toRemoteVersion(selected))
                .cacheTtlSeconds(60);

        ResolutionResponse.ResolutionContext.ResolutionContextBuilder contextBuilder = ResolutionResponse.ResolutionContext
                .builder()
                .flags(flags);

        // Always attempt to include fallback version for resiliency
        // even if we are on STABLE (allows client-side retries or alternative stable
        // versions)
        String finalFallbackTrack = fallbackTrack;
        versions.stream()
                .filter(v -> v.isActive() && finalFallbackTrack.equalsIgnoreCase(v.getReleaseTrack()))
                .findFirst()
                .ifPresent(fb -> {
                    // Start of block
                    responseBuilder.fallback(toRemoteVersion(fb));
                });

        // Always include variant info if we are not on STABLE
        if (!"STABLE".equalsIgnoreCase(targetTrack)) {
            contextBuilder.variant(ResolutionResponse.Variant.builder()
                    .name(targetTrack.toLowerCase())
                    .type(targetTrack.toLowerCase())
                    .build());
        }

        responseBuilder.resolutionContext(contextBuilder.build());

        return ResponseEntity.ok(responseBuilder.build());
    }

    private boolean isHigherPriority(String newTrack, String currentTrack) {
        Map<String, Integer> priority = Map.of("CANARY", 3, "BETA", 2, "STABLE", 1);
        return priority.getOrDefault(newTrack.toUpperCase(), 0) > priority.getOrDefault(currentTrack.toUpperCase(), 0);
    }

    private ResolutionResponse.RemoteVersion toRemoteVersion(Version version) {
        return ResolutionResponse.RemoteVersion.builder()
                .version(version.getVersion())
                .remoteEntry(version.getRemoteEntry())
                .integrity(version.getIntegrity())
                .build();
    }
}
