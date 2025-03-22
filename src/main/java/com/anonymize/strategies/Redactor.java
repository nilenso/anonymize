package com.anonymize.strategies;

import com.anonymize.common.PIIEntity;
import java.util.List;
import java.util.Map;

/**
 * Interface for redaction implementations that apply a redaction strategy to detected PII entities.
 */
public interface Redactor {
    /**
     * Redacts detected PII entities from the original text.
     *
     * @param text The original text
     * @param entities The list of detected PII entities
     * @return The redacted text
     */
    String redact(String text, List<PIIEntity> entities);
    
    /**
     * Gets the redaction strategy used by this redactor.
     *
     * @return The redaction strategy
     */
    RedactionStrategy getStrategy();
}