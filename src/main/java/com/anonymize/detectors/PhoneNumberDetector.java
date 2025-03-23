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
    private static final Map<Locale, List<String>> DEFAULT_LOCALE_PATTERNS = new HashMap<>();
    
    // Instance map that can be modified by users
    private final Map<Locale, List<String>> localePatterns = new HashMap<>();
    
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
        DEFAULT_LOCALE_PATTERNS.put(Locale.US, usPatterns);
        
        // UK phone patterns
        List<String> ukPatterns = Arrays.asList(
            "\\+44\\s?\\d{4}\\s?\\d{6}",          // +44 7911 123456
            "\\(0\\d{3,4}\\)\\s?\\d{3,4}\\s?\\d{4}", // (0161) 999 8888
            "0\\d{3,4}[- ]?\\d{3,4}[- ]?\\d{4}"   // 01619998888 or 0161-999-8888
        );
        DEFAULT_LOCALE_PATTERNS.put(Locale.UK, ukPatterns);
        
        // India phone patterns
        List<String> indiaPatterns = Arrays.asList(
            "\\+91[- ]?\\d{10}",                  // +91 9999999999
            "0\\d{10}",                           // 09999999999
            "\\d{5}[- ]?\\d{5}"                   // 99999 99999
        );
        DEFAULT_LOCALE_PATTERNS.put(Locale.INDIA, indiaPatterns);
        
        // Canada phone patterns (similar to US)
        DEFAULT_LOCALE_PATTERNS.put(Locale.CANADA, usPatterns);
        
        // Australia phone patterns
        List<String> auPatterns = Arrays.asList(
            "\\+61\\s?\\d{1}\\s?\\d{4}\\s?\\d{4}", // +61 4 1234 5678
            "0\\d{1}\\s?\\d{4}\\s?\\d{4}",        // 04 1234 5678
            "\\(0\\d{1}\\)\\s?\\d{4}\\s?\\d{4}"   // (04) 1234 5678
        );
        DEFAULT_LOCALE_PATTERNS.put(Locale.AUSTRALIA, auPatterns);
        
        // Generic international patterns
        List<String> genericPatterns = Arrays.asList(
            "\\+\\d{1,3}[- ]?\\d{3,14}",          // +XX XXXXXXXXXXXX
            "\\d{5,15}"                           // Basic digits-only pattern
        );
        DEFAULT_LOCALE_PATTERNS.put(Locale.GENERIC, genericPatterns);
        
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
        DEFAULT_LOCALE_PATTERNS.put(Locale.EU, euPatterns);
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
        // Initialize with default patterns
        for (Map.Entry<Locale, List<String>> entry : DEFAULT_LOCALE_PATTERNS.entrySet()) {
            localePatterns.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        initializePatterns();
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
        super(PIIType.PHONE_NUMBER.getValue(), locale, getSupportedLocalesStatic());
        
        // Initialize with default patterns first
        for (Map.Entry<Locale, List<String>> entry : DEFAULT_LOCALE_PATTERNS.entrySet()) {
            localePatterns.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        
        // Then override the specified locale with custom patterns
        if (patterns != null && !patterns.isEmpty()) {
            localePatterns.put(locale, new ArrayList<>(patterns));
        }
        
        initializePatterns();
    }
    
    /**
     * Static method to get the set of locales supported by this detector.
     *
     * @return Set of supported locales
     */
    private static Set<Locale> getSupportedLocalesStatic() {
        return new HashSet<>(DEFAULT_LOCALE_PATTERNS.keySet());
    }
    
    /**
     * Gets the currently supported locales for this detector instance.
     * This includes both default locales and any custom locales added.
     *
     * @return Set of supported locales
     */
    public Set<Locale> getSupportedLocales() {
        Set<Locale> supportedLocales = new HashSet<>(super.getSupportedLocales());
        supportedLocales.addAll(localePatterns.keySet());
        return supportedLocales;
    }
    
    /**
     * Add a custom pattern for the current locale.
     *
     * @param pattern The regex pattern string to add
     * @return This detector instance for method chaining
     */
    public PhoneNumberDetector addPattern(String pattern) {
        return addPattern(getLocale(), pattern);
    }
    
    /**
     * Add a custom pattern for a specific locale.
     *
     * @param locale The locale to add the pattern for
     * @param pattern The regex pattern string to add
     * @return This detector instance for method chaining
     */
    public PhoneNumberDetector addPattern(Locale locale, String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return this;
        }
        
        // Make sure the locale exists in our map
        if (!localePatterns.containsKey(locale)) {
            localePatterns.put(locale, new ArrayList<>());
        }
        
        localePatterns.get(locale).add(pattern);
        
        // Reinitialize patterns since we've made changes
        initializePatterns();
        
        return this;
    }
    
    /**
     * Add multiple custom patterns for the current locale.
     *
     * @param patterns The list of regex pattern strings to add
     * @return This detector instance for method chaining
     */
    public PhoneNumberDetector addPatterns(List<String> patterns) {
        return addPatterns(getLocale(), patterns);
    }
    
    /**
     * Add multiple custom patterns for a specific locale.
     *
     * @param locale The locale to add patterns for
     * @param patterns The list of regex pattern strings to add
     * @return This detector instance for method chaining
     */
    public PhoneNumberDetector addPatterns(Locale locale, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return this;
        }
        
        // Make sure the locale exists in our map
        if (!localePatterns.containsKey(locale)) {
            localePatterns.put(locale, new ArrayList<>());
        }
        
        localePatterns.get(locale).addAll(patterns);
        
        // Reinitialize patterns since we've made changes
        initializePatterns();
        
        return this;
    }
    
    /**
     * Clear all patterns for a specific locale.
     *
     * @param locale The locale to clear patterns for
     * @return This detector instance for method chaining
     */
    public PhoneNumberDetector clearPatterns(Locale locale) {
        if (localePatterns.containsKey(locale)) {
            localePatterns.get(locale).clear();
            
            // Reinitialize patterns since we've made changes
            initializePatterns();
        }
        
        return this;
    }
    
    /**
     * Replace all patterns for a specific locale.
     *
     * @param locale The locale to set patterns for
     * @param patterns The new patterns to use
     * @return This detector instance for method chaining
     */
    public PhoneNumberDetector setPatterns(Locale locale, List<String> patterns) {
        if (patterns == null) {
            return this;
        }
        
        localePatterns.put(locale, new ArrayList<>(patterns));
        
        // Reinitialize patterns since we've made changes
        initializePatterns();
        
        return this;
    }
    
    /**
     * Create a new locale with custom patterns.
     *
     * @param locale The new locale to create
     * @param patterns The patterns for this locale
     * @return This detector instance for method chaining
     */
    public PhoneNumberDetector addLocale(Locale locale, List<String> patterns) {
        if (locale == null || patterns == null || patterns.isEmpty()) {
            return this;
        }
        
        localePatterns.put(locale, new ArrayList<>(patterns));
        
        // Reinitialize patterns since we've made changes
        initializePatterns();
        
        return this;
    }
    
    /**
     * Initialize patterns based on the configured locale.
     */
    private void initializePatterns() {
        compiledPatterns.clear();
        
        // Add patterns for the current locale
        List<String> currentLocalePatterns = localePatterns.get(getLocale());
        if (currentLocalePatterns != null) {
            for (String pattern : currentLocalePatterns) {
                try {
                    compiledPatterns.add(Pattern.compile(pattern));
                } catch (Exception e) {
                    // Skip invalid patterns
                    System.err.println("Invalid pattern: " + pattern + " - " + e.getMessage());
                }
            }
        }
        
        // Also add generic patterns if not already using the GENERIC locale
        if (getLocale() != Locale.GENERIC) {
            List<String> genericPatterns = localePatterns.get(Locale.GENERIC);
            if (genericPatterns != null) {
                for (String pattern : genericPatterns) {
                    try {
                        compiledPatterns.add(Pattern.compile(pattern));
                    } catch (Exception e) {
                        // Skip invalid patterns
                        System.err.println("Invalid pattern: " + pattern + " - " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Gets the patterns for a specific locale.
     *
     * @param locale The locale to get patterns for
     * @return List of pattern strings for the locale, or empty list if none exist
     */
    public List<String> getPatternsForLocale(Locale locale) {
        List<String> patterns = localePatterns.get(locale);
        return patterns != null ? new ArrayList<>(patterns) : new ArrayList<>();
    }
    
    /**
     * Gets all patterns for the current locale.
     *
     * @return List of pattern strings for the current locale
     */
    public List<String> getPatterns() {
        return getPatternsForLocale(getLocale());
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