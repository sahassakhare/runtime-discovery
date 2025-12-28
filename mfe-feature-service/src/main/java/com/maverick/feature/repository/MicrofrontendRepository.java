package com.maverick.feature.repository;

import com.maverick.feature.domain.Microfrontend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MicrofrontendRepository extends JpaRepository<Microfrontend, Long> {
    Optional<Microfrontend> findByName(String name);
}
