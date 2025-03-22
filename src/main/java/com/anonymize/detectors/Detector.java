package com.anonymize.detectors;

import com.anonymize.common.PIIEntity;
import java.util.List;

/**
 * Interface for all PII entity detectors.
 */
public interface Detector {
    /**
     * Detects PII entities in the provided text.
     *
     * @param text The text to analyze for PII entities
     * @return A list of detected PII entities
     */
    List<PIIEntity> detect(String text);
    
    /**
     * Returns the type of PII this detector handles.
     *
     * @return The PII type this detector is focused on
     */
    String getType();
}