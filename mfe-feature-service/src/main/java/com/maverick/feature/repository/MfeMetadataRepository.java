package com.maverick.feature.repository;

import com.maverick.feature.domain.MfeMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MfeMetadataRepository extends JpaRepository<MfeMetadata, Long> {
}
