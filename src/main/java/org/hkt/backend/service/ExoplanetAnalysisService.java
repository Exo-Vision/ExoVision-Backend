package org.hkt.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hkt.backend.dto.ExoplanetAnalysisRequest;
import org.hkt.backend.dto.ExoplanetAnalysisResponse;
import org.hkt.backend.entity.ExoplanetAnalysis;
import org.hkt.backend.repository.ExoplanetAnalysisRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExoplanetAnalysisService {

    private final ExoplanetAnalysisRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ExoplanetAnalysisResponse save(ExoplanetAnalysisRequest request) {
        log.info("Saving exoplanet analysis: {}", request);

        String chartDataString = null;
        if (request.getChartData() != null) {
            try {
                chartDataString = objectMapper.writeValueAsString(request.getChartData());
            } catch (JsonProcessingException e) {
                log.error("Failed to convert chartData to JSON string", e);
                throw new RuntimeException("Failed to process chart data", e);
            }
        }

        ExoplanetAnalysis entity = ExoplanetAnalysis.builder()
                .orbitalPeriod(request.getOrbitalPeriod())
                .transitDuration(request.getTransitDuration())
                .transitDepth(request.getTransitDepth())
                .snr(request.getSnr())
                .planetRadius(request.getPlanetRadius())
                .probability(request.getProbability())
                .accuracy(request.getAccuracy())
                .f1Score(request.getF1Score())
                .precision(request.getPrecision())
                .recall(request.getRecall())
                .falsePositiveRate(request.getFalsePositiveRate())
                .classification(request.getClassification())
                .confidenceLevel(request.getConfidenceLevel())
                .chartData(chartDataString)
                .build();

        ExoplanetAnalysis saved = repository.save(entity);
        log.info("Successfully saved exoplanet analysis with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    public ExoplanetAnalysisResponse findById(Long id) {
        log.info("Finding exoplanet analysis by id: {}", id);

        ExoplanetAnalysis entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Exoplanet analysis not found with id: {}", id);
                    return new RuntimeException("Exoplanet analysis not found with id: " + id);
                });

        return mapToResponse(entity);
    }

    public Page<ExoplanetAnalysisResponse> findAll(Pageable pageable) {
        log.info("Finding all exoplanet analyses with pagination: {}", pageable);

        Page<ExoplanetAnalysis> page = repository.findAll(pageable);
        log.info("Found {} exoplanet analyses", page.getTotalElements());

        return page.map(this::mapToResponse);
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting exoplanet analysis with id: {}", id);

        if (!repository.existsById(id)) {
            log.error("Cannot delete - Exoplanet analysis not found with id: {}", id);
            throw new RuntimeException("Exoplanet analysis not found with id: " + id);
        }

        repository.deleteById(id);
        log.info("Successfully deleted exoplanet analysis with id: {}", id);
    }

    @Transactional
    public void deleteByIds(List<Long> ids) {
        log.info("Deleting {} exoplanet analyses", ids.size());

        repository.deleteAllById(ids);
        log.info("Successfully deleted {} exoplanet analyses", ids.size());
    }

    @Transactional
    public void deleteAll() {
        log.info("Deleting all exoplanet analyses");

        long count = repository.count();
        repository.deleteAll();
        log.info("Successfully deleted {} exoplanet analyses", count);
    }

    public Page<ExoplanetAnalysisResponse> findByClassification(String classification, Pageable pageable) {
        log.info("Finding exoplanet analyses by classification: {}", classification);

        return repository.findByClassification(classification, pageable)
                .map(this::mapToResponse);
    }

    public Page<ExoplanetAnalysisResponse> findByConfidenceLevel(String confidenceLevel, Pageable pageable) {
        log.info("Finding exoplanet analyses by confidence level: {}", confidenceLevel);

        return repository.findByConfidenceLevel(confidenceLevel, pageable)
                .map(this::mapToResponse);
    }

    public Page<ExoplanetAnalysisResponse> findByProbability(Double minProbability, Pageable pageable) {
        log.info("Finding exoplanet analyses with probability >= {}", minProbability);

        return repository.findByProbabilityGreaterThanEqual(minProbability, pageable)
                .map(this::mapToResponse);
    }

    private ExoplanetAnalysisResponse mapToResponse(ExoplanetAnalysis entity) {
        JsonNode chartDataNode = null;
        if (entity.getChartData() != null) {
            try {
                chartDataNode = objectMapper.readTree(entity.getChartData());
            } catch (JsonProcessingException e) {
                log.error("Failed to parse chartData JSON string", e);
            }
        }

        return ExoplanetAnalysisResponse.builder()
                .id(entity.getId())
                .orbitalPeriod(entity.getOrbitalPeriod())
                .transitDuration(entity.getTransitDuration())
                .transitDepth(entity.getTransitDepth())
                .snr(entity.getSnr())
                .planetRadius(entity.getPlanetRadius())
                .probability(entity.getProbability())
                .accuracy(entity.getAccuracy())
                .f1Score(entity.getF1Score())
                .precision(entity.getPrecision())
                .recall(entity.getRecall())
                .falsePositiveRate(entity.getFalsePositiveRate())
                .classification(entity.getClassification())
                .confidenceLevel(entity.getConfidenceLevel())
                .chartData(chartDataNode)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
