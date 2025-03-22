package com.anonymize.detectors;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import java.util.List;
import java.util.Set;

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
    
    /**
     * Returns the set of locales supported by this detector.
     *
     * @return Set of supported locales
     */
    Set<Locale> getSupportedLocales();
    
    /**
     * Checks if this detector supports the specified locale.
     *
     * @param locale The locale to check
     * @return true if the locale is supported, false otherwise
     */
    default boolean supportsLocale(Locale locale) {
        return getSupportedLocales().contains(locale) || 
               getSupportedLocales().contains(Locale.GENERIC);
    }
    
    /**
     * Gets the locale this detector is configured for.
     *
     * @return The current locale
     */
    Locale getLocale();
}