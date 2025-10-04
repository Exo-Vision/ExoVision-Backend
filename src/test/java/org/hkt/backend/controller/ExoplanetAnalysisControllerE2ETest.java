package org.hkt.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hkt.backend.dto.ExoplanetAnalysisRequest;
import org.hkt.backend.dto.ExoplanetAnalysisResponse;
import org.hkt.backend.entity.ExoplanetAnalysis;
import org.hkt.backend.fixture.ExoplanetAnalysisFixture;
import org.hkt.backend.repository.ExoplanetAnalysisRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller E2E Integration Tests using H2 MySQL compatible mode
 * Tests full HTTP request/response cycle including:
 * - Endpoint paths (/api/analyses)
 * - JSON serialization/deserialization (snake_case)
 * - HTTP status codes
 * - Request/Response DTO mapping
 * - Pagination structure
 * - Database operations with H2
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ExoplanetAnalysisController E2E 통합 테스트")
class ExoplanetAnalysisControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExoplanetAnalysisRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/analyses - 분석 결과 저장 성공 (201 Created)")
    void createAnalysis_ValidRequest_Returns201() throws Exception {
        // Given
        ExoplanetAnalysisRequest request = ExoplanetAnalysisFixture.createValidRequest();

        // When & Then
        MvcResult result = mockMvc.perform(post("/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // JSON 필드명 snake_case 검증
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orbital_period").value(10.5))
                .andExpect(jsonPath("$.transit_duration").value(3.2))
                .andExpect(jsonPath("$.transit_depth").value(1.5))
                .andExpect(jsonPath("$.snr").value(15.0))
                .andExpect(jsonPath("$.probability").value(75.5))
                .andExpect(jsonPath("$.accuracy").value(0.85))
                .andExpect(jsonPath("$.f1_score").value(0.83))
                .andExpect(jsonPath("$.precision").value(0.88))
                .andExpect(jsonPath("$.recall").value(0.79))
                .andExpect(jsonPath("$.false_positive_rate").value(0.05))
                .andExpect(jsonPath("$.classification").value("Strong Candidate"))
                .andExpect(jsonPath("$.confidence_level").value("High"))
                .andExpect(jsonPath("$.created_at").exists())
                .andReturn();

        // Response DTO → JSON 변환 검증
        String responseJson = result.getResponse().getContentAsString();
        ExoplanetAnalysisResponse response = objectMapper.readValue(responseJson, ExoplanetAnalysisResponse.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getProbability()).isEqualTo(75.5);
        assertThat(response.getClassification()).isEqualTo("Strong Candidate");

        // DB 저장 검증
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/analyses - 유효성 검증 실패 (400 Bad Request)")
    void createAnalysis_InvalidRequest_Returns400() throws Exception {
        // Given - probability > 100 (invalid)
        ExoplanetAnalysisRequest invalidRequest = ExoplanetAnalysisFixture.createValidRequest();
        invalidRequest.setProbability(150.0);  // Invalid: must be <= 100

        // When & Then
        mockMvc.perform(post("/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/analyses - 전체 조회 with Pagination (200 OK)")
    void getAllAnalyses_WithPagination_Returns200() throws Exception {
        // Given
        repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());
        repository.save(ExoplanetAnalysisFixture.createValidEntity());
        repository.save(ExoplanetAnalysisFixture.createWeakSignal());

        // When & Then
        mockMvc.perform(get("/analyses")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDirection", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Page<T> 구조 검증
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.total_elements").value(3))
                .andExpect(jsonPath("$.total_pages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                // snake_case 필드 검증
                .andExpect(jsonPath("$.content[0].orbital_period").exists())
                .andExpect(jsonPath("$.content[0].transit_duration").exists())
                .andExpect(jsonPath("$.content[0].false_positive_rate").exists());
    }

    @Test
    @DisplayName("GET /api/analyses/{id} - 단일 조회 성공 (200 OK)")
    void getAnalysisById_Exists_Returns200() throws Exception {
        // Given
        ExoplanetAnalysis saved = repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());
        Long id = saved.getId();

        // When & Then
        mockMvc.perform(get("/analyses/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.probability").value(95.8))
                .andExpect(jsonPath("$.classification").value("Confirmed Exoplanet"))
                .andExpect(jsonPath("$.confidence_level").value("Very High"));
    }

    @Test
    @DisplayName("GET /api/analyses/{id} - 존재하지 않는 ID (404 Not Found)")
    void getAnalysisById_NotFound_Returns404() throws Exception {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        mockMvc.perform(get("/analyses/{id}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    @DisplayName("GET /api/analyses/classification/{classification} - 분류별 조회 (200 OK)")
    void getByClassification_Exists_Returns200() throws Exception {
        // Given
        repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());
        repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());
        repository.save(ExoplanetAnalysisFixture.createWeakSignal());

        // When & Then
        mockMvc.perform(get("/analyses/classification/{classification}", "Confirmed Exoplanet")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].classification", everyItem(is("Confirmed Exoplanet"))));
    }

    @Test
    @DisplayName("GET /api/analyses/confidence/{confidenceLevel} - 신뢰도별 조회 (200 OK)")
    void getByConfidenceLevel_Exists_Returns200() throws Exception {
        // Given
        repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());  // Very High
        repository.save(ExoplanetAnalysisFixture.createValidEntity());         // High
        repository.save(ExoplanetAnalysisFixture.createWeakSignal());          // Low

        // When & Then
        mockMvc.perform(get("/analyses/confidence/{confidenceLevel}", "Very High")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].confidence_level").value("Very High"));
    }

    @Test
    @DisplayName("GET /api/analyses/probability - 확률 이상 조회 (200 OK)")
    void getByProbability_Exists_Returns200() throws Exception {
        // Given
        repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());  // 95.8%
        repository.save(ExoplanetAnalysisFixture.createValidEntity());         // 75.5%
        repository.save(ExoplanetAnalysisFixture.createWeakSignal());          // 25.3%

        // When & Then
        mockMvc.perform(get("/analyses/probability")
                        .param("minProbability", "70.0")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].probability", everyItem(greaterThanOrEqualTo(70.0))));
    }

    @Test
    @DisplayName("DELETE /api/analyses/{id} - 단일 삭제 성공 (204 No Content)")
    void deleteAnalysisById_Exists_Returns204() throws Exception {
        // Given
        ExoplanetAnalysis saved = repository.save(ExoplanetAnalysisFixture.createValidEntity());
        Long id = saved.getId();

        // When & Then
        mockMvc.perform(delete("/analyses/{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());

        // DB 삭제 검증
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/analyses - 선택 삭제 성공 (204 No Content)")
    void deleteMultipleAnalyses_ValidIds_Returns204() throws Exception {
        // Given
        ExoplanetAnalysis entity1 = repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());
        ExoplanetAnalysis entity2 = repository.save(ExoplanetAnalysisFixture.createValidEntity());
        ExoplanetAnalysis entity3 = repository.save(ExoplanetAnalysisFixture.createWeakSignal());

        List<Long> idsToDelete = List.of(entity1.getId(), entity2.getId());

        // When & Then
        mockMvc.perform(delete("/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idsToDelete)))
                .andDo(print())
                .andExpect(status().isNoContent());

        // DB 검증
        assertThat(repository.findById(entity1.getId())).isEmpty();
        assertThat(repository.findById(entity2.getId())).isEmpty();
        assertThat(repository.findById(entity3.getId())).isPresent();
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("DELETE /api/analyses/all - 전체 삭제 성공 (204 No Content)")
    void deleteAllAnalyses_Success_Returns204() throws Exception {
        // Given
        repository.save(ExoplanetAnalysisFixture.createConfirmedExoplanet());
        repository.save(ExoplanetAnalysisFixture.createValidEntity());
        repository.save(ExoplanetAnalysisFixture.createWeakSignal());

        assertThat(repository.count()).isEqualTo(3);

        // When & Then
        mockMvc.perform(delete("/analyses/all"))
                .andDo(print())
                .andExpect(status().isNoContent());

        // DB 전체 삭제 검증
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("전체 플로우 테스트 - 생성 → 조회 → 수정(재생성) → 삭제")
    void fullWorkflow_CreateReadUpdateDelete_Success() throws Exception {
        // 1. Create
        ExoplanetAnalysisRequest createRequest = ExoplanetAnalysisFixture.createValidRequest();

        MvcResult createResult = mockMvc.perform(post("/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createJson = createResult.getResponse().getContentAsString();
        ExoplanetAnalysisResponse created = objectMapper.readValue(createJson, ExoplanetAnalysisResponse.class);
        Long id = created.getId();

        // 2. Read
        mockMvc.perform(get("/analyses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.probability").value(75.5));

        // 3. List
        mockMvc.perform(get("/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // 4. Delete
        mockMvc.perform(delete("/analyses/{id}", id))
                .andExpect(status().isNoContent());

        // 5. Verify deletion
        mockMvc.perform(get("/analyses/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("JSON snake_case 변환 검증 - Request camelCase → DB snake_case → Response camelCase")
    void verifyJsonNamingStrategy_SnakeCaseConversion() throws Exception {
        // Given
        ExoplanetAnalysisRequest request = ExoplanetAnalysisFixture.createValidRequest();

        // When - POST with camelCase in JSON
        MvcResult result = mockMvc.perform(post("/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then - Response should have snake_case in JSON
        String responseJson = result.getResponse().getContentAsString();

        assertThat(responseJson).contains("orbital_period");
        assertThat(responseJson).contains("transit_duration");
        assertThat(responseJson).contains("transit_depth");
        assertThat(responseJson).contains("false_positive_rate");
        assertThat(responseJson).contains("confidence_level");
        assertThat(responseJson).contains("created_at");

        // Verify DTO deserialization works correctly
        ExoplanetAnalysisResponse response = objectMapper.readValue(responseJson, ExoplanetAnalysisResponse.class);
        assertThat(response.getOrbitalPeriod()).isEqualTo(request.getOrbitalPeriod());
        assertThat(response.getFalsePositiveRate()).isEqualTo(request.getFalsePositiveRate());
    }
}
