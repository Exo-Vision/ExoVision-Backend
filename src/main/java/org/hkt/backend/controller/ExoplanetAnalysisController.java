package org.hkt.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hkt.backend.dto.ExoplanetAnalysisRequest;
import org.hkt.backend.dto.ExoplanetAnalysisResponse;
import org.hkt.backend.service.ExoplanetAnalysisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analyses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exoplanet Analysis", description = "Exoplanet analysis result management API")
public class ExoplanetAnalysisController {

    private final ExoplanetAnalysisService service;

    @PostMapping
    @Operation(summary = "Save analysis result", description = "Save exoplanet analysis result to database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Analysis result saved successfully",
                    content = @Content(schema = @Schema(implementation = ExoplanetAnalysisResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (Validation error)",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    public ResponseEntity<ExoplanetAnalysisResponse> saveAnalysis(
            @Valid @RequestBody @Parameter(description = "Exoplanet analysis request data", required = true)
            ExoplanetAnalysisRequest request) {
        log.info("POST /analyses - Saving new analysis");

        ExoplanetAnalysisResponse response = service.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all analysis results", description = "Retrieve all analysis results with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<ExoplanetAnalysisResponse>> getAllAnalyses(
            @Parameter(description = "Page number (starts from 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /analyses - Retrieving all analyses (page: {}, size: {}, sortBy: {}, sortDirection: {})",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ExoplanetAnalysisResponse> responses = service.findAll(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get analysis result by ID", description = "Retrieve analysis result by specific ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ExoplanetAnalysisResponse.class))),
            @ApiResponse(responseCode = "404", description = "Analysis result not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ExoplanetAnalysisResponse> getAnalysisById(
            @Parameter(description = "Analysis result ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("GET /analyses/{} - Retrieving analysis by id", id);

        ExoplanetAnalysisResponse response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/classification/{classification}")
    public ResponseEntity<Page<ExoplanetAnalysisResponse>> getAnalysesByClassification(
            @PathVariable String classification,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /analyses/classification/{} - Retrieving analyses by classification", classification);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExoplanetAnalysisResponse> responses = service.findByClassification(classification, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/confidence/{confidenceLevel}")
    public ResponseEntity<Page<ExoplanetAnalysisResponse>> getAnalysesByConfidenceLevel(
            @PathVariable String confidenceLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /analyses/confidence/{} - Retrieving analyses by confidence level", confidenceLevel);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExoplanetAnalysisResponse> responses = service.findByConfidenceLevel(confidenceLevel, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/probability")
    public ResponseEntity<Page<ExoplanetAnalysisResponse>> getAnalysesByProbability(
            @RequestParam Double minProbability,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /analyses/probability?minProbability={} - Retrieving analyses by minimum probability",
                minProbability);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "probability"));
        Page<ExoplanetAnalysisResponse> responses = service.findByProbability(minProbability, pageable);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete analysis result", description = "Delete analysis result by specific ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Analysis result not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteAnalysis(
            @Parameter(description = "Analysis result ID to delete", required = true, example = "1")
            @PathVariable Long id) {
        log.info("DELETE /analyses/{} - Deleting analysis by id", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Delete multiple analysis results", description = "Delete multiple analysis results at once")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully")
    })
    public ResponseEntity<Void> deleteMultipleAnalyses(
            @Parameter(description = "List of analysis result IDs to delete", required = true, example = "[1, 2, 3]")
            @RequestBody List<Long> ids) {
        log.info("DELETE /analyses - Deleting {} analyses", ids.size());

        service.deleteByIds(ids);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @Operation(summary = "Delete all analysis results", description = "Delete all analysis results")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully")
    })
    public ResponseEntity<Void> deleteAllAnalyses() {
        log.info("DELETE /analyses/all - Deleting all analyses");

        service.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error occurred: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors,
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Error occurred: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    record ErrorResponse(int status, String message, long timestamp) {}
    record ValidationErrorResponse(int status, String message, Map<String, String> errors, long timestamp) {}
}
