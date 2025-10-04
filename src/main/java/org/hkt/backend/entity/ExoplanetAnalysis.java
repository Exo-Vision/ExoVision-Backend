package org.hkt.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exoplanet_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExoplanetAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orbital_period")
    private Double orbitalPeriod; // days

    @Column(name = "transit_duration")
    private Double transitDuration; // hours

    @Column(name = "transit_depth")
    private Double transitDepth; // percentage

    @Column(name = "snr")
    private Double snr; // signal-to-noise ratio

    @Column(name = "planet_radius")
    private Double planetRadius; // Earth radii

    @Column(name = "probability")
    private Double probability; // exoplanet probability %

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "f1_score")
    private Double f1Score;

    @Column(name = "`precision`")
    private Double precision;

    @Column(name = "recall")
    private Double recall;

    @Column(name = "false_positive_rate")
    private Double falsePositiveRate;

    @Column(name = "classification")
    private String classification;

    @Column(name = "confidence_level")
    private String confidenceLevel;

    @Column(name = "chart_data", columnDefinition = "TEXT")
    private String chartData; // JSON string containing all visualization data

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
