package com.maverick.feature.repository;

import com.maverick.feature.domain.MfeApplicationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MfeApplicationGroupRepository extends JpaRepository<MfeApplicationGroup, Long> {
}
