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
            org.ff4j.FF4j ff4j,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
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

                Version v1 = new Version();
                v1.setVersion("1.0.0");
                v1.setRemoteEntry("http://localhost:4201/remoteEntry.js");
                // v1.setIntegrity(null); // Disable SRI for dev
                v1.setActive(true);
                v1.setReleaseTrack("STABLE");
                v1.setMicrofrontend(mfe);
                v1.setCreatedAt(LocalDateTime.now());
                versionRepo.save(v1);

                Version v2 = new Version();
                v2.setVersion("1.1.0-canary");
                v2.setRemoteEntry("http://localhost:4201/remoteEntry.js");
                // v2.setIntegrity(null);
                v2.setActive(true);
                v2.setReleaseTrack("CANARY");
                v2.setMicrofrontend(mfe);
                v2.setCreatedAt(LocalDateTime.now());
                versionRepo.save(v2);

                System.out.println(">>> Database seeded with remote-profile (v1.0.0 and v1.1.0-canary) <<<");
            }
        };
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
        fRouting.addProperty(new org.ff4j.property.PropertyString("trackMapping", "CANARY"));
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
