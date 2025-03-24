package com.anonymize.detectors;

import com.anonymize.common.Locale;
import java.util.Set;

/**
 * Base class for all detectors that use Named Entity Recognition (NER) models to identify PII entities.
 * This provides a foundation for implementing machine learning-based detection capabilities.
 */
public abstract class BaseNERDetector extends AbstractDetector {
    
    /**
     * Creates a new BaseNERDetector with the specified type, locale, and supported locales.
     *
     * @param type The type of PII this detector handles
     * @param locale The locale this detector is configured for
     * @param supportedLocales Set of locales supported by this detector
     */
    protected BaseNERDetector(String type, Locale locale, Set<Locale> supportedLocales) {
        super(type, locale, supportedLocales);
    }
    
    /**
     * Creates a new BaseNERDetector with the specified type and locale.
     *
     * @param type The type of PII this detector handles
     * @param locale The locale this detector is configured for
     */
    protected BaseNERDetector(String type, Locale locale) {
        super(type, locale);
    }
    
    /**
     * Creates a new BaseNERDetector with the specified type and the GENERIC locale.
     *
     * @param type The type of PII this detector handles
     */
    protected BaseNERDetector(String type) {
        super(type);
    }
    
    /**
     * Loads the ML model for the current locale.
     * Implementations should load the appropriate model based on the locale.
     */
    protected abstract void loadModel();
    
    /**
     * Gets the confidence threshold for this detector.
     * Entities with confidence below this threshold will be filtered out.
     *
     * @return The confidence threshold (0.0-1.0)
     */
    protected abstract double getConfidenceThreshold();
}