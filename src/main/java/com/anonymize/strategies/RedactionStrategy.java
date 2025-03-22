package com.anonymize.strategies;

/**
 * Enumeration of different redaction strategies that can be applied to PII entities.
 */
public enum RedactionStrategy {
    /**
     * Replace the PII entity with a fixed mask (e.g., [REDACTED]).
     */
    MASK,
    
    /**
     * Remove the PII entity from the text entirely.
     */
    REMOVE,
    
    /**
     * Replace the PII entity with a token (e.g., <PERSON_1>).
     */
    TOKENIZE
}