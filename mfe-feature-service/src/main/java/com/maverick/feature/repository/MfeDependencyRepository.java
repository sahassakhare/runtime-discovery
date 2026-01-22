package com.maverick.feature.repository;

import com.maverick.feature.domain.MfeDependency;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MfeDependencyRepository extends CrudRepository<MfeDependency, Long> {
    List<MfeDependency> findByApplicationVersionId(Long versionId);
}
