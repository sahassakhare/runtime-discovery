package com.maverick.feature.config;

import com.maverick.feature.domain.Microfrontend;
import com.maverick.feature.domain.Version;
import com.maverick.feature.repository.MicrofrontendRepository;
import com.maverick.feature.repository.VersionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

        @Bean
        public CommandLineRunner initData(
                        com.maverick.feature.repository.MicrofrontendRepository mfeRepo,
                        com.maverick.feature.repository.VersionRepository versionRepo,
                        com.maverick.feature.repository.DeploymentRepository deploymentRepo,
                        com.maverick.feature.repository.TenantRepository tenantRepo,
                        com.maverick.feature.repository.PolicyRepository policyRepo,
                        org.ff4j.FF4j ff4j,
                        org.springframework.jdbc.core.JdbcTemplate jdbcTemplate,
                        com.maverick.feature.service.GovernancePolicySyncService syncService, // Added syncService
                        com.maverick.feature.config.MfeProperties properties) {
                return args -> {
                        System.out.println(
                                        ">>> FF4j Store Type: " + ff4j.getFeatureStore().getClass().getName() + " <<<");

                        // 1. Seed FF4j Features
                        seedFeatures(ff4j);

                        // 3. Verify Seeding
                        try {
                                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FF4J_FEATURES",
                                                Integer.class);
                                System.out.println(">>> VERIFICATION: FF4J_FEATURES row count = " + count + " <<<");
                        } catch (Exception e) {
                                System.err.println(">>> VERIFICATION FAILED: " + e.getMessage() + " <<<");
                        }

                        // 3.1 Seed Tenants (Enterprise)
                        if (tenantRepo.count() == 0) {
                                properties.getTenants().forEach(t -> {
                                        com.maverick.feature.domain.Tenant newTenant = new com.maverick.feature.domain.Tenant(
                                                        t.getId(),
                                                        t.getName());
                                        tenantRepo.save(newTenant);
                                });
                                System.out.println(">>> Seeded Tenants from Config: " + properties.getTenants().size()
                                                + " <<<");
                        }

                        // 4. Seed Microfrontends and Versions
                        if (mfeRepo.count() == 0) {
                                Microfrontend mfe = new Microfrontend("remote-profile");
                                mfe.setDescription("User Profile Management Remote");
                                mfe.setType("module-federation");
                                mfe.setFeatureGroupName("remote-profile");
                                mfe.setCreatedAt(LocalDateTime.now());
                                mfe.setUpdatedAt(LocalDateTime.now());
                                mfe = mfeRepo.save(mfe);
                                System.out.println(">>> Seeded MFE: remote-profile (Group: remote-profile) <<<");

                                System.out.println(">>> Seeded MFE: remote-profile (Group: remote-profile) <<<");

                                String remoteEntryUrl = properties.getRemoteUrls().getOrDefault("remote-profile",
                                                "http://localhost:4201/remoteEntry.js");

                                Version v1 = new Version();
                                v1.setVersion("1.0.0");
                                v1.setRemoteEntry(remoteEntryUrl);
                                v1.setMicrofrontend(mfe);
                                v1.setCreatedAt(LocalDateTime.now());
                                v1 = versionRepo.save(v1);

                                // Create Stable Deployment
                                com.maverick.feature.domain.Deployment d1 = new com.maverick.feature.domain.Deployment(
                                                v1,
                                                com.maverick.feature.domain.Environment.PRODUCTION,
                                                true);
                                deploymentRepo.save(d1);

                                Version v2 = new Version();
                                v2.setVersion("1.1.0-canary");
                                v2.setRemoteEntry(remoteEntryUrl);
                                v2.setMicrofrontend(mfe);
                                v2.setCreatedAt(LocalDateTime.now());
                                v2 = versionRepo.save(v2);

                                // Create Canary Deployment (Simulated as "production" env but handled via FF4j
                                // logic mostly, or separate env)
                                // For this architecture, we map it to "staging" to verify env scoping
                                com.maverick.feature.domain.Deployment stagingDeploy = new com.maverick.feature.domain.Deployment(
                                                v2,
                                                com.maverick.feature.domain.Environment.STAGING,
                                                true);
                                deploymentRepo.save(stagingDeploy);

                                // 4.3 Create Active Deployment for "development" (Canary)
                                com.maverick.feature.domain.Deployment devDeploy = new com.maverick.feature.domain.Deployment(
                                                v2,
                                                com.maverick.feature.domain.Environment.DEVELOPMENT, true);
                                deploymentRepo.save(devDeploy);

                                System.out
                                                .println(
                                                                ">>> Database seeded with remote-profile (v1.0.0 Stable / v1.1.0-canary Staging & Dev) <<<");

                                // Seed remote-audit for Policy Demo
                                Microfrontend auditMfe = new Microfrontend("remote-audit");
                                auditMfe.setDescription("Critical Audit Terminal");
                                auditMfe.setType("module-federation");
                                auditMfe.setCreatedAt(LocalDateTime.now());
                                auditMfe = mfeRepo.save(auditMfe);

                                Version vAudit = new Version();
                                vAudit.setVersion("2.0.0");
                                vAudit.setRemoteEntry("http://localhost:4205/remoteEntry.js");
                                vAudit.setMicrofrontend(auditMfe);
                                vAudit.setCreatedAt(LocalDateTime.now());
                                versionRepo.save(vAudit);

                                deploymentRepo.save(new com.maverick.feature.domain.Deployment(vAudit,
                                                com.maverick.feature.domain.Environment.PRODUCTION, true));
                                System.out.println(">>> Seeded MFE: remote-audit (v2.0.0 Stable) <<<");
                        }

                        // 5. Seed Policies
                        seedPolicies(policyRepo, syncService); // Updated call to include syncService
                        System.out.println("Data Seeding Completed Successfully.");
                };
        }

        private void seedPolicies(com.maverick.feature.repository.PolicyRepository policyRepo,
                        com.maverick.feature.service.GovernancePolicySyncService syncService) {
                policyRepo.deleteAll();
                System.out.println(">>> Seeding New MFE Governance Protocols from Filesystem... <<<");

                try {
                        List<com.maverick.feature.domain.Policy> policies = java.util.List.of(
                                        // 1. Discovery
                                        createPolicy(policyRepo, "POL-MFE-01", "Discovery Governance", "Discovery",
                                                        "Environment, version, channel, and domain governance.",
                                                        "discovery.rego"),
                                        // 2. Access
                                        createPolicy(policyRepo, "POL-MFE-02", "UI Authorization (ABAC)", "Security",
                                                        "Fine-grained UI-level access control based on user attributes.",
                                                        "access.rego"),
                                        // 3. Routing
                                        createPolicy(policyRepo, "POL-MFE-03", "Route Protection", "Security",
                                                        "Route ownership and administrative path protection.",
                                                        "routing.rego"),
                                        // 4. Compatibility
                                        createPolicy(policyRepo, "POL-MFE-04", "Runtime Compatibility", "Compatibility",
                                                        "Runtime dependency version matching (Angular/RxJS).",
                                                        "compatibility.rego"),
                                        // 5. Performance
                                        createPolicy(policyRepo, "POL-MFE-05", "Performance SLO", "Operational",
                                                        "Circuit-breaker and SLO enforcement (Error Rate/Latency).",
                                                        "performance.rego"),
                                        // 6. Feature Flags
                                        createPolicy(policyRepo, "POL-MFE-06", "Experimentation Guard", "Operational",
                                                        "Feature flags and bucketed experimentation rules.",
                                                        "feature_flags.rego"),
                                        // 7. UX Compliance
                                        createPolicy(policyRepo, "POL-MFE-07", "UX Governance", "UX",
                                                        "Design system and accessibility (WCAG) enforcement.",
                                                        "ux.rego"),
                                        // 8. Security Vetting (Demo)
                                        createPolicy(policyRepo, "POL-MFE-08", "Security Vetting", "Security",
                                                        "Deep context verification for sensitive protocols.",
                                                        "vetting.rego"),
                                        // 9. Unified Decision
                                        createPolicy(policyRepo, "POL-MFE-09", "Unified Decision Engine",
                                                        "Orchestration",
                                                        "🎯 Centralized decision point unifying all protocols.",
                                                        "decision.rego"));

                        System.out.println(">>> 9 Governance Protocols Seeded successfully! <<<");

                        // Trigger Hot-Sync to OPA on startup
                        syncService.syncAll(policies);

                } catch (Exception e) {
                        System.err.println(">>> FAILED to seed policies from filesystem: " + e.getMessage() + " <<<");
                        e.printStackTrace();
                }
        }

        private com.maverick.feature.domain.Policy createPolicy(com.maverick.feature.repository.PolicyRepository repo,
                        String code, String name, String category,
                        String description, String filename) throws java.io.IOException {
                String regoContent = readFile("../policies/mfe/" + filename);

                com.maverick.feature.domain.Policy policy = com.maverick.feature.domain.Policy.builder()
                                .code(code)
                                .name(name)
                                .category(category)
                                .type("REGO_OPA")
                                .description(description)
                                .isActive(true)
                                .enforcementLevel("BLOCK")
                                .configuration(regoContent)
                                .build();

                return repo.save(policy);
        }

        private String readFile(String path) throws java.io.IOException {
                java.nio.file.Path filePath = java.nio.file.Paths.get(path);
                if (!java.nio.file.Files.exists(filePath)) {
                        // Try absolute path if relative fails (for different environments)
                        filePath = java.nio.file.Paths
                                        .get("/Users/sahassakhare/Downloads/mfe-discovery-client/policies/mfe/"
                                                        + filePath.getFileName());
                }
                return java.nio.file.Files.readString(filePath);
        }

        private String quote(String s) {
                return "\"" + s + "\"";
        }

        private void seedFeatures(org.ff4j.FF4j ff4j) {
                // Force update for demo purposes (remove if exists)
                if (ff4j.getFeatureStore().exist("profile.new-ui")) {
                        ff4j.getFeatureStore().delete("profile.new-ui");
                }
                if (ff4j.getFeatureStore().exist("dark-mode")) {
                        ff4j.getFeatureStore().delete("dark-mode");
                }

                if (ff4j.getFeatureStore().exist("profile.canary")) {
                        ff4j.getFeatureStore().delete("profile.canary");
                }
                if (ff4j.getFeatureStore().exist("profile.routing")) {
                        ff4j.getFeatureStore().delete("profile.routing");
                }

                // 1. Feature Flag: Controls UI Visibility only (No routing)
                org.ff4j.core.Feature fNewUi = new org.ff4j.core.Feature("profile.new-ui");
                fNewUi.setEnable(true);
                fNewUi.setDescription("New Profile UI (Feature Toggle)");
                fNewUi.setGroup("remote-profile");
                ff4j.getFeatureStore().create(fNewUi);
                System.out.println(">>> Seeded FF4j feature: profile.new-ui (UI Toggle) <<<");

                // 2. Routing Flag: Unified Deployment Strategy
                // "This flag determines IF we route to a non-stable track, and WHICH one."
                org.ff4j.core.Feature fRouting = new org.ff4j.core.Feature("profile.routing");
                fRouting.setEnable(true);
                fRouting.setDescription("Deployment Routing Control");
                fRouting.setGroup("remote-profile");

                // Strategy Configuration (Best Practice: Configuration as Code)
                // trackMapping: Defines the Target Track (CANARY, BETA, EXPERIMENT)
                // trafficWeight: Defines the rollout percentage (0.0 to 1.0)
                fRouting.addProperty(new org.ff4j.property.PropertyString("trackMapping",
                                com.maverick.feature.domain.VariantType.CANARY.name()));
                fRouting.addProperty(new org.ff4j.property.PropertyDouble("trafficWeight", 0.2));

                ff4j.getFeatureStore().create(fRouting);
                System.out.println(">>> Seeded FF4j feature: profile.routing (Track: CANARY) <<<");

                org.ff4j.core.Feature fDark = new org.ff4j.core.Feature("dark-mode");
                fDark.setEnable(true);
                fDark.setDescription("Enable dark mode globally");
                fDark.setGroup("remote-profile");
                ff4j.getFeatureStore().create(fDark);
                System.out.println(">>> Seeded FF4j feature: dark-mode (Group: remote-profile) <<<");
                if (ff4j.getFeatureStore().exist("governance.enforcement")) {
                        ff4j.getFeatureStore().delete("governance.enforcement");
                }
                org.ff4j.core.Feature fGov = new org.ff4j.core.Feature("governance.enforcement");
                fGov.setEnable(true);
                fGov.setDescription("Global Policy Enforcement Kill-Switch");
                fGov.setGroup("governance");
                ff4j.getFeatureStore().create(fGov);
                System.out.println(">>> Seeded FF4j feature: governance.enforcement (Global Switch) <<<");
        }
}
