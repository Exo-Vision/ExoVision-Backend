package org.hkt.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hkt.backend.dto.ExoplanetAnalysisRequest;
import org.hkt.backend.dto.ExoplanetAnalysisResponse;
import org.hkt.backend.entity.ExoplanetAnalysis;
import org.hkt.backend.fixture.ExoplanetAnalysisFixture;
import org.hkt.backend.repository.ExoplanetAnalysisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Service layer unit tests with mocked repository
 * Verifies business logic and DTO conversion
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExoplanetAnalysisService 단위 테스트")
class ExoplanetAnalysisServiceTest {

    @Mock
    private ExoplanetAnalysisRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExoplanetAnalysisService service;

    @Test
    @DisplayName("분석 결과 저장 - Request DTO → Entity → Response DTO 변환 검증")
    void save_RequestToEntityToResponse_Success() {
        // Given
        ExoplanetAnalysisRequest request = ExoplanetAnalysisFixture.createValidRequest();
        ExoplanetAnalysis savedEntity = ExoplanetAnalysisFixture.createValidEntity();
        savedEntity.setId(1L);

        when(repository.save(any(ExoplanetAnalysis.class))).thenReturn(savedEntity);

        // When
        ExoplanetAnalysisResponse response = service.save(request);

        // Then
        verify(repository, times(1)).save(any(ExoplanetAnalysis.class));

        // Response DTO 검증
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProbability()).isEqualTo(75.5);
        assertThat(response.getOrbitalPeriod()).isEqualTo(request.getOrbitalPeriod());
        assertThat(response.getClassification()).isEqualTo(request.getClassification());
        assertThat(response.getConfidenceLevel()).isEqualTo(request.getConfidenceLevel());

        // 메트릭 값 검증 (0-1 범위)
        assertThat(response.getAccuracy()).isBetween(0.0, 1.0);
        assertThat(response.getF1Score()).isBetween(0.0, 1.0);
        assertThat(response.getPrecision()).isBetween(0.0, 1.0);
        assertThat(response.getRecall()).isBetween(0.0, 1.0);
        assertThat(response.getFalsePositiveRate()).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("ID로 조회 성공 - Entity → Response DTO 변환 검증")
    void findById_EntityToResponse_Success() {
        // Given
        Long id = 1L;
        ExoplanetAnalysis entity = ExoplanetAnalysisFixture.createConfirmedExoplanet();
        entity.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(entity));

        // When
        ExoplanetAnalysisResponse response = service.findById(id);

