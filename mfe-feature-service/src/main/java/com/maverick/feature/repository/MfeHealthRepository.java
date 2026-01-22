package com.maverick.feature.repository;

import com.maverick.feature.domain.MfeHealth;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MfeHealthRepository extends CrudRepository<MfeHealth, Long> {
    Optional<MfeHealth> findByApplicationVersionId(Long versionId);
}
