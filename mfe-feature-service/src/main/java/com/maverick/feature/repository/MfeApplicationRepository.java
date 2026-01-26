package com.maverick.feature.repository;

import com.maverick.feature.domain.MfeApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MfeApplicationRepository extends JpaRepository<MfeApplication, Long> {
    Optional<MfeApplication> findByName(String name);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM MfeApplication a WHERE a.group.tenant.id = :tenantId AND a.group.name = :groupName AND a.name = :appName")
    Optional<MfeApplication> findByFQN(@org.springframework.data.repository.query.Param("tenantId") String tenantId,
            @org.springframework.data.repository.query.Param("groupName") String groupName,
            @org.springframework.data.repository.query.Param("appName") String appName);
}
