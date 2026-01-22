package com.maverick.feature.repository;

import com.maverick.feature.domain.MfeConsumedRemote;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MfeConsumedRemoteRepository extends CrudRepository<MfeConsumedRemote, Long> {
    List<MfeConsumedRemote> findByConsumerVersionId(Long versionId);
}
