package com.anonymize.detectors;

import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detector for email addresses using regex pattern matching.
 */
public class EmailDetector implements Detector {
    private static final String EMAIL_PATTERN = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private static final double DEFAULT_CONFIDENCE = 0.9;

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
            
            results.add(new PIIEntity(
                    PIIType.EMAIL.getValue(),
                    start,
                    end,
                    match,
                    DEFAULT_CONFIDENCE
            ));
        }
        
        return results;
    }

    @Override
    public String getType() {
        return PIIType.EMAIL.getValue();
    }
}