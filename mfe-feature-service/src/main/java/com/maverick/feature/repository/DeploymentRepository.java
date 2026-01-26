package com.maverick.feature.repository;

import com.maverick.feature.domain.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

        // Find specific tenant deployment
        @Query("SELECT d FROM Deployment d JOIN FETCH d.version v JOIN FETCH v.application a " +
                        "WHERE a.name = :mfeName AND d.environment = :env AND d.tenantId = :tenantId AND d.active = true")
        Optional<Deployment> findActiveByTenant(@Param("mfeName") String mfeName,
                        @Param("env") com.maverick.feature.domain.Environment env,
                        @Param("tenantId") String tenantId);

        // Find global deployment
        @Query("SELECT d FROM Deployment d JOIN FETCH d.version v JOIN FETCH v.application a " +
                        "WHERE a.name = :mfeName AND d.environment = :env AND d.tenantId IS NULL AND d.active = true")
        Optional<Deployment> findActiveGlobal(@Param("mfeName") String mfeName,
                        @Param("env") com.maverick.feature.domain.Environment env);

        // Find LOCKED deployment (Resolution Access Override)
        @Query("SELECT d FROM Deployment d JOIN FETCH d.version v JOIN FETCH v.application a " +
                        "WHERE a.name = :mfeName AND d.environment = :env AND d.isLocked = true")
        Optional<Deployment> findLocked(@Param("mfeName") String mfeName,
                        @Param("env") com.maverick.feature.domain.Environment env);

        List<Deployment> findByEnvironment(com.maverick.feature.domain.Environment environment);

        long countByCreatedAtAfter(java.time.LocalDateTime date);

        @Query("SELECT d FROM Deployment d JOIN FETCH d.version v JOIN FETCH v.application a WHERE a.id = :appId AND d.environment = :env AND d.active = true")
        Optional<Deployment> findActiveByApplicationId(@Param("appId") Long appId,
                        @Param("env") com.maverick.feature.domain.Environment env);
}
