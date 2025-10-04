package org.hkt.backend.repository;

import org.hkt.backend.entity.ExoplanetAnalysis;
import org.hkt.backend.fixture.ExoplanetAnalysisFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository layer integration tests using H2 MySQL compatible mode
 * Tests database operations with H2 in MySQL mode
 */
@DataJpaTest
@DisplayName("ExoplanetAnalysisRepository 통합 테스트")
class ExoplanetAnalysisRepositoryTest {

    @Autowired
    private ExoplanetAnalysisRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("엔티티 저장 및 조회 성공")
    void saveAndFindById_Success() {
        // Given
        ExoplanetAnalysis entity = ExoplanetAnalysisFixture.createValidEntity();

        // When
        ExoplanetAnalysis saved = repository.save(entity);
        Optional<ExoplanetAnalysis> found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getProbability()).isEqualTo(75.5);
        assertThat(found.get().getClassification()).isEqualTo("Strong Candidate");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("분류별 조회 - Pagination 적용")
    void findByClassification_WithPagination() {
        // Given
        ExoplanetAnalysis confirmed1 = ExoplanetAnalysisFixture.createConfirmedExoplanet();
        ExoplanetAnalysis confirmed2 = ExoplanetAnalysisFixture.createConfirmedExoplanet();
        ExoplanetAnalysis weakSignal = ExoplanetAnalysisFixture.createWeakSignal();

        repository.saveAll(List.of(confirmed1, confirmed2, weakSignal));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // When
        Page<ExoplanetAnalysis> result = repository.findByClassification("Confirmed Exoplanet", pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(a -> a.getClassification().equals("Confirmed Exoplanet"));
    }

    @Test
    @DisplayName("신뢰도 레벨별 조회 - Pagination 적용")
    void findByConfidenceLevel_WithPagination() {
        // Given
        ExoplanetAnalysis high1 = ExoplanetAnalysisFixture.createConfirmedExoplanet();
        ExoplanetAnalysis high2 = ExoplanetAnalysisFixture.createValidEntity();
        ExoplanetAnalysis low = ExoplanetAnalysisFixture.createWeakSignal();

        repository.saveAll(List.of(high1, high2, low));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // When
        Page<ExoplanetAnalysis> result = repository.findByConfidenceLevel("Very High", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getConfidenceLevel()).isEqualTo("Very High");
    }

    @Test
    @DisplayName("확률 이상 조회 - Pagination 적용")
    void findByProbabilityGreaterThanEqual_WithPagination() {
        // Given
        ExoplanetAnalysis high = ExoplanetAnalysisFixture.createConfirmedExoplanet();  // 95.8%
        ExoplanetAnalysis medium = ExoplanetAnalysisFixture.createValidEntity();      // 75.5%
        ExoplanetAnalysis low = ExoplanetAnalysisFixture.createWeakSignal();          // 25.3%

        repository.saveAll(List.of(high, medium, low));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "probability"));

        // When
        Page<ExoplanetAnalysis> result = repository.findByProbabilityGreaterThanEqual(70.0, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(a -> a.getProbability() >= 70.0);
        assertThat(result.getContent().get(0).getProbability()).isGreaterThan(result.getContent().get(1).getProbability());
    }

    @Test
    @DisplayName("전체 조회 - Pagination 및 정렬 적용")
    void findAll_WithPaginationAndSorting() {
        // Given
        for (int i = 0; i < 15; i++) {
            repository.save(ExoplanetAnalysisFixture.createWithProbability(50.0 + i));
        }

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "probability"));

        // When
        Page<ExoplanetAnalysis> firstPage = repository.findAll(pageable);

        // Then
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.getTotalElements()).isEqualTo(15);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.hasNext()).isTrue();

        // 정렬 검증
        List<Double> probabilities = firstPage.getContent().stream()
                .map(ExoplanetAnalysis::getProbability)
                .toList();
        assertThat(probabilities).isSortedAccordingTo((a, b) -> Double.compare(b, a));
    }

    @Test
    @DisplayName("엔티티 삭제 성공")
    void deleteById_Success() {
        // Given
        ExoplanetAnalysis saved = repository.save(ExoplanetAnalysisFixture.createValidEntity());
        Long savedId = saved.getId();

        // When
        repository.deleteById(savedId);

        // Then
        Optional<ExoplanetAnalysis> found = repository.findById(savedId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("여러 엔티티 일괄 삭제 성공")
    void deleteAllByIdInBatch_Success() {
        // Given
        ExoplanetAnalysis entity1 = repository.save(ExoplanetAnalysisFixture.createValidEntity());
        ExoplanetAnalysis entity2 = repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());
        ExoplanetAnalysis entity3 = repository.save(ExoplanetAnalysisFixture.createWeakSignal());

        List<Long> idsToDelete = List.of(entity1.getId(), entity2.getId());

        // When
        repository.deleteAllByIdInBatch(idsToDelete);
        repository.flush();
        entityManager.clear();  // Clear L1 cache after batch delete

        // Then
        assertThat(repository.findById(entity1.getId())).isEmpty();
        assertThat(repository.findById(entity2.getId())).isEmpty();
        assertThat(repository.findById(entity3.getId())).isPresent();
    }

    @Test
    @DisplayName("전체 삭제 성공")
    void deleteAll_Success() {
        // Given
        repository.save(ExoplanetAnalysisFixture.createValidEntity());
        repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());
        repository.save(ExoplanetAnalysisFixture.createWeakSignal());

        // When
        repository.deleteAll();

        // Then
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("createdAt 자동 생성 확인")
    void createdAt_AutoGenerated() {
        // Given
        ExoplanetAnalysis entity = ExoplanetAnalysisFixture.createValidEntity();
        assertThat(entity.getCreatedAt()).isNull();

        // When
        ExoplanetAnalysis saved = repository.save(entity);

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
