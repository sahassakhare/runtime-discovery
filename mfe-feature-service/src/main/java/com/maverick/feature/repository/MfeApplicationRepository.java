package com.maverick.feature.repository;

import com.maverick.feature.domain.MfeApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MfeApplicationRepository extends JpaRepository<MfeApplication, Long> {
    Optional<MfeApplication> findByName(String name);
}
