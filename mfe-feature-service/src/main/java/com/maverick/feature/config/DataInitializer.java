package com.maverick.feature.config;

import com.maverick.feature.domain.Microfrontend;
import com.maverick.feature.domain.Version;
import com.maverick.feature.repository.MicrofrontendRepository;
import com.maverick.feature.repository.VersionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

            com.maverick.feature.config.MfeProperties properties) {
        return args -> {
            System.out.println(">>> FF4j Store Type: " + ff4j.getFeatureStore().getClass().getName() + " <<<");

            // 1. Seed FF4j Features
            seedFeatures(ff4j);

            // 3. Verify Seeding
            try {
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FF4J_FEATURES", Integer.class);
                System.out.println(">>> VERIFICATION: FF4J_FEATURES row count = " + count + " <<<");
            } catch (Exception e) {
                System.err.println(">>> VERIFICATION FAILED: " + e.getMessage() + " <<<");
            }

            // 3.1 Seed Tenants (Enterprise)
            if (tenantRepo.count() == 0) {
                properties.getTenants().forEach(t -> {
                    com.maverick.feature.domain.Tenant newTenant = new com.maverick.feature.domain.Tenant(t.getId(),
                            t.getName());
                    tenantRepo.save(newTenant);
                });
                System.out.println(">>> Seeded Tenants from Config: " + properties.getTenants().size() + " <<<");
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
                com.maverick.feature.domain.Deployment d1 = new com.maverick.feature.domain.Deployment(v1,
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
                com.maverick.feature.domain.Deployment stagingDeploy = new com.maverick.feature.domain.Deployment(v2,
                        com.maverick.feature.domain.Environment.STAGING,
                        true);
                deploymentRepo.save(stagingDeploy);

                // 4.3 Create Active Deployment for "development" (Canary)
                com.maverick.feature.domain.Deployment devDeploy = new com.maverick.feature.domain.Deployment(v2,
                        com.maverick.feature.domain.Environment.DEVELOPMENT, true);
                deploymentRepo.save(devDeploy);

                System.out
                        .println(
                                ">>> Database seeded with remote-profile (v1.0.0 Stable / v1.1.0-canary Staging & Dev) <<<");
            }

            // 5. Seed Policies
            seedPolicies(policyRepo);
        };
    }

    private void seedPolicies(com.maverick.feature.repository.PolicyRepository policyRepo) {
        policyRepo.deleteAll(); // Clear existing to ensure fresh state with new categories/strategies
        System.out.println(">>> Seeding Governance Policies... <<<");

        policyRepo.saveAll(java.util.List.of(
                // 1. Env Integrity
                com.maverick.feature.domain.Policy.builder()
                        .code("POL-01")
                        .name("Environment Integrity")
                        .category("Discovery")
                        .type("ENV_INTEGRITY")
                        .description("Blocks External Users from accessing Canary/Beta channels in Production.")
                        .isActive(true)
                        .enforcementLevel("BLOCK")
                        .configuration("{\"restricted_env\": " + quote("PRODUCTION")
                                + ", \"restricted_channels\": [\"CANARY\"], \"bypass_role\": " + quote("INTERNAL")
                                + "}")
                        .build(),

                // 2. Compatibility
                com.maverick.feature.domain.Policy.builder()
                        .code("POL-02")
                        .name("Lifecycle Compatibility")
                        .category("Compatibility")
                        .type("COMPATIBILITY")
                        .description("Blocks deprecated 0.x versions.")
                        .isActive(true)
                        .enforcementLevel("BLOCK")
                        .configuration("{\"blocked_prefixes\": [\"0.\"]}")
                        .build(),

                // 3. Security
                com.maverick.feature.domain.Policy.builder()
                        .code("POL-03")
                        .name("Sensitive Access Control")
                        .category("Security")
                        .type("SECURITY_ABAC")
                        .description("Restricts remote-audit to ADMINs.")
                        .isActive(true)
                        .enforcementLevel("BLOCK")
                        .configuration("{\"target_mfe\": " + quote("remote-audit") + ", \"required_role\": "
                                + quote("ADMIN") + "}")
                        .build(),

                // 4. Operational
                com.maverick.feature.domain.Policy.builder()
                        .code("POL-04")
                        .name("Operational State")
                        .category("Operational")
                        .type("OPERATIONAL")
                        .description("Enforces Maintenance Mode.")
                        .isActive(true)
                        .enforcementLevel("BLOCK")
                        .configuration("{\"flag_key\": " + quote("maintenance-mode") + ", \"bypass_internal\": true}")
                        .build(),

                // 5. External OPA (Rego)
                com.maverick.feature.domain.Policy.builder()
                        .code("POL-05")
                        .name("Global Rego Guard")
                        .category("Security")
                        .type("REGO_OPA")
                        .description("Delegates complex evaluations to OPA Sidecar (Rego).")
                        .isActive(true)
                        .enforcementLevel("BLOCK")
                        .configuration("{}")
                        .build()));

        System.out.println(">>> Policies Seeded! <<<");
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
    }
}
