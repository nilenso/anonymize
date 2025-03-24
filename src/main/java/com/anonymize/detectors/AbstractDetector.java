package com.anonymize.detectors;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for PII entity detectors that provides common functionality.
 */
public abstract class AbstractDetector implements Detector {
    
    private final String type;
    private final Locale locale;
    private final Set<Locale> supportedLocales;
    
    /**
     * Creates a new detector for the specified entity type and locale.
     *
     * @param type The PII entity type this detector handles
     * @param locale The locale this detector is configured for
     * @param supportedLocales Set of locales supported by this detector
     */
    protected AbstractDetector(String type, Locale locale, Set<Locale> supportedLocales) {
        this.type = type;
        
        // If the specified locale is not supported, fall back to GENERIC
        if (supportedLocales.contains(locale)) {
            this.locale = locale;
        } else {
            this.locale = Locale.GENERIC;
        }
        
        this.supportedLocales = Collections.unmodifiableSet(new HashSet<>(supportedLocales));
    }
    
    /**
     * Creates a new detector for the specified entity type with GENERIC locale.
     *
     * @param type The PII entity type this detector handles
     */
    protected AbstractDetector(String type) {
        this.type = type;
        this.locale = Locale.GENERIC;
        Set<Locale> locales = new HashSet<>();
        locales.add(Locale.GENERIC);
        this.supportedLocales = Collections.unmodifiableSet(locales);
    }
    
    /**
     * Creates a new detector for the specified entity type with the specified locale.
     *
     * @param type The PII entity type this detector handles
     * @param locale The locale this detector is configured for
     */
    protected AbstractDetector(String type, Locale locale) {
        this.type = type;
        this.locale = locale;
        Set<Locale> locales = new HashSet<>();
        locales.add(locale);
        locales.add(Locale.GENERIC);
        this.supportedLocales = Collections.unmodifiableSet(locales);
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public Locale getLocale() {
        return locale;
    }
    
    @Override
    public Set<Locale> getSupportedLocales() {
        return supportedLocales;
    }
    
    /**
     * Helper method to create a PIIEntity with the detector's type.
     *
     * @param startPosition Start position of the entity in the text
     * @param endPosition End position of the entity in the text
     * @param text The detected text
     * @param confidence Confidence level of the detection
     * @return A new PIIEntity
     */
    protected PIIEntity createEntity(int startPosition, int endPosition, String text, double confidence) {
        return new PIIEntity(type, startPosition, endPosition, text, confidence);
    }
}