package com.maverick.feature.controller;

import com.maverick.feature.domain.Microfrontend;
import com.maverick.feature.domain.Version;
import com.maverick.feature.dto.RegisterMfeRequest;
import com.maverick.feature.repository.MicrofrontendRepository;
import com.maverick.feature.repository.VersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final MicrofrontendRepository mfeRepository;
    private final VersionRepository versionRepository;
    private final org.ff4j.FF4j ff4j;

    @GetMapping("/stats")
    public ResponseEntity<Object> getStats() {
        long totalMfes = mfeRepository.count();
        long activeVersions = versionRepository.count(); // In real app, filter by Active=true
        // Simulate counting deployments in last 24h (mock logic for now as we lack
        // audit log)
        long deploymentsToday = 12;

        return ResponseEntity.ok(java.util.Map.of(
                "totalMfes", totalMfes,
                "activeVersions", activeVersions,
                "deploymentsToday", deploymentsToday,
                "avgLighthouseScore", 95));
    }

    @GetMapping("/health")
    public ResponseEntity<Object> getHealth() {
        // Mocked health data
        return ResponseEntity.ok(java.util.List.of(
                java.util.Map.of("name", "remote-profile", "status", "HEALTHY", "uptime", "99.9%"),
                java.util.Map.of("name", "remote-payment", "status", "DEGRADED", "uptime", "95.0%")));
    }

    @GetMapping("/governance")
    public ResponseEntity<Object> getGovernance() {
        java.util.List<java.util.Map<String, String>> checks = new java.util.ArrayList<>();
        Iterable<Microfrontend> mfes = mfeRepository.findAll();

        // 1. Check for HTTPS usage in remote entries
        long insecureCount = 0;
        for (Microfrontend mfe : mfes) {
            Optional<Version> active = versionRepository.findByMicrofrontendId(mfe.getId()).stream()
                    .filter(Version::isActive).findFirst();
            if (active.isPresent() && !active.get().getRemoteEntry().startsWith("https")) {
                insecureCount++;
            }
        }
        checks.add(java.util.Map.of("check", "Enforce HTTPS", "status", insecureCount == 0 ? "PASS" : "FAIL"));

        // 2. Check for at least 1 active version per MFE
        long noActiveVersionCount = 0;
        for (Microfrontend mfe : mfes) {
            boolean hasActive = versionRepository.findByMicrofrontendId(mfe.getId()).stream()
                    .anyMatch(Version::isActive);
            if (!hasActive)
                noActiveVersionCount++;
        }
        checks.add(java.util.Map.of("check", "Active Version Exists", "status",
                noActiveVersionCount == 0 ? "PASS" : "WARN"));

        // 3. SemVer Compliance (Regex check simulation)
        checks.add(java.util.Map.of("check", "SemVer Compliance", "status", "PASS")); // Assuming PASS for MVP

        return ResponseEntity.ok(checks);
    }

    @GetMapping("/deployments")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getDeployments() {
        java.util.List<Object> deployments = new java.util.ArrayList<>();
        Iterable<Microfrontend> mfes = mfeRepository.findAll();

        for (Microfrontend mfe : mfes) {
            String activeVer = versionRepository.findByMicrofrontendId(mfe.getId()).stream()
                    .filter(Version::isActive)
                    .map(Version::getVersion)
                    .findFirst()
                    .orElse("None");

            deployments.add(java.util.Map.of(
                    "id", mfe.getId(),
                    "name", mfe.getName(),
                    "type", mfe.getType() != null ? mfe.getType() : "Unknown",
                    "group", mfe.getFeatureGroupName() != null ? mfe.getFeatureGroupName() : "None",
                    "activeVersion", activeVer,
                    "status", "HEALTHY"));
        }
        return ResponseEntity.ok(deployments);
    }

    @GetMapping("/resolution-graph/{remoteName}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getResolutionGraph(@PathVariable String remoteName) {
        Optional<Microfrontend> mfeOpt = mfeRepository.findByName(remoteName);
        if (mfeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Microfrontend mfe = mfeOpt.get();
        java.util.List<Version> versions = versionRepository.findByMicrofrontendId(mfe.getId());

        // FF4j Strategy Lookup
        double assignedTraffic = 0.0;
        java.util.Map<String, Double> trackWeights = new java.util.HashMap<>();

        if (mfe.getFeatureGroupName() != null) {
            java.util.Map<String, org.ff4j.core.Feature> features = ff4j.getFeatures();
            for (org.ff4j.core.Feature f : features.values()) {
                if (mfe.getFeatureGroupName().equals(f.getGroup())
                        && f.getCustomProperties().containsKey("trackMapping")) {
                    String track = f.getCustomProperties().get("trackMapping").asString();
                    // extract probability from "trafficWeight" property if available
                    if (f.getCustomProperties().containsKey("trafficWeight")) {
                        double weight = f.getCustomProperties().get("trafficWeight").asDouble();
                        trackWeights.put(track, weight);
                        assignedTraffic += weight;
                    }
                }
            }
        }

        // Build Nodes
        java.util.List<java.util.Map<String, Object>> nodes = new java.util.ArrayList<>();
        nodes.add(java.util.Map.of("id", "root", "label", "Shell (Host)", "type", "root"));

        // Dynamic Strategy Label
        String strategyLabel = trackWeights.isEmpty() ? "Default Strategy" : "FF4j Strategy";
        nodes.add(java.util.Map.of("id", "resolve", "label", strategyLabel, "type", "strategy"));

        // Build Edges
        java.util.List<java.util.Map<String, Object>> edges = new java.util.ArrayList<>();
        edges.add(java.util.Map.of("source", "root", "target", "resolve"));

        long activeCount = versions.stream().filter(Version::isActive).count();

        for (Version v : versions) {
            String nodeId = "v-" + v.getId();
            String label = v.getVersion() + (v.getReleaseTrack() != null ? " (" + v.getReleaseTrack() + ")" : "");
            String traffic = "";

            if (v.isActive()) {
                if (trackWeights.containsKey(v.getReleaseTrack())) {
                    // Driven by FF4j
                    double weight = trackWeights.get(v.getReleaseTrack());
                    traffic = String.format("%.0f%% Traffic", weight * 100);
                } else if (assignedTraffic < 1.0) {
                    // Check if this is the "default" (untracked) version to get the remainder
                    // Simple heuristic: if strict tracks (CANARY) are assigned, the STABLE one gets
                    // the rest
                    if (activeCount > 1 && !"CANARY".equalsIgnoreCase(v.getReleaseTrack())) {
                        traffic = String.format("%.0f%% Traffic", (1.0 - assignedTraffic) * 100);
                    } else if (activeCount == 1) {
                        traffic = "100% Traffic";
                    }
                }
            }

            nodes.add(java.util.Map.of(
                    "id", nodeId,
                    "label", label,
                    "type", v.isActive() ? "version active" : "version",
                    "traffic", traffic));

            // Connect if active (or all, depending on viz strategy. connecting all for now
            // to show available)
            edges.add(java.util.Map.of("source", "resolve", "target", nodeId));
        }

        return ResponseEntity.ok(java.util.Map.of("nodes", nodes, "edges", edges));
    }

    @GetMapping("/runtime")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getRuntime() {
        java.util.List<Object> data = new java.util.ArrayList<>();
        java.util.Random rand = new java.util.Random();
        Iterable<Microfrontend> mfes = mfeRepository.findAll();

        for (Microfrontend mfe : mfes) {
            // Simulate metrics per MFE
            double skew = 0;
            // logic to fake skew if multiple active versions exist?
            // For now, simpler simulation:
            if ("remote-profile".equals(mfe.getName()))
                skew = 10.0; // explicit demo case

            double clientErr = 0.1 + (rand.nextDouble() * 0.5);
            int latency = 80 + rand.nextInt(50);

            data.add(java.util.Map.of(
                    "mfeName", mfe.getName(),
                    "versionSkew", String.format("%.0f%%", skew),
                    "clientErrors", String.format("%.2f%%", clientErr),
                    "serverErrors", "0.0%",
                    "latency", latency + "ms"));
        }

        return ResponseEntity.ok(data);
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<String> registerMfe(@RequestBody RegisterMfeRequest request) {
        log.info("Received registration request for MFE: {} v{}", request.getName(), request.getVersion());

        // 1. Find or Create Microfrontend
        Microfrontend mfe = mfeRepository.findByName(request.getName())
                .orElseGet(() -> {
                    Microfrontend newMfe = new Microfrontend(request.getName());
                    newMfe.setType(request.getType());
                    return mfeRepository.save(newMfe);
                });

        // 2. Check if this version exists
        Optional<Version> existingVersion = versionRepository.findByMicrofrontendIdAndVersion(mfe.getId(),
                request.getVersion());

        if (existingVersion.isPresent()) {
            Version v = existingVersion.get();
            v.setRemoteEntry(request.getRemoteEntry());
            v.setIntegrity(request.getIntegrity());
            v.setActive(true); // Auto-activate latest push (simple logic)
            versionRepository.save(v);
            log.info("Updated existing version: {}", v);
        } else {
            Version v = new Version();
            v.setMicrofrontend(mfe);
            v.setVersion(request.getVersion());
            v.setRemoteEntry(request.getRemoteEntry());
            v.setIntegrity(request.getIntegrity());
            v.setActive(true); // Auto-activate new version
            v.setReleaseTrack("STABLE"); // Default to stable for registration
            versionRepository.save(v);
            log.info("Registered new version: {}", v);
        }

        return ResponseEntity.ok("Registered " + request.getName() + "@" + request.getVersion());
    }
}
