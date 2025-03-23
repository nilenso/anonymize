package com.anonymize.strategies;

import com.anonymize.common.PIIEntity;
import java.util.List;

/**
 * Interface for anonymization implementations that apply an anonymization strategy to detected PII entities.
 */
public interface AnonymizerStrategy {
    /**
     * Anonymizes detected PII entities from the original text.
     *
     * @param text The original text
     * @param entities The list of detected PII entities
     * @return The anonymized text
     */
    String anonymize(String text, List<PIIEntity> entities);
    
    /**
     * Gets a descriptive name for this anonymization strategy.
     *
     * @return The strategy name
     */
    String getStrategyName();
}