package org.hkt.backend.fixture;

import com.navercorp.fixturemonkey.FixtureMonkey;
import org.hkt.backend.dto.ExoplanetAnalysisRequest;
import org.hkt.backend.entity.ExoplanetAnalysis;

/**
 * Fixture factory for ExoplanetAnalysis test data
 */
public class ExoplanetAnalysisFixture {

    private static final FixtureMonkey fixtureMonkey = FixtureMonkeyConfig.getInstance();

    /**
     * Create valid ExoplanetAnalysis entity for testing
     */
    public static ExoplanetAnalysis createValidEntity() {
        return fixtureMonkey.giveMeBuilder(ExoplanetAnalysis.class)
                .set("id", null)  // Auto-generated
                .set("orbitalPeriod", 10.5)
                .set("transitDuration", 3.2)
                .set("transitDepth", 1.5)
                .set("snr", 15.0)
                .set("probability", 75.5)
                .set("accuracy", 0.85)
                .set("f1Score", 0.83)
                .set("precision", 0.88)
                .set("recall", 0.79)
                .set("falsePositiveRate", 0.05)
                .set("classification", "Strong Candidate")
                .set("confidenceLevel", "High")
                .set("createdAt", null)  // @PrePersist handles this
                .sample();
    }

    /**
     * Create confirmed exoplanet entity
     */
    public static ExoplanetAnalysis createConfirmedExoplanet() {
        return fixtureMonkey.giveMeBuilder(ExoplanetAnalysis.class)
                .set("id", null)
                .set("orbitalPeriod", 365.25)
                .set("transitDuration", 4.5)
                .set("transitDepth", 1.2)
                .set("snr", 20.0)
                .set("probability", 95.8)
                .set("accuracy", 0.95)
                .set("f1Score", 0.92)
                .set("precision", 0.93)
                .set("recall", 0.91)
                .set("falsePositiveRate", 0.02)
                .set("classification", "Confirmed Exoplanet")
                .set("confidenceLevel", "Very High")
                .set("createdAt", null)
                .sample();
    }

    /**
     * Create weak signal entity
     */
    public static ExoplanetAnalysis createWeakSignal() {
        return fixtureMonkey.giveMeBuilder(ExoplanetAnalysis.class)
                .set("id", null)
                .set("orbitalPeriod", 5.2)
                .set("transitDuration", 1.8)
                .set("transitDepth", 0.3)
                .set("snr", 5.0)
                .set("probability", 25.3)
                .set("accuracy", 0.65)
                .set("f1Score", 0.60)
                .set("precision", 0.62)
                .set("recall", 0.58)
                .set("falsePositiveRate", 0.15)
                .set("classification", "Weak Signal")
                .set("confidenceLevel", "Low")
                .set("createdAt", null)
                .sample();
    }

    /**
     * Create valid request DTO for testing
     */
    public static ExoplanetAnalysisRequest createValidRequest() {
        return fixtureMonkey.giveMeBuilder(ExoplanetAnalysisRequest.class)
                .set("orbitalPeriod", 10.5)
                .set("transitDuration", 3.2)
                .set("transitDepth", 1.5)
                .set("snr", 15.0)
                .set("probability", 75.5)
                .set("accuracy", 0.85)
                .set("f1Score", 0.83)
                .set("precision", 0.88)
                .set("recall", 0.79)
                .set("falsePositiveRate", 0.05)
                .set("classification", "Strong Candidate")
                .set("confidenceLevel", "High")
                .sample();
    }

    /**
     * Create custom entity with specific probability
     */
    public static ExoplanetAnalysis createWithProbability(double probability) {
        String classification;
        String confidenceLevel;

        if (probability >= 80) {
            classification = "Confirmed Exoplanet";
            confidenceLevel = "Very High";
        } else if (probability >= 60) {
            classification = "Strong Candidate";
            confidenceLevel = "High";
        } else if (probability >= 40) {
            classification = "Potential Candidate";
            confidenceLevel = "Medium";
        } else if (probability >= 20) {
            classification = "Weak Signal";
            confidenceLevel = "Low";
        } else {
            classification = "Unlikely Detection";
            confidenceLevel = "Very Low";
        }

        return fixtureMonkey.giveMeBuilder(ExoplanetAnalysis.class)
                .set("id", null)
                .set("probability", probability)
                .set("classification", classification)
                .set("confidenceLevel", confidenceLevel)
                .set("createdAt", null)
                .sample();
    }
}
