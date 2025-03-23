package com.anonymize.detectors;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;

import java.util.List;
import java.util.Set;

/**
 * Base class for all detectors that use machine learning models to identify PII entities.
 * This is a placeholder for future ML-based detection capabilities.
 */
public abstract class MLDetector extends AbstractDetector {
    
    /**
     * Creates a new MLDetector with the specified type, locale, and supported locales.
     *
     * @param type The type of PII this detector handles
     * @param locale The locale this detector is configured for
     * @param supportedLocales Set of locales supported by this detector
     */
    protected MLDetector(String type, Locale locale, Set<Locale> supportedLocales) {
        super(type, locale, supportedLocales);
    }
    
    /**
     * Creates a new MLDetector with the specified type and locale.
     *
     * @param type The type of PII this detector handles
     * @param locale The locale this detector is configured for
     */
    protected MLDetector(String type, Locale locale) {
        super(type, locale);
    }
    
    /**
     * Creates a new MLDetector with the specified type and the GENERIC locale.
     *
     * @param type The type of PII this detector handles
     */
    protected MLDetector(String type) {
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