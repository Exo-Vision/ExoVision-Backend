package org.hkt.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Exoplanet analysis response DTO")
public class ExoplanetAnalysisResponse {

    @Schema(description = "Analysis result ID", example = "1")
    private Long id;

    @Schema(description = "Orbital period (days)", example = "50.5")
    private Double orbitalPeriod;

    @Schema(description = "Transit duration (hours)", example = "8.2")
    private Double transitDuration;

    @Schema(description = "Transit depth (percentage)", example = "2.5")
    private Double transitDepth;

    @Schema(description = "Signal-to-Noise Ratio", example = "25.3")
    private Double snr;

    @Schema(description = "Planet radius (Earth radii)", example = "4.2")
    private Double planetRadius;

    @Schema(description = "Exoplanet probability (0-100%)", example = "95.8")
    private Double probability;

    @Schema(description = "Accuracy (0-1)", example = "0.95")
    private Double accuracy;

    @Schema(description = "F1 score (0-1)", example = "0.93")
    private Double f1Score;

    @Schema(description = "Precision (0-1)", example = "0.94")
    private Double precision;

    @Schema(description = "Recall (0-1)", example = "0.92")
    private Double recall;

    @Schema(description = "False positive rate (0-1)", example = "0.05")
    private Double falsePositiveRate;

    @Schema(description = "Classification", example = "Confirmed Exoplanet")
    private String classification;

    @Schema(description = "Confidence level", example = "Very High")
    private String confidenceLevel;

    @Schema(description = "Chart visualization data (JSON object)", nullable = true)
    private JsonNode chartData;

    @Schema(description = "Created timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
