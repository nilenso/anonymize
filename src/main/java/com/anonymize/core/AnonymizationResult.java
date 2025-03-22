package com.anonymize.core;

import com.anonymize.common.PIIEntity;
import com.anonymize.strategies.RedactionStrategy;

import java.util.List;

/**
 * Represents the result of an anonymization operation, including both the redacted text
 * and metadata about detected PII entities.
 */
public class AnonymizationResult {
    private final String originalText;
    private final String redactedText;
    private final List<PIIEntity> detectedEntities;
    private final RedactionStrategy strategyUsed;

    public AnonymizationResult(String originalText, String redactedText, 
                               List<PIIEntity> detectedEntities, 
                               RedactionStrategy strategyUsed) {
        this.originalText = originalText;
        this.redactedText = redactedText;
        this.detectedEntities = detectedEntities;
        this.strategyUsed = strategyUsed;
    }

    /**
     * Gets the original text before redaction.
     *
     * @return The original text
     */
    public String getOriginalText() {
        return originalText;
    }

    /**
     * Gets the redacted text after applying the redaction strategy.
     *
     * @return The redacted text
     */
    public String getRedactedText() {
        return redactedText;
    }

    /**
     * Gets a list of all detected PII entities in the original text.
     *
     * @return List of detected PII entities
     */
    public List<PIIEntity> getDetectedEntities() {
        return detectedEntities;
    }

    /**
     * Gets the redaction strategy that was applied.
     *
     * @return The redaction strategy used
     */
    public RedactionStrategy getStrategyUsed() {
        return strategyUsed;
    }

    /**
     * Checks if any PII entities were detected.
     *
     * @return true if PII entities were found, false otherwise
     */
    public boolean hasDetectedEntities() {
        return detectedEntities != null && !detectedEntities.isEmpty();
    }

    /**
     * Gets the count of detected PII entities.
     *
     * @return Number of detected entities
     */
    public int getDetectionCount() {
        return detectedEntities != null ? detectedEntities.size() : 0;
    }
}