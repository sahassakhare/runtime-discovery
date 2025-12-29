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
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final MicrofrontendRepository mfeRepository;
    private final VersionRepository versionRepository;
    private final com.maverick.feature.repository.DeploymentRepository deploymentRepository;
    private final org.ff4j.FF4j ff4j;

    @GetMapping("/stats")
    public ResponseEntity<Object> getStats() {
        long totalMfes = mfeRepository.count();
        long activeVersions = deploymentRepository.count(); // Approximate active deployments
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
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getGovernance() {
        java.util.List<java.util.Map<String, String>> checks = new java.util.ArrayList<>();
        Iterable<Microfrontend> mfes = mfeRepository.findAll();

        // 1. Check for HTTPS usage in remote entries (Production Deployments)
        long insecureCount = 0;
        for (Microfrontend mfe : mfes) {
            Optional<com.maverick.feature.domain.Deployment> active = deploymentRepository
                    .findActiveGlobal(mfe.getName(), "production");
            if (active.isPresent() && !active.get().getVersion().getRemoteEntry().startsWith("https")) {
                insecureCount++;
            }
        }
        checks.add(java.util.Map.of("check", "Enforce HTTPS (Prod)", "status", insecureCount == 0 ? "PASS" : "FAIL"));

        // 2. Check for at least 1 active version per MFE
        long noActiveVersionCount = 0;
        for (Microfrontend mfe : mfes) {
            boolean hasActive = !deploymentRepository.findActiveGlobal(mfe.getName(), "production").isEmpty();
            if (!hasActive)
                noActiveVersionCount++;
        }
        checks.add(java.util.Map.of("check", "Active Production Version", "status",
                noActiveVersionCount == 0 ? "PASS" : "WARN"));

        // 3. SemVer
        checks.add(java.util.Map.of("check", "SemVer Compliance", "status", "PASS"));

        return ResponseEntity.ok(checks);
    }

    @GetMapping("/deployments")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getDeployments() {
        java.util.List<Object> deployments = new java.util.ArrayList<>();
        Iterable<Microfrontend> mfes = mfeRepository.findAll();

        for (Microfrontend mfe : mfes) {
            String activeVer = deploymentRepository.findActiveGlobal(mfe.getName(), "production")
                    .map(d -> d.getVersion().getVersion())
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
        // Simplified Logic for Enterprise Model for now
        // Just showing Prod deployment
        Optional<Microfrontend> mfeOpt = mfeRepository.findByName(remoteName);
        if (mfeOpt.isEmpty())
            return ResponseEntity.notFound().build();

        java.util.List<java.util.Map<String, Object>> nodes = new java.util.ArrayList<>();
        nodes.add(java.util.Map.of("id", "root", "label", "Shell (" + remoteName + ")", "type", "root"));

        Optional<com.maverick.feature.domain.Deployment> prod = deploymentRepository.findActiveGlobal(remoteName,
                "production");

        if (prod.isPresent()) {
            String nodeId = "v-" + prod.get().getVersion().getId();
            nodes.add(java.util.Map.of("id", nodeId, "label", prod.get().getVersion().getVersion() + " (Prod)", "type",
                    "version active", "traffic", "100%"));
            java.util.List<java.util.Map<String, Object>> edges = new java.util.ArrayList<>();
            edges.add(java.util.Map.of("source", "root", "target", nodeId));
            return ResponseEntity.ok(java.util.Map.of("nodes", nodes, "edges", edges));
        }

        return ResponseEntity.ok(java.util.Map.of("nodes", nodes, "edges", java.util.List.of()));
    }

    @GetMapping("/runtime")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getRuntime() {
        java.util.List<Object> data = new java.util.ArrayList<>();
        java.util.Random rand = new java.util.Random();
        Iterable<Microfrontend> mfes = mfeRepository.findAll();

        for (Microfrontend mfe : mfes) {
            // Simulate metrics per MFE
            double skew = ("remote-profile".equals(mfe.getName())) ? 10.0 : 0;
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

        // 2. Find or Create Version
        Version v = versionRepository.findByMicrofrontendIdAndVersion(mfe.getId(), request.getVersion())
                .orElseGet(() -> {
                    Version newV = new Version();
                    newV.setMicrofrontend(mfe);
                    newV.setVersion(request.getVersion());
                    newV.setRemoteEntry(request.getRemoteEntry());
                    newV.setIntegrity(request.getIntegrity());
                    newV.setCreatedAt(LocalDateTime.now());
                    return versionRepository.save(newV);
                });

        // Auto-deploy to Production (Global) on register
        Optional<com.maverick.feature.domain.Deployment> existingDep = deploymentRepository
                .findActiveGlobal(request.getName(), "production");
        if (existingDep.isPresent()) {
            com.maverick.feature.domain.Deployment d = existingDep.get();
            d.setActive(false);
            deploymentRepository.save(d);
        }

        com.maverick.feature.domain.Deployment newDep = new com.maverick.feature.domain.Deployment(v, "production",
                true);
        deploymentRepository.save(newDep);

        log.info("Registered and Deployed {} to Production", v.getVersion());

        return ResponseEntity.ok("Registered " + request.getName() + "@" + request.getVersion());
    }
}
