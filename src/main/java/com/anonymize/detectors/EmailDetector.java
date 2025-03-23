package com.anonymize.detectors;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detector for email addresses using regex pattern matching.
 * Email format is generally consistent across locales, so this detector supports all locales.
 */
public class EmailDetector extends RegexDetector {
    // Default confidence level for email matches
    private static final double DEFAULT_CONFIDENCE = 0.9;
    
    // Default email pattern that works across all locales
    private static final String DEFAULT_EMAIL_PATTERN = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
    
    // Static initialization of default patterns
    private static final Map<Locale, List<String>> DEFAULT_PATTERNS = initializeDefaultPatterns();
    
    /**
     * Initialize the default patterns map for email detection.
     * 
     * @return Map of default patterns by locale
     */
    private static Map<Locale, List<String>> initializeDefaultPatterns() {
        Map<Locale, List<String>> patterns = new HashMap<>();
        
        // For emails, we use the same pattern across all locales
        for (Locale locale : Locale.values()) {
            patterns.put(locale, List.of(DEFAULT_EMAIL_PATTERN));
        }
        
        return patterns;
    }

    /**
     * Creates a new EmailDetector with the specified locale.
     *
     * @param locale The locale to use
     */
    public EmailDetector(Locale locale) {
        super(PIIType.EMAIL.getValue(), locale, getSupportedLocalesStatic(), DEFAULT_CONFIDENCE, DEFAULT_PATTERNS);
    }
    
    /**
     * Creates a new EmailDetector with the GENERIC locale.
     */
    public EmailDetector() {
        this(Locale.GENERIC);
    }
    
    /**
     * Static method to get the set of locales supported by this detector.
     *
     * @return Set of supported locales
     */
    private static Set<Locale> getSupportedLocalesStatic() {
        Set<Locale> supportedLocales = new HashSet<>();
        // Email format is standardized globally, so we support all locales
        for (Locale locale : Locale.values()) {
            supportedLocales.add(locale);
        }
        return supportedLocales;
    }
    
    /**
     * Specialized confidence calculation method for email addresses.
     * Could be enhanced to provide higher confidence for specific domain types, etc.
     */
    @Override
    protected double calculateConfidence(String match, String patternString) {
        // For now, we use a fixed confidence value
        // This could be enhanced to apply different confidence levels based on
        // email domains, format specifics, etc.
        return DEFAULT_CONFIDENCE;
    }
}