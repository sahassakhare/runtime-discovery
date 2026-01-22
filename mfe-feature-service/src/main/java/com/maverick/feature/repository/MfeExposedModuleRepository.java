package com.maverick.feature.repository;

import com.maverick.feature.domain.MfeExposedModule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MfeExposedModuleRepository extends CrudRepository<MfeExposedModule, Long> {
    List<MfeExposedModule> findByApplicationVersionId(Long versionId);
}
