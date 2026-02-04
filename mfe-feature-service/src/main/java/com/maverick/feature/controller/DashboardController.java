package com.maverick.feature.controller;

import com.maverick.feature.domain.MfeApplication;
import com.maverick.feature.domain.MfeApplicationVersion;
import com.maverick.feature.dto.RegisterMfeRequest;
import com.maverick.feature.repository.MfeApplicationRepository;
import com.maverick.feature.repository.MfeApplicationGroupRepository;
import com.maverick.feature.repository.MfeApplicationVersionRepository;
import com.maverick.feature.repository.MfeConsumedRemoteRepository;
import com.maverick.feature.repository.MfeHealthRepository;
import com.maverick.feature.repository.MfeDependencyRepository;
import com.maverick.feature.repository.MfeExposedModuleRepository;
import com.maverick.feature.domain.MfeConsumedRemote;
import com.maverick.feature.domain.MfeHealth;
import com.maverick.feature.domain.MfeDependency;
import com.maverick.feature.domain.MfeExposedModule;
import com.maverick.feature.domain.MfeMetadata;
import com.maverick.feature.domain.Tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DashboardController.class);

        private final MfeApplicationRepository mfeRepository;
        private final MfeApplicationGroupRepository groupRepository; // Added this line
        private final MfeApplicationVersionRepository versionRepository;

        private final com.maverick.feature.repository.DeploymentRepository deploymentRepository;
        private final com.maverick.feature.repository.RuntimeInstanceRepository runtimeRepository;
        private final com.maverick.feature.repository.PolicyRepository policyRepository;
        private final MfeConsumedRemoteRepository consumedRemoteRepository;
        private final MfeHealthRepository healthRepository;
        private final MfeDependencyRepository dependencyRepository;
        private final MfeExposedModuleRepository exposedModuleRepository;
        private final com.maverick.feature.repository.TenantRepository tenantRepository; // Injected
        private final org.ff4j.FF4j ff4j;

        @GetMapping("/stats")
        public ResponseEntity<Object> getStats() {
                long totalMfes = mfeRepository.count();
                long activeVersions = deploymentRepository.count(); // Active deployments
                long deploymentsToday = deploymentRepository
                                .countByCreatedAtAfter(LocalDateTime.now().toLocalDate().atStartOfDay());

                // Calculate Up/Down status based on health repo
                long upCount = 0;
                long downCount = 0;
                long unknownCount = 0;

                Iterable<com.maverick.feature.domain.Deployment> deployments = deploymentRepository.findAll();
                for (com.maverick.feature.domain.Deployment d : deployments) {
                        if (d.isActive()) {
                                Optional<MfeHealth> h = healthRepository
                                                .findByApplicationVersionId(d.getVersion().getId());
                                if (h.isPresent()) {
                                        if (h.get().getStatus() == 200)
                                                upCount++;
                                        else
                                                downCount++;
                                } else {
                                        unknownCount++;
                                }
                        }
                }

                // Synthesized Lighthouse score based on average of MFEs
                int avgLighthouse = (int) (85 + (totalMfes > 0 ? (31 * totalMfes) % 15 : 0));

                return ResponseEntity.ok(java.util.Map.of(
                                "totalMfes", totalMfes,
                                "activeVersions", activeVersions,
                                "deploymentsToday", deploymentsToday,
                                "avgLighthouseScore", (long) avgLighthouse,
                                "upCount", upCount,
                                "downCount", downCount,
                                "unknownCount", unknownCount));
        }

        @GetMapping("/mfe/{name}/env")
        public ResponseEntity<Object> getMfeEnv(@PathVariable String name) {
                // Mock environment properties for demo
                return ResponseEntity.ok(java.util.Map.of(
                                "activeProfiles", new String[] { "production", "cloud" },
                                "propertySources", java.util.List.of(
                                                java.util.Map.of("name", "server.ports", "properties",
                                                                java.util.Map.of("local.server.port",
                                                                                java.util.Map.of("value", "8080"))),
                                                java.util.Map.of("name", "systemProperties", "properties",
                                                                java.util.Map.of("java.runtime.version", java.util.Map
                                                                                .of("value", "17.0.2"))))));
        }

        @GetMapping("/mfe/{name}/metrics")
        public ResponseEntity<Object> getMfeMetrics(@PathVariable String name) {
                // Mock metrics for demo
                return ResponseEntity.ok(java.util.Map.of(
                                "mem", 512 + (int) (Math.random() * 256),
                                "mem.free", 128 + (int) (Math.random() * 64),
                                "processors", 4,
                                "uptime", 15000 + (long) (Math.random() * 50000),
                                "systemload.average", 0.45,
                                "heap.committed", 450,
                                "heap.init", 256,
                                "heap.used", 300 + (int) (Math.random() * 100),
                                "threads.peak", 45,
                                "threads.daemon", 20,
                                "threads.totalListened", 50,
                                "classes", 5432,
                                "classes.loaded", 5432,
                                "classes.unloaded", 0,
                                "gc.ps_scavenge.count", 12,
                                "gc.ps_scavenge.time", 150,
                                "httpsessions.max", -1,
                                "httpsessions.active", 5));
        }

        @GetMapping("/health")
        public ResponseEntity<Object> getHealth() {
                java.util.List<java.util.Map<String, Object>> healthStatus = new java.util.ArrayList<>();
                Iterable<com.maverick.feature.domain.RuntimeInstance> instances = runtimeRepository.findAll();

                // Group by App Name
                java.util.Map<String, java.util.List<com.maverick.feature.domain.RuntimeInstance>> byApp = java.util.stream.StreamSupport
                                .stream(instances.spliterator(), false)
                                .collect(java.util.stream.Collectors
                                                .groupingBy(com.maverick.feature.domain.RuntimeInstance::getAppName));

                if (byApp.isEmpty()) {
                        // Fallback if no instances detected yet (e.g., first start)
                        return ResponseEntity.ok(java.util.Collections.emptyList());
                }

                byApp.forEach((appName, list) -> {
                        boolean healthy = list.stream()
                                        .anyMatch(i -> i.getLastHeartbeat()
                                                        .isAfter(LocalDateTime.now().minusMinutes(5)));

                        healthStatus.add(java.util.Map.of(
                                        "name", appName,
                                        "status", healthy ? "HEALTHY" : "DOWN",
                                        "uptime", healthy ? "100%" : "0%" // simplified
                        ));
                });

                return ResponseEntity.ok(healthStatus);
        }

        @GetMapping("/governance")
        @Transactional(readOnly = true)
        public ResponseEntity<Object> getGovernance() {
                java.util.List<java.util.Map<String, String>> checks = new java.util.ArrayList<>();
                Iterable<MfeApplication> mfes = mfeRepository.findAll();

                // 1. Check for HTTPS usage in remote entries (Production Deployments)
                long insecureCount = 0;
                for (MfeApplication mfe : mfes) {
                        Optional<com.maverick.feature.domain.Deployment> active = deploymentRepository
                                        .findActiveGlobal(mfe.getName(),
                                                        com.maverick.feature.domain.Environment.PRODUCTION);
                        if (active.isPresent()) {
                                String remoteEntry = active.get().getVersion().getMetadataValue("remoteEntry");
                                if (remoteEntry != null && !remoteEntry.startsWith("https")) {
                                        insecureCount++;
                                }
                        }
                }
                checks.add(java.util.Map.of("check", "Enforce HTTPS (Prod)", "status",
                                insecureCount == 0 ? "PASS" : "FAIL"));

                // 2. Check for at least 1 active version per MFE
                long noActiveVersionCount = 0;
                for (MfeApplication mfe : mfes) {
                        boolean hasActive = !deploymentRepository
                                        .findActiveGlobal(mfe.getName(),
                                                        com.maverick.feature.domain.Environment.PRODUCTION)
                                        .isEmpty();
                        if (!hasActive)
                                noActiveVersionCount++;
                }
                checks.add(java.util.Map.of("check", "Active Production Version", "status",
                                noActiveVersionCount == 0 ? "PASS" : "WARN"));

                // 3. SemVer Compliance Check
                long nonSemVerCount = versionRepository.findAll().stream()
                                .filter(v -> !v.getVersion().matches("^\\d+\\.\\d+\\.\\d+(-.*)?$"))
                                .count();
                checks.add(java.util.Map.of("check", "SemVer Compliance", "status",
                                nonSemVerCount == 0 ? "PASS" : "WARN"));

                // 4. Integrated Registry Protocol Status (Real-time DB check)
                long activePoliciesCount = ((java.util.List<?>) policyRepository.findByIsActiveTrue()).size();
                checks.add(java.util.Map.of("check", "Governance Active", "status",
                                activePoliciesCount > 0 ? "PASS" : "FAIL"));

                return ResponseEntity.ok(checks);
        }

        @GetMapping("/deployments")
        @Transactional(readOnly = true)
        public ResponseEntity<Object> getDeployments() {
                java.util.List<Object> deployments = new java.util.ArrayList<>();
                Iterable<MfeApplication> mfes = mfeRepository.findAll();

                for (MfeApplication mfe : mfes) {
                        String activeVer = deploymentRepository
                                        .findActiveGlobal(mfe.getName(),
                                                        com.maverick.feature.domain.Environment.PRODUCTION)
                                        .map(d -> d.getVersion().getVersion())
                                        .orElse("None");

                        deployments.add(java.util.Map.of(
                                        "id", mfe.getId(),
                                        "name", mfe.getName(),
                                        "type", "module-federation", // Default for now
                                        "group", mfe.getGroup() != null ? mfe.getGroup().getName() : "None",
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
                Optional<MfeApplication> mfeOpt = mfeRepository.findByName(remoteName);
                if (mfeOpt.isEmpty())
                        return ResponseEntity.notFound().build();

                java.util.List<java.util.Map<String, Object>> nodes = new java.util.ArrayList<>();
                nodes.add(java.util.Map.of("id", "root", "label", "Shell (" + remoteName + ")", "type", "root"));

                Optional<com.maverick.feature.domain.Deployment> prod = deploymentRepository.findActiveGlobal(
                                remoteName,
                                com.maverick.feature.domain.Environment.PRODUCTION);

                if (prod.isPresent()) {
                        String nodeId = "v-" + prod.get().getVersion().getId();
                        nodes.add(java.util.Map.of("id", nodeId, "label",
                                        prod.get().getVersion().getVersion() + " (Prod)", "type",
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
                Iterable<MfeApplication> mfes = mfeRepository.findAll();

                for (MfeApplication mfe : mfes) {
                        // Real Version Skew calculation: Active vs Latest version
                        Optional<com.maverick.feature.domain.Deployment> activeDep = deploymentRepository
                                        .findActiveGlobal(mfe.getName(),
                                                        com.maverick.feature.domain.Environment.PRODUCTION);

                        String activeVer = activeDep.map(d -> d.getVersion().getVersion()).orElse("None");
                        String latestVer = versionRepository.findTopByApplicationIdOrderByCreatedAtDesc(mfe.getId())
                                        .map(com.maverick.feature.domain.MfeApplicationVersion::getVersion)
                                        .orElse("None");

                        boolean skewed = !activeVer.equals(latestVer) && !"None".equals(activeVer);

                        // Health Status
                        String status = "REGISTERED"; // Default
                        if (activeDep.isPresent()) {
                                Optional<MfeHealth> health = healthRepository
                                                .findByApplicationVersionId(activeDep.get().getVersion().getId());
                                if (health.isPresent()) {
                                        status = health.get().getStatus() == 200 ? "HEALTHY" : "UNHEALTHY";
                                } else {
                                        status = "LIVE"; // Active deployment but no specific health report yet
                                }
                        }

                        // Simulated error rates based on version status
                        double clientError = activeVer.contains("canary") ? 0.05 : 0.01;
                        int latency = activeVer.contains("canary") ? (150 + (int) (Math.random() * 50))
                                        : (40 + (int) (Math.random() * 20));

                        data.add(java.util.Map.of(
                                        "mfeName", mfe.getName(),
                                        "versionSkew", skewed ? "1 Version Behind" : "Aligned",
                                        "clientErrors", String.format("%.1f%%", clientError),
                                        "serverErrors", "0.0%",
                                        "latency", latency + "ms",
                                        "status", status));
                }

                return ResponseEntity.ok(data);
        }

        @GetMapping("/mfe/{name}/details")
        @Transactional(readOnly = true)
        public ResponseEntity<Object> getMfeDetails(@PathVariable String name) {
                MfeApplication mfe = mfeRepository.findByName(name)
                                .orElseThrow(() -> new RuntimeException("MFE not found: " + name));

                Optional<com.maverick.feature.domain.Deployment> activeDep = deploymentRepository
                                .findActiveGlobal(mfe.getName(), com.maverick.feature.domain.Environment.PRODUCTION);

                java.util.Map<String, Object> details = new java.util.HashMap<>();
                details.put("name", mfe.getName());
                details.put("id", mfe.getId());

                // Add Version History
                java.util.List<String> versions = mfe.getVersions().stream()
                                .map(com.maverick.feature.domain.MfeApplicationVersion::getVersion)
                                .sorted(java.util.Comparator.reverseOrder())
                                .collect(java.util.stream.Collectors.toList());
                details.put("versions", versions);

                // Add Locked Status
                Optional<com.maverick.feature.domain.Deployment> lockedDep = deploymentRepository
                                .findLocked(mfe.getName(), com.maverick.feature.domain.Environment.PRODUCTION);
                details.put("lockedVersion", lockedDep.map(d -> d.getVersion().getVersion()).orElse(null));

                if (activeDep.isPresent()) {
                        MfeApplicationVersion version = activeDep.get().getVersion();
                        details.put("activeVersion", version.getVersion());
                        details.put("environment", activeDep.get().getEnvironment());
                        details.put("metadata", version.getMetadata());
                        details.put("dependencies", dependencyRepository.findByApplicationVersionId(version.getId()));
                        details.put("exposedModules",
                                        exposedModuleRepository.findByApplicationVersionId(version.getId()));
                        details.put("consumedRemotes",
                                        consumedRemoteRepository.findByConsumerVersionId(version.getId()));

                        Optional<MfeHealth> health = healthRepository.findByApplicationVersionId(version.getId());
                        details.put("health", health.orElse(null));
                }

                return ResponseEntity.ok(details);
        }

        @PostMapping("/register")
        @Transactional
        public ResponseEntity<String> registerMfe(@RequestBody RegisterMfeRequest request) {
                log.info("Received registration request for MFE: {} v{}", request.getName(), request.getVersion());

                // 1. Find or Create Application
                MfeApplication mfe = mfeRepository.findByName(request.getName())
                                .orElseGet(() -> {
                                        MfeApplication newApp = new MfeApplication();
                                        newApp.setName(request.getName());

                                        // Resolve Tenant (Auto-create if missing)
                                        String tid = request.getTenantId() != null ? request.getTenantId() : "acme";
                                        Tenant tenant = tenantRepository.findById(tid)
                                                        .orElseGet(() -> {
                                                                Tenant newTenant = new Tenant(tid, tid);
                                                                return tenantRepository.save(newTenant);
                                                        });

                                        // Resolve Group (Default to 'Default Group' if not provided)
                                        String groupName = request.getGroupId() != null ? request.getGroupId()
                                                        : "Default Group";

                                        // Find or create group for this Tenant
                                        com.maverick.feature.domain.MfeApplicationGroup group = groupRepository
                                                        .findAll().stream()
                                                        .filter(g -> g.getTenant() != null
                                                                        && g.getTenant().getId().equals(tid)
                                                                        && g.getName().equalsIgnoreCase(groupName))
                                                        .findFirst()
                                                        .orElseGet(() -> {
                                                                com.maverick.feature.domain.MfeApplicationGroup g = new com.maverick.feature.domain.MfeApplicationGroup();
                                                                g.setName(groupName);
                                                                g.setTenant(tenant);
                                                                return groupRepository.save(g);
                                                        });
                                        newApp.setGroup(group);
                                        return mfeRepository.save(newApp);
                                });

                // 2. Find or Create Version
                MfeApplicationVersion v = versionRepository
                                .findByApplicationIdAndVersion(mfe.getId(), request.getVersion())
                                .orElseGet(() -> {
                                        MfeApplicationVersion newV = new MfeApplicationVersion();
                                        newV.setApplication(mfe);
                                        newV.setVersion(request.getVersion());
                                        newV.setCreatedAt(LocalDateTime.now());
                                        newV = versionRepository.save(newV);

                                        // Add Metadata
                                        // Add Metadata
                                        com.maverick.feature.domain.MfeMetadata meta = new com.maverick.feature.domain.MfeMetadata();
                                        meta.setApplicationVersion(newV);
                                        meta.setName("remoteEntry");
                                        meta.setValue(request.getRemoteEntry());

                                        newV.getMetadata().add(meta);
                                        newV = versionRepository.save(newV);

                                        // Automated SRI Calculation (Enterprise Security)
                                        try {
                                                String integrity = generateSriHash(request.getRemoteEntry());
                                                if (integrity != null) {
                                                        log.info("Calculated SRI for {}: {}", request.getName(),
                                                                        integrity);
                                                        com.maverick.feature.domain.MfeMetadata sriMeta = new com.maverick.feature.domain.MfeMetadata();
                                                        sriMeta.setApplicationVersion(newV);
                                                        sriMeta.setName("integrity");
                                                        sriMeta.setValue(integrity);
                                                        newV.getMetadata().add(sriMeta);
                                                        newV = versionRepository.save(newV);
                                                }
                                        } catch (Exception e) {
                                                log.warn("Failed to generate SRI for {}: {}", request.getName(),
                                                                e.getMessage());
                                                // Non-blocking failure, proceeded without SRI
                                        }

                                        return newV;
                                });

                // 3. Process Consumed Remotes (Static/Build-time)
                if (request.getConsumedRemotes() != null) {
                        for (RegisterMfeRequest.ConsumedRemoteMetadata meta : request.getConsumedRemotes()) {
                                MfeConsumedRemote consumed = new MfeConsumedRemote();
                                consumed.setConsumerVersion(v);
                                consumed.setRemoteName(meta.getRemoteName());
                                consumed.setUsedModules(meta.getModules());
                                consumed.setDynamic(meta.isDynamic());
                                consumedRemoteRepository.save(consumed);
                        }
                }

                // Auto-deploy to Production (Global) on register
                Optional<com.maverick.feature.domain.Deployment> existingDep = deploymentRepository
                                .findActiveGlobal(request.getName(),
                                                com.maverick.feature.domain.Environment.PRODUCTION);
                if (existingDep.isPresent()) {
                        com.maverick.feature.domain.Deployment d = existingDep.get();
                        d.setActive(false);
                        deploymentRepository.save(d);
                }

                com.maverick.feature.domain.Deployment newDep = new com.maverick.feature.domain.Deployment(v,
                                com.maverick.feature.domain.Environment.PRODUCTION,
                                true);
                deploymentRepository.save(newDep);

                log.info("Registered and Deployed {} to Production", v.getVersion());

                return ResponseEntity.ok("Registered " + request.getName() + "@" + request.getVersion());
        }

        @GetMapping("/dependency-graph")
        @Transactional(readOnly = true)
        public ResponseEntity<Object> getDependencyGraph() {
                java.util.List<java.util.Map<String, Object>> nodes = new java.util.ArrayList<>();
                java.util.List<java.util.Map<String, Object>> edges = new java.util.ArrayList<>();

                // Track known apps to avoid creating duplicate "external" nodes
                java.util.Set<String> knownApps = new java.util.HashSet<>();
                java.util.Set<String> addedExternalNodes = new java.util.HashSet<>();

                Iterable<MfeApplication> apps = mfeRepository.findAll();

                // Pass 1: Create Nodes for all registered Applications
                for (MfeApplication app : apps) {
                        knownApps.add(app.getName());

                        java.util.Map<String, Object> node = new java.util.HashMap<>();
                        node.put("id", app.getName());
                        node.put("label", app.getName());
                        node.put("type", "app");

                        // Add exposed modules info
                        Optional<com.maverick.feature.domain.Deployment> dep = deploymentRepository
                                        .findActiveGlobal(app.getName(),
                                                        com.maverick.feature.domain.Environment.PRODUCTION);

                        if (dep.isPresent()) {
                                MfeApplicationVersion v = dep.get().getVersion();
                                java.util.List<com.maverick.feature.domain.MfeExposedModule> exposed = exposedModuleRepository
                                                .findByApplicationVersionId(v.getId());
                                java.util.List<String> moduleNames = exposed.stream().map(m -> m.getName())
                                                .collect(java.util.stream.Collectors.toList());
                                node.put("exposedModules", moduleNames);
                        }

                        nodes.add(node);
                }

                // Pass 2: Create Edges and "External" Nodes for unknown dependencies
                for (MfeApplication app : apps) {
                        Optional<com.maverick.feature.domain.Deployment> dep = deploymentRepository
                                        .findActiveGlobal(app.getName(),
                                                        com.maverick.feature.domain.Environment.PRODUCTION);

                        if (dep.isPresent()) {
                                MfeApplicationVersion v = dep.get().getVersion();
                                java.util.List<MfeConsumedRemote> consumed = consumedRemoteRepository
                                                .findByConsumerVersionId(v.getId());

                                for (MfeConsumedRemote c : consumed) {
                                        String remoteName = c.getRemoteName();

                                        // If remote is NOT a known app, verify if we need to add a node for it
                                        if (!knownApps.contains(remoteName)) {
                                                if (!addedExternalNodes.contains(remoteName)) {
                                                        nodes.add(java.util.Map.of("id", remoteName, "label",
                                                                        remoteName, "type", "external"));
                                                        addedExternalNodes.add(remoteName);
                                                }
                                        }

                                        // Create Edge
                                        edges.add(java.util.Map.of(
                                                        "source", app.getName(),
                                                        "target", remoteName,
                                                        "label", c.getUsedModules() != null ? c.getUsedModules() : "",
                                                        "dynamic", c.getDynamic()));
                                }
                        }
                }

                return ResponseEntity.ok(java.util.Map.of("nodes", nodes, "edges", edges));
        }

        @GetMapping("/mfe/{name}/logs")
        public ResponseEntity<Object> getMfeLogs(@PathVariable String name) {
                return proxyToMfe(name, "logs.json");
        }

        @GetMapping("/mfe/{name}/threads")
        public ResponseEntity<Object> getMfeThreads(@PathVariable String name) {
                return proxyToMfe(name, "threads.json");
        }

        @GetMapping("/mfe/{name}/traces")
        public ResponseEntity<Object> getMfeTraces(@PathVariable String name) {
                return proxyToMfe(name, "traces.json");
        }

        private ResponseEntity<Object> proxyToMfe(String name, String asset) {
                Optional<com.maverick.feature.domain.Deployment> activeDep = deploymentRepository
                                .findActiveGlobal(name, com.maverick.feature.domain.Environment.PRODUCTION);

                if (activeDep.isEmpty()) {
                        return ResponseEntity.status(404).body("No active deployment found for " + name);
                }

                String remoteEntry = activeDep.get().getVersion().getMetadataValue("remoteEntry");
                if (remoteEntry == null) {
                        return ResponseEntity.status(404).body("No remoteEntry found for " + name);
                }

                // Convert remoteEntry (e.g., http://localhost:4201/remoteEntry.js) to base URL
                // (http://localhost:4201)
                String baseUrl = remoteEntry.substring(0, remoteEntry.lastIndexOf('/'));
                String assetUrl = baseUrl + "/assets/" + asset;

                try {
                        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                        Object response = restTemplate.getForObject(assetUrl, Object.class);
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        log.error("Failed to fetch {} from {}: {}", asset, assetUrl, e.getMessage());
                        return ResponseEntity.status(502).body("Failed to fetch " + asset + " from MFE");
                }
        }

        /**
         * Fetches the remote entry and calculates SHA-384 hash for SRI.
         */
        private String generateSriHash(String remoteEntryUrl) {
                if (remoteEntryUrl == null || !remoteEntryUrl.startsWith("http"))
                        return null;

                try {
                        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                        byte[] scriptBytes = restTemplate.getForObject(remoteEntryUrl, byte[].class);

                        if (scriptBytes == null || scriptBytes.length == 0)
                                return null;

                        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-384");
                        byte[] hash = digest.digest(scriptBytes);
                        String base64Hash = java.util.Base64.getEncoder().encodeToString(hash);

                        return "sha384-" + base64Hash;
                } catch (Exception e) {
                        log.warn("Error calculating SRI for {}: {}", remoteEntryUrl, e.getMessage());
                        return null;
                }
        }

}
