package com.anonymize.detectors;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detector for phone numbers using regex pattern matching.
 * Supports multiple locales with different phone formats.
 */
public class PhoneNumberDetector extends AbstractDetector {
    
    // Map of locale-specific phone patterns
    private static final Map<Locale, List<String>> LOCALE_PATTERNS = new HashMap<>();
    
    static {
        // US phone patterns
        List<String> usPatterns = Arrays.asList(
            "\\(\\d{3}\\)\\s*\\d{3}[-.]?\\d{4}",  // (123) 456-7890 or (123)456-7890
            "\\d{3}[-.]\\d{3}[-.]\\d{4}",         // 123-456-7890 or 123.456.7890
            "\\d{10}",                             // 1234567890
            "\\+1\\s*\\(\\d{3}\\)\\s*\\d{3}[-. ]?\\d{4}", // +1 (123) 456-7890
            "\\(\\d{3}\\)\\s+\\d{3}\\s+\\d{4}",   // (123) 456 7890
            "\\d{3}\\s+\\d{3}\\s+\\d{4}"          // 123 456 7890
        );
        LOCALE_PATTERNS.put(Locale.US, usPatterns);
        
        // UK phone patterns
        List<String> ukPatterns = Arrays.asList(
            "\\+44\\s?\\d{4}\\s?\\d{6}",          // +44 7911 123456
            "\\(0\\d{3,4}\\)\\s?\\d{3,4}\\s?\\d{4}", // (0161) 999 8888
            "0\\d{3,4}[- ]?\\d{3,4}[- ]?\\d{4}"   // 01619998888 or 0161-999-8888
        );
        LOCALE_PATTERNS.put(Locale.UK, ukPatterns);
        
        // India phone patterns
        List<String> indiaPatterns = Arrays.asList(
            "\\+91[- ]?\\d{10}",                  // +91 9999999999
            "0\\d{10}",                           // 09999999999
            "\\d{5}[- ]?\\d{5}"                   // 99999 99999
        );
        LOCALE_PATTERNS.put(Locale.INDIA, indiaPatterns);
        
        // Canada phone patterns (similar to US)
        LOCALE_PATTERNS.put(Locale.CANADA, usPatterns);
        
        // Australia phone patterns
        List<String> auPatterns = Arrays.asList(
            "\\+61\\s?\\d{1}\\s?\\d{4}\\s?\\d{4}", // +61 4 1234 5678
            "0\\d{1}\\s?\\d{4}\\s?\\d{4}",        // 04 1234 5678
            "\\(0\\d{1}\\)\\s?\\d{4}\\s?\\d{4}"   // (04) 1234 5678
        );
        LOCALE_PATTERNS.put(Locale.AUSTRALIA, auPatterns);
        
        // Generic international patterns
        List<String> genericPatterns = Arrays.asList(
            "\\+\\d{1,3}[- ]?\\d{3,14}",          // +XX XXXXXXXXXXXX
            "\\d{5,15}"                           // Basic digits-only pattern
        );
        LOCALE_PATTERNS.put(Locale.GENERIC, genericPatterns);
        
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
        LOCALE_PATTERNS.put(Locale.EU, euPatterns);
    }
    
    private final List<Pattern> compiledPatterns = new ArrayList<>();
    private static final double DEFAULT_CONFIDENCE = 0.85;
    
    /**
     * Creates a PhoneNumberDetector for the specified locale.
     *
     * @param locale The locale to use for phone pattern matching
     */
    public PhoneNumberDetector(Locale locale) {
        super(PIIType.PHONE_NUMBER.getValue(), locale, getSupportedLocalesStatic());
        initializePatterns();
    }
    
    /**
     * Creates a PhoneNumberDetector with the GENERIC locale.
     */
    public PhoneNumberDetector() {
        super(PIIType.PHONE_NUMBER.getValue(), Locale.GENERIC, getSupportedLocalesStatic());
        initializePatterns();
    }
    
    /**
     * Static method to get the set of locales supported by this detector.
     *
     * @return Set of supported locales
     */
    private static Set<Locale> getSupportedLocalesStatic() {
        return new HashSet<>(LOCALE_PATTERNS.keySet());
    }
    
    /**
     * Initialize patterns based on the configured locale.
     */
    private void initializePatterns() {
        compiledPatterns.clear();
        
        // Add patterns for the current locale
        List<String> localePatterns = LOCALE_PATTERNS.get(getLocale());
        if (localePatterns != null) {
            for (String pattern : localePatterns) {
                compiledPatterns.add(Pattern.compile(pattern));
            }
        }
        
        // Also add generic patterns if not already using the GENERIC locale
        if (getLocale() != Locale.GENERIC) {
            List<String> genericPatterns = LOCALE_PATTERNS.get(Locale.GENERIC);
            if (genericPatterns != null) {
                for (String pattern : genericPatterns) {
                    compiledPatterns.add(Pattern.compile(pattern));
                }
            }
        }
    }

    @Override
    public List<PIIEntity> detect(String text) {
        List<PIIEntity> results = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return results;
        }

        for (Pattern pattern : compiledPatterns) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String match = matcher.group();
                int start = matcher.start();
                int end = matcher.end();
                
                // Check if this match overlaps with any existing match
                boolean overlaps = false;
                for (PIIEntity existing : results) {
                    if ((start >= existing.getStartPosition() && start < existing.getEndPosition()) ||
                        (end > existing.getStartPosition() && end <= existing.getEndPosition())) {
                        overlaps = true;
                        break;
                    }
                }
                
                if (!overlaps) {
                    results.add(createEntity(start, end, match, DEFAULT_CONFIDENCE));
                }
            }
        }
        
        return results;
    }
}