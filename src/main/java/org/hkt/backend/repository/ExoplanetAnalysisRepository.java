package org.hkt.backend.repository;

import org.hkt.backend.entity.ExoplanetAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExoplanetAnalysisRepository extends JpaRepository<ExoplanetAnalysis, Long> {

    // Find by classification
    Page<ExoplanetAnalysis> findByClassification(String classification, Pageable pageable);

    // Find by confidence level
    Page<ExoplanetAnalysis> findByConfidenceLevel(String confidenceLevel, Pageable pageable);

    // Find by probability greater than or equal to
    Page<ExoplanetAnalysis> findByProbabilityGreaterThanEqual(Double probability, Pageable pageable);

    // Find by date range
    @Query("SELECT e FROM ExoplanetAnalysis e WHERE e.createdAt BETWEEN :startDate AND :endDate")
    Page<ExoplanetAnalysis> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Find by classification and confidence level
    Page<ExoplanetAnalysis> findByClassificationAndConfidenceLevel(
            String classification,
            String confidenceLevel,
            Pageable pageable
    );

    // Custom query to find high probability exoplanets
    @Query("SELECT e FROM ExoplanetAnalysis e WHERE e.probability >= :minProbability AND e.accuracy >= :minAccuracy")
    List<ExoplanetAnalysis> findHighConfidenceExoplanets(
            @Param("minProbability") Double minProbability,
            @Param("minAccuracy") Double minAccuracy
    );

    // Delete by id list
    void deleteByIdIn(List<Long> ids);

    // Count by classification
    long countByClassification(String classification);
}
