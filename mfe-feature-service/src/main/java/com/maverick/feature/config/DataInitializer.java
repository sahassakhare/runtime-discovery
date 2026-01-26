package com.maverick.feature.config;

import com.maverick.feature.domain.*;
import com.maverick.feature.repository.*;

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
                        MfeApplicationRepository appRepo,
                        MfeApplicationVersionRepository versionRepo,
                        MfeApplicationGroupRepository groupRepo,
                        MfeMetadataRepository metadataRepo,
                        DeploymentRepository deploymentRepo,
                        TenantRepository tenantRepo,
                        PolicyRepository policyRepo,
                        org.ff4j.FF4j ff4j,
                        org.springframework.jdbc.core.JdbcTemplate jdbcTemplate,
                        com.maverick.feature.service.GovernancePolicySyncService syncService,
                        MfeProperties properties) {

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
                        if (appRepo.count() == 0) {
                                // Fetch the first tenant (e.g., acme) to assign to the default group
                                Tenant defaultTenant = tenantRepo.findById("acme")
                                                .orElseThrow(() -> new RuntimeException("Default tenant not found"));

                                MfeApplicationGroup group = new MfeApplicationGroup();
                                group.setName("Default Group");
                                group.setTenant(defaultTenant); // Set mandatory tenant
                                group = groupRepo.save(group);

                                MfeApplication app = new MfeApplication();
                                app.setName("remote-profile");
                                app.setGroup(group);
                                app.setTags("profile,user");
                                app = appRepo.save(app);
                                System.out.println(">>> Seeded App: remote-profile (Group: Default Group) <<<");

                                String remoteEntryUrl = properties.getRemoteUrls().getOrDefault("remote-profile",
                                                "http://localhost:4201/remoteEntry.js");

                                MfeApplicationVersion v1 = new MfeApplicationVersion();
                                v1.setVersion("1.0.0");
                                v1.setApplication(app);
                                v1.setEnvironment("PRODUCTION");
                                v1.setLatest(true);
                                v1 = versionRepo.save(v1);

                                // Seed Metadata for v1
                                MfeMetadata m1 = new MfeMetadata();
                                m1.setApplicationVersion(v1);
                                m1.setName("remoteEntry");
                                m1.setValue(remoteEntryUrl);
                                metadataRepo.save(m1);

                                // Create Stable Deployment
                                Deployment d1 = new Deployment(v1, Environment.PRODUCTION, true);
                                deploymentRepo.save(d1);

                                MfeApplicationVersion v2 = new MfeApplicationVersion();
                                v2.setVersion("1.1.0-canary");
                                v2.setApplication(app);
                                v2.setEnvironment("STAGING");
                                v2.setLatest(false);
                                v2 = versionRepo.save(v2);

                                // Seed Metadata for v2
                                MfeMetadata m2 = new MfeMetadata();
                                m2.setApplicationVersion(v2);
                                m2.setName("remoteEntry");
                                m2.setValue(remoteEntryUrl);
                                metadataRepo.save(m2);

                                // Create Canary Deployment
                                deploymentRepo.save(new Deployment(v2, Environment.STAGING, true));
                                deploymentRepo.save(new Deployment(v2, Environment.DEVELOPMENT, true));

                                System.out.println(
                                                ">>> Database seeded with remote-profile (v1.0.0 Stable / v1.1.0-canary Staging & Dev) <<<");

                                // Seed remote-audit
                                MfeApplication auditApp = new MfeApplication();
                                auditApp.setName("remote-audit");
                                auditApp.setGroup(group);
                                auditApp = appRepo.save(auditApp);

                                MfeApplicationVersion vAudit = new MfeApplicationVersion();
                                vAudit.setVersion("2.0.0");
                                vAudit.setApplication(auditApp);
                                vAudit.setEnvironment("PRODUCTION");
                                vAudit.setLatest(true);
                                vAudit = versionRepo.save(vAudit);

                                MfeMetadata ma = new MfeMetadata();
                                ma.setApplicationVersion(vAudit);
                                ma.setName("remoteEntry");
                                ma.setValue("http://localhost:4205/remoteEntry.js");
                                metadataRepo.save(ma);

                                deploymentRepo.save(new Deployment(vAudit, Environment.PRODUCTION, true));
                                System.out.println(">>> Seeded App: remote-audit (v2.0.0 Stable) <<<");
                        }

                        // 5. Seed Policies
                        seedPolicies(policyRepo, syncService); // Updated call to include syncService

                        // 6. Seed Custom Hierarchy (Tenant 12345)
                        seedCustomHierarchy(tenantRepo, groupRepo, appRepo, versionRepo, metadataRepo, deploymentRepo);

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
                                                        "Centralized decision point unifying all protocols.",
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

        private void seedCustomHierarchy(TenantRepository tenantRepo, MfeApplicationGroupRepository groupRepo,
                        MfeApplicationRepository appRepo, MfeApplicationVersionRepository versionRepo,
                        MfeMetadataRepository metadataRepo, DeploymentRepository deploymentRepo) {

                String tenantId = "12345";
                if (tenantRepo.existsById(tenantId)) {
                        System.out.println(">>> Tenant " + tenantId + " already exists. Skipping custom seed. <<<");
                        return;
                }

                // 1. Create Tenant
                Tenant tenant = new Tenant(tenantId, "MyPortal Tenant");
                tenantRepo.save(tenant);
                System.out.println(">>> Seeded Tenant: " + tenantId + " <<<");

                // 2. Create Group
                MfeApplicationGroup group = new MfeApplicationGroup();
                group.setName("myportal");
                group.setTenant(tenant);
                group = groupRepo.save(group);
                System.out.println(">>> Seeded Group: myportal (Tenant: " + tenantId + ") <<<");

                // 3. Create Applications
                createApp(appRepo, versionRepo, metadataRepo, deploymentRepo, group, "shell", 5000);
                createApp(appRepo, versionRepo, metadataRepo, deploymentRepo, group, "admin", 4204); // Arbitrary port
                createApp(appRepo, versionRepo, metadataRepo, deploymentRepo, group, "intake", 4205); // Arbitrary port
                createApp(appRepo, versionRepo, metadataRepo, deploymentRepo, group, "profile", 4201); // Real port for
                                                                                                       // Remote Profile
        }

        private void createApp(MfeApplicationRepository appRepo, MfeApplicationVersionRepository versionRepo,
                        MfeMetadataRepository metadataRepo, DeploymentRepository deploymentRepo,
                        MfeApplicationGroup group, String appName, int port) {

                MfeApplication app = new MfeApplication();
                app.setName(appName);
                app.setGroup(group);
                app = appRepo.save(app);

                MfeApplicationVersion v1 = new MfeApplicationVersion();
                v1.setVersion("1.0.0");
                v1.setApplication(app);
                v1.setEnvironment("PRODUCTION");
                v1.setLatest(true);
                v1 = versionRepo.save(v1);

                MfeMetadata meta = new MfeMetadata();
                meta.setApplicationVersion(v1);
                meta.setName("remoteEntry");
                meta.setValue("http://localhost:" + port + "/remoteEntry.js");
                metadataRepo.save(meta);

                deploymentRepo.save(new Deployment(v1, Environment.PRODUCTION, true));
                System.out.println(">>> Seeded App: " + appName + " (Group: " + group.getName() + ") <<<");
        }
}
