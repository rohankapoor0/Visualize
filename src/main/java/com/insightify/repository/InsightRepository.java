package com.insightify.repository;

import com.insightify.model.Insight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsightRepository extends JpaRepository<Insight, Long> {
    List<Insight> findByDatasetId(Long datasetId);
    Optional<Insight> findFirstByDatasetId(Long datasetId);
    void deleteByDatasetId(Long datasetId);
}
