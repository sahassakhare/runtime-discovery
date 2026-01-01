package com.maverick.feature.repository;

import com.maverick.feature.domain.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    Optional<Version> findByMicrofrontendIdAndVersion(Long microfrontendId, String version);

    List<Version> findByMicrofrontendId(Long microfrontendId);

    Optional<Version> findTopByMicrofrontendIdOrderByCreatedAtDesc(Long microfrontendId);
}
