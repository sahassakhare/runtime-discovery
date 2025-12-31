package com.maverick.feature.repository;

import com.maverick.feature.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, String> {
    List<Policy> findByIsActiveTrue();

    java.util.Optional<Policy> findByCode(String code);

    boolean existsByCode(String code);
}