        // Then
        verify(repository, times(1)).findById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getProbability()).isEqualTo(95.8);
        assertThat(response.getClassification()).isEqualTo("Confirmed Exoplanet");
        assertThat(response.getConfidenceLevel()).isEqualTo("Very High");
    }

    @Test
    @DisplayName("ID로 조회 실패 - 존재하지 않는 ID")
    void findById_NotFound_ThrowsException() {
        // Given
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found with id: " + id);

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("전체 조회 with Pagination - Page<Entity> → Page<Response> 변환 검증")
    void findAll_WithPagination_EntityPageToResponsePage() {
        // Given
        List<ExoplanetAnalysis> entities = List.of(
                ExoplanetAnalysisFixture.createConfirmedExoplanet(),
                ExoplanetAnalysisFixture.createValidEntity(),
                ExoplanetAnalysisFixture.createWeakSignal()
        );

        Page<ExoplanetAnalysis> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), entities.size());
        when(repository.findAll(any(Pageable.class))).thenReturn(entityPage);

        // When
        Page<ExoplanetAnalysisResponse> responsePage = service.findAll(PageRequest.of(0, 10));

        // Then
        verify(repository, times(1)).findAll(any(Pageable.class));

        assertThat(responsePage.getContent()).hasSize(3);
        assertThat(responsePage.getTotalElements()).isEqualTo(3);
        assertThat(responsePage.getContent().get(0).getClassification()).isEqualTo("Confirmed Exoplanet");
    }

    @Test
    @DisplayName("분류별 조회 - 필터링 및 DTO 변환 검증")
    void findByClassification_FilterAndConvert_Success() {
        // Given
        String classification = "Strong Candidate";
        List<ExoplanetAnalysis> entities = List.of(
                ExoplanetAnalysisFixture.createValidEntity(),
                ExoplanetAnalysisFixture.createValidEntity()
        );

        Page<ExoplanetAnalysis> entityPage = new PageImpl<>(entities);
        when(repository.findByClassification(eq(classification), any(Pageable.class)))
                .thenReturn(entityPage);

        // When
        Page<ExoplanetAnalysisResponse> responsePage = service.findByClassification(classification, PageRequest.of(0, 10));

        // Then
        verify(repository, times(1)).findByClassification(eq(classification), any(Pageable.class));

        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent()).allMatch(r -> r.getClassification().equals(classification));
    }

    @Test
    @DisplayName("신뢰도별 조회 - 필터링 및 DTO 변환 검증")
    void findByConfidenceLevel_FilterAndConvert_Success() {
        // Given
        String confidenceLevel = "Very High";
        ExoplanetAnalysis entity = ExoplanetAnalysisFixture.createConfirmedExoplanet();

        Page<ExoplanetAnalysis> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findByConfidenceLevel(eq(confidenceLevel), any(Pageable.class)))
                .thenReturn(entityPage);

        // When
        Page<ExoplanetAnalysisResponse> responsePage = service.findByConfidenceLevel(confidenceLevel, PageRequest.of(0, 10));

        // Then
        verify(repository, times(1)).findByConfidenceLevel(eq(confidenceLevel), any(Pageable.class));

        assertThat(responsePage.getContent()).hasSize(1);
        assertThat(responsePage.getContent().get(0).getConfidenceLevel()).isEqualTo(confidenceLevel);
    }

    @Test
    @DisplayName("확률 이상 조회 - 필터링 및 DTO 변환 검증")
    void findByProbability_FilterAndConvert_Success() {
        // Given
        Double minProbability = 70.0;
        List<ExoplanetAnalysis> entities = List.of(
                ExoplanetAnalysisFixture.createConfirmedExoplanet(),  // 95.8%
                ExoplanetAnalysisFixture.createValidEntity()           // 75.5%
        );

        Page<ExoplanetAnalysis> entityPage = new PageImpl<>(entities);
        when(repository.findByProbabilityGreaterThanEqual(eq(minProbability), any(Pageable.class)))
                .thenReturn(entityPage);

        // When
        Page<ExoplanetAnalysisResponse> responsePage = service.findByProbability(minProbability, PageRequest.of(0, 10));

        // Then
        verify(repository, times(1)).findByProbabilityGreaterThanEqual(eq(minProbability), any(Pageable.class));

        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent()).allMatch(r -> r.getProbability() >= minProbability);
    }

    @Test
    @DisplayName("단일 삭제 성공")
    void deleteById_Success() {
        // Given
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);
        doNothing().when(repository).deleteById(id);

        // When
        service.deleteById(id);

        // Then
        verify(repository, times(1)).existsById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("단일 삭제 실패 - 존재하지 않는 ID")
    void deleteById_NotFound_ThrowsException() {
        // Given
        Long id = 999L;
        when(repository.existsById(id)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> service.deleteById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found with id: " + id);

        verify(repository, times(1)).existsById(id);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("여러 ID 일괄 삭제 성공")
    void deleteByIds_Success() {
        // Given
        List<Long> ids = List.of(1L, 2L, 3L);
        doNothing().when(repository).deleteAllById(ids);

        // When
        service.deleteByIds(ids);

        // Then
        verify(repository, times(1)).deleteAllById(ids);
    }

    @Test
    @DisplayName("전체 삭제 성공")
    void deleteAll_Success() {
        // Given
        when(repository.count()).thenReturn(10L);
        doNothing().when(repository).deleteAll();

        // When
        service.deleteAll();

        // Then
        verify(repository, times(1)).count();
        verify(repository, times(1)).deleteAll();
    }

    @Test
    @DisplayName("DTO 필드 매핑 완벽 검증 - Request to Entity")
    void verifyDtoMapping_RequestToEntity() {
        // Given
        ExoplanetAnalysisRequest request = ExoplanetAnalysisFixture.createValidRequest();
        ExoplanetAnalysis savedEntity = ExoplanetAnalysisFixture.createValidEntity();
        savedEntity.setId(1L);

        when(repository.save(any(ExoplanetAnalysis.class))).thenReturn(savedEntity);

        // When
        service.save(request);

        // Then - verify all fields mapped from Request to Entity
        verify(repository).save(argThat(entity ->
                entity.getOrbitalPeriod().equals(request.getOrbitalPeriod()) &&
                entity.getTransitDuration().equals(request.getTransitDuration()) &&
                entity.getTransitDepth().equals(request.getTransitDepth()) &&
                entity.getSnr().equals(request.getSnr()) &&
                entity.getProbability().equals(request.getProbability()) &&
                entity.getAccuracy().equals(request.getAccuracy()) &&
                entity.getF1Score().equals(request.getF1Score()) &&
                entity.getPrecision().equals(request.getPrecision()) &&
                entity.getRecall().equals(request.getRecall()) &&
                entity.getFalsePositiveRate().equals(request.getFalsePositiveRate()) &&
                entity.getClassification().equals(request.getClassification()) &&
                entity.getConfidenceLevel().equals(request.getConfidenceLevel())
        ));
    }
}
