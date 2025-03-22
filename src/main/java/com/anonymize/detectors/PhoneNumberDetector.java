package com.anonymize.detectors;

import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detector for phone numbers using regex pattern matching.
 */
public class PhoneNumberDetector implements Detector {
    // Define several patterns for different phone formats
    private static final String[] PHONE_PATTERNS = {
        "\\(\\d{3}\\)\\s*\\d{3}[-.]?\\d{4}", // (123) 456-7890 or (123)456-7890
        "\\d{3}[-.]\\d{3}[-.]\\d{4}",       // 123-456-7890 or 123.456.7890
        "\\d{10}",                           // 1234567890
        "\\+\\d{1,2}\\s*\\(\\d{3}\\)\\s*\\d{3}[-. ]?\\d{4}", // +1 (123) 456-7890
        "\\(\\d{3}\\)\\s+\\d{3}\\s+\\d{4}", // (123) 456 7890
        "\\d{3}\\s+\\d{3}\\s+\\d{4}"        // 123 456 7890
    };
    
    private static final List<Pattern> patterns = new ArrayList<>();
    static {
        for (String patternStr : PHONE_PATTERNS) {
            patterns.add(Pattern.compile(patternStr));
        }
    }
    
    private static final double DEFAULT_CONFIDENCE = 0.85;

    @Override
    public List<PIIEntity> detect(String text) {
        List<PIIEntity> results = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return results;
        }

        for (Pattern pattern : patterns) {
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
                    results.add(new PIIEntity(
                            PIIType.PHONE_NUMBER.getValue(),
                            start,
                            end,
                            match,
                            DEFAULT_CONFIDENCE
                    ));
                }
            }
        }
        
        return results;
    }

    @Override
    public String getType() {
        return PIIType.PHONE_NUMBER.getValue();
    }
}