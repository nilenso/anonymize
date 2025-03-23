package com.anonymize.detectors;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIType;

import java.util.*;

/**
 * Detector for phone numbers using regex pattern matching.
 * Supports multiple locales with different phone formats.
 */
public class PhoneNumberDetector extends RegexDetector {
    
    // Default confidence level for phone number matches
    private static final double DEFAULT_CONFIDENCE = 0.85;
    
    // Map of locale-specific phone patterns
    private static final Map<Locale, List<String>> DEFAULT_PATTERNS = initializeDefaultPatterns();
    
    /**
     * Initialize the default patterns map for phone number detection.
     * 
     * @return Map of default patterns by locale
     */
    private static Map<Locale, List<String>> initializeDefaultPatterns() {
        Map<Locale, List<String>> patterns = new HashMap<>();
        
        // US phone patterns
        List<String> usPatterns = Arrays.asList(
            "\\(\\d{3}\\)\\s*\\d{3}[-.]?\\d{4}",  // (123) 456-7890 or (123)456-7890
            "\\d{3}[-.]\\d{3}[-.]\\d{4}",         // 123-456-7890 or 123.456.7890
            "\\d{10}",                             // 1234567890
            "\\+1\\s*\\(\\d{3}\\)\\s*\\d{3}[-. ]?\\d{4}", // +1 (123) 456-7890
            "\\(\\d{3}\\)\\s+\\d{3}\\s+\\d{4}",   // (123) 456 7890
            "\\d{3}\\s+\\d{3}\\s+\\d{4}"          // 123 456 7890
        );
        patterns.put(Locale.US, usPatterns);
        
        // UK phone patterns
        List<String> ukPatterns = Arrays.asList(
            "\\+44\\s?\\d{4}\\s?\\d{6}",          // +44 7911 123456
            "\\(0\\d{3,4}\\)\\s?\\d{3,4}\\s?\\d{4}", // (0161) 999 8888
            "0\\d{3,4}[- ]?\\d{3,4}[- ]?\\d{4}"   // 01619998888 or 0161-999-8888
        );
        patterns.put(Locale.UK, ukPatterns);
        
        // India phone patterns
        List<String> indiaPatterns = Arrays.asList(
            "\\+91[- ]?\\d{10}",                  // +91 9999999999
            "0\\d{10}",                           // 09999999999
            "\\d{5}[- ]?\\d{5}"                   // 99999 99999
        );
        patterns.put(Locale.INDIA, indiaPatterns);
        
        // Canada phone patterns (similar to US)
        patterns.put(Locale.CANADA, usPatterns);
        
        // Australia phone patterns
        List<String> auPatterns = Arrays.asList(
            "\\+61\\s?\\d{1}\\s?\\d{4}\\s?\\d{4}", // +61 4 1234 5678
            "0\\d{1}\\s?\\d{4}\\s?\\d{4}",        // 04 1234 5678
            "\\(0\\d{1}\\)\\s?\\d{4}\\s?\\d{4}"   // (04) 1234 5678
        );
        patterns.put(Locale.AUSTRALIA, auPatterns);
        
        // Generic international patterns
        List<String> genericPatterns = Arrays.asList(
            "\\+\\d{1,3}[- ]?\\d{3,14}",          // +XX XXXXXXXXXXXX
            "\\d{5,15}"                           // Basic digits-only pattern
        );
        patterns.put(Locale.GENERIC, genericPatterns);
        
        // EU patterns - combination of several European formats
        List<String> euPatterns = new ArrayList<>();
        // Germany
        euPatterns.add("\\+49[- ]?\\d{3,4}[- ]?\\d{5,8}");
        // France
        euPatterns.add("\\+33[- ]?\\d{1}[- ]?\\d{2}[- ]?\\d{2}[- ]?\\d{2}[- ]?\\d{2}");
        // Italy
        euPatterns.add("\\+39[- ]?\\d{2,4}[- ]?\\d{6,8}");
        // Spain
        euPatterns.add("\\+34[- ]?\\d{2}[- ]?\\d{3}[- ]?\\d{3}");
        patterns.put(Locale.EU, euPatterns);
        
        return patterns;
    }
    
    /**
     * Creates a PhoneNumberDetector for the specified locale.
     *
     * @param locale The locale to use for phone pattern matching
     */
    public PhoneNumberDetector(Locale locale) {
        super(PIIType.PHONE_NUMBER.getValue(), locale, getSupportedLocalesStatic(), 
              DEFAULT_CONFIDENCE, DEFAULT_PATTERNS);
    }
    
    /**
     * Creates a PhoneNumberDetector with the GENERIC locale.
     */
    public PhoneNumberDetector() {
        this(Locale.GENERIC);
    }
    
    /**
     * Creates a PhoneNumberDetector with custom patterns for a specific locale.
     *
     * @param locale The locale to use
     * @param patterns The custom patterns to use for this locale
     */
    public PhoneNumberDetector(Locale locale, List<String> patterns) {
        super(PIIType.PHONE_NUMBER.getValue(), locale, getSupportedLocalesStatic(), 
              DEFAULT_CONFIDENCE, DEFAULT_PATTERNS);
        
        // Override the specified locale with custom patterns if provided
        if (patterns != null && !patterns.isEmpty()) {
            setPatterns(locale, patterns);
        }
    }
    
    /**
     * Static method to get the set of locales supported by this detector.
     *
     * @return Set of supported locales
     */
    private static Set<Locale> getSupportedLocalesStatic() {
        return new HashSet<>(DEFAULT_PATTERNS.keySet());
    }
    
    /**
     * Specialized confidence calculation for phone numbers.
     * Could be enhanced to provide different confidence levels for different formats.
     */
    @Override
    protected double calculateConfidence(String match, String patternString) {
        // For now we use a fixed confidence value
        // This could be enhanced to adjust confidence based on pattern characteristics
        // For example, a complete phone number with country code might have higher confidence
        return DEFAULT_CONFIDENCE;
    }
}