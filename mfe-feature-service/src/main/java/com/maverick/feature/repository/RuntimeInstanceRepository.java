package com.maverick.feature.repository;

import com.maverick.feature.domain.RuntimeInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuntimeInstanceRepository extends JpaRepository<RuntimeInstance, Long> {
    Optional<RuntimeInstance> findByAppNameAndEnvironmentAndUrl(String appName,
            com.maverick.feature.domain.Environment environment, String url);
}
