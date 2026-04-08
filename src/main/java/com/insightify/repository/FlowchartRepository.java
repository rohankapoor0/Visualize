package com.insightify.repository;

import com.insightify.model.Flowchart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlowchartRepository extends JpaRepository<Flowchart, Long> {
    List<Flowchart> findByDatasetId(Long datasetId);
    Optional<Flowchart> findFirstByDatasetId(Long datasetId);
    void deleteByDatasetId(Long datasetId);
}
