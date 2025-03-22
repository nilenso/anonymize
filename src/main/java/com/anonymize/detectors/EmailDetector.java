package com.anonymize.detectors;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detector for email addresses using regex pattern matching.
 * Email format is generally consistent across locales, so this detector supports all locales.
 */
public class EmailDetector extends AbstractDetector {
    // Email regex is generally the same across locales
    private static final String EMAIL_PATTERN = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private static final double DEFAULT_CONFIDENCE = 0.9;

    /**
     * Creates a new EmailDetector with the specified locale.
     *
     * @param locale The locale to use
     */
    public EmailDetector(Locale locale) {
        super(PIIType.EMAIL.getValue(), locale, getSupportedLocalesStatic());
    }
    
    /**
     * Creates a new EmailDetector with the GENERIC locale.
     */
    public EmailDetector() {
        super(PIIType.EMAIL.getValue(), Locale.GENERIC, getSupportedLocalesStatic());
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

    @Override
    public List<PIIEntity> detect(String text) {
        List<PIIEntity> results = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return results;
        }

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String match = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
            
            results.add(createEntity(start, end, match, DEFAULT_CONFIDENCE));
        }
        
        return results;
    }
}