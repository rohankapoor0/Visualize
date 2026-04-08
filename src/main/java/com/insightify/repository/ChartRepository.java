package com.insightify.repository;

import com.insightify.model.Chart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChartRepository extends JpaRepository<Chart, Long> {
    List<Chart> findByDatasetId(Long datasetId);
    void deleteByDatasetId(Long datasetId);
}
