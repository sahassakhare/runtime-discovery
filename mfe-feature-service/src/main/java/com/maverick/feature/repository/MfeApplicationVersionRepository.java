package com.maverick.feature.repository;

import com.maverick.feature.domain.MfeApplicationVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MfeApplicationVersionRepository extends JpaRepository<MfeApplicationVersion, Long> {
    java.util.List<MfeApplicationVersion> findByApplicationNameAndEnvironment(String appName, String environment);

    java.util.Optional<MfeApplicationVersion> findTopByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    java.util.Optional<MfeApplicationVersion> findByApplicationIdAndVersion(Long applicationId, String version);
}
