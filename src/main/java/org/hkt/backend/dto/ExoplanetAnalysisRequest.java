package org.hkt.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Exoplanet analysis request DTO")
public class ExoplanetAnalysisRequest {

    @Schema(description = "Orbital period (days)", example = "50.5", required = true)
    @NotNull(message = "Orbital period is required")
    private Double orbitalPeriod;

    @Schema(description = "Transit duration (hours)", example = "8.2", required = true)
    @NotNull(message = "Transit duration is required")
    private Double transitDuration;

    @Schema(description = "Transit depth (percentage)", example = "2.5", required = true)
    @NotNull(message = "Transit depth is required")
    private Double transitDepth;

    @Schema(description = "Signal-to-Noise Ratio", example = "25.3", required = true)
    @NotNull(message = "SNR is required")
    private Double snr;

    @Schema(description = "Planet radius (Earth radii)", example = "4.2", required = true)
    @NotNull(message = "Planet radius is required")
    private Double planetRadius;

    @Schema(description = "Exoplanet probability (0-100%)", example = "95.8", required = true)
    @NotNull(message = "Probability is required")
    @DecimalMin(value = "0.0", message = "Probability must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Probability must be between 0 and 100")
    private Double probability;

    @Schema(description = "Accuracy (0-1)", example = "0.95")
    @DecimalMin(value = "0.0", message = "Accuracy must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Accuracy must be between 0 and 1")
    private Double accuracy;

    @Schema(description = "F1 score (0-1)", example = "0.93")
    @DecimalMin(value = "0.0", message = "F1 score must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "F1 score must be between 0 and 1")
    private Double f1Score;

    @Schema(description = "Precision (0-1)", example = "0.94")
    @DecimalMin(value = "0.0", message = "Precision must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Precision must be between 0 and 1")
    private Double precision;

    @Schema(description = "Recall (0-1)", example = "0.92")
    @DecimalMin(value = "0.0", message = "Recall must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Recall must be between 0 and 1")
    private Double recall;

    @Schema(description = "False positive rate (0-1)", example = "0.05")
    @DecimalMin(value = "0.0", message = "False positive rate must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "False positive rate must be between 0 and 1")
    private Double falsePositiveRate;

    @Schema(description = "Classification (Confirmed Exoplanet/Strong Candidate/Weak Candidate/False Positive)",
            example = "Confirmed Exoplanet", required = true)
    @NotNull(message = "Classification is required")
    private String classification;

    @Schema(description = "Confidence level (Very High/High/Moderate/Low/Very Low)", example = "Very High", required = true)
    @NotNull(message = "Confidence level is required")
    private String confidenceLevel;

    @Schema(description = "Chart visualization data (JSON object)", nullable = true)
    private JsonNode chartData;
}
