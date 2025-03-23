package com.anonymize.examples;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.PhoneNumberDetector;
import com.anonymize.strategies.MaskAnonymizer;

import java.util.List;

import java.util.Arrays;

/**
 * Example demonstrating how to extend detectors with custom patterns and locales.
 */
public class ExtendingDetectorsExample {

    public static void main(String[] args) {
        System.out.println("===== Extending Phone Number Detector =====\n");
        
        // 1. Adding a custom pattern to an existing locale
        PhoneNumberDetector customizedDetector = new PhoneNumberDetector(Locale.US);
        
        // Add a pattern for vanity numbers (e.g., 1-800-FLOWERS)
        customizedDetector.addPattern("\\b1-\\d{3}-[A-Z]{7}\\b");
        
        // Test with regular and vanity numbers
        String vanityText = "Call us at 1-800-FLOWERS or 555-123-4567 to place an order.";
        
        // Create anonymizer with custom detector
        Anonymizer anonymizer = new Anonymizer.Builder()
                .withDetector(customizedDetector)
                .withAnonymizerStrategy(new MaskAnonymizer())
                .withLocale(Locale.US)
                .build();
                
        AnonymizationResult result = anonymizer.anonymize(vanityText);
        
        System.out.println("Example 1: Adding a custom pattern for vanity numbers");
        System.out.println("Original: " + vanityText);
        System.out.println("Anonymized: " + result.getAnonymizedText());
        System.out.println("Detected entities: " + result.getDetectionCount());
        for (PIIEntity entity : result.getDetectedEntities()) {
            System.out.println("  - " + entity.getText());
        }
        
        // 2. Creating a custom locale for a specific country or region
        System.out.println("\nExample 2: Creating a custom locale for Singapore");
        
        // Create a custom detector for Singapore phone numbers
        // Define a separate detector to highlight custom locale approach
        PhoneNumberDetector singaporeDetector = new PhoneNumberDetector(Locale.GENERIC);
        // Clear existing patterns that might interfere
        singaporeDetector.clearPatterns(Locale.GENERIC);
        // Add custom Singapore patterns and make them more explicit
        singaporeDetector.addPatterns(Arrays.asList(
                // Singapore mobile numbers (must match the entire number)
                "\\+65[\\s-]?[89]\\d{3}[\\s-]?\\d{4}",   // +65 9123 4567
                "\\b[89]\\d{3}[\\s-]?\\d{4}\\b",          // 9123 4567
                // Singapore landline numbers
                "\\+65[\\s-]?[36]\\d{3}[\\s-]?\\d{4}",   // +65 6123 4567
                "\\b[36]\\d{3}[\\s-]?\\d{4}\\b"           // 6123 4567
        ));
        
        String singaporeText = "Contact our Singapore office at +65 9123 4567 or 6123 4567.";
        
        Anonymizer sgAnonymizer = new Anonymizer.Builder()
                .withDetector(singaporeDetector)
                .withAnonymizerStrategy(new MaskAnonymizer())
                .build();
                
        AnonymizationResult sgResult = sgAnonymizer.anonymize(singaporeText);
        
        System.out.println("Original: " + singaporeText);
        System.out.println("Anonymized: " + sgResult.getAnonymizedText());
        System.out.println("Detected entities: " + sgResult.getDetectionCount());
        for (PIIEntity entity : sgResult.getDetectedEntities()) {
            System.out.println("  - " + entity.getText());
        }
        
        // 3. Overriding existing patterns
        System.out.println("\nExample 3: Replacing all patterns for a locale with custom ones");
        
        PhoneNumberDetector customizedUKDetector = new PhoneNumberDetector(Locale.UK);
        
        // Clear existing patterns and set our own
        customizedUKDetector.clearPatterns(Locale.UK);
        customizedUKDetector.addPatterns(Locale.UK, Arrays.asList(
                // UK mobile numbers only
                "\\b07\\d{9}\\b",                // 07911123456
                "\\b\\+44\\s?7\\d{9}\\b"         // +447911123456
        ));
        
        String ukText = "Call UK mobile 07911123456 or landline (0161) 999 8888.";
        
        Anonymizer ukAnonymizer = new Anonymizer.Builder()
                .withDetector(customizedUKDetector)
                .withAnonymizerStrategy(new MaskAnonymizer())
                .withLocale(Locale.UK)
                .build();
                
        AnonymizationResult ukResult = ukAnonymizer.anonymize(ukText);
        
        System.out.println("Original: " + ukText);
        System.out.println("Anonymized: " + ukResult.getAnonymizedText());
        System.out.println("Detected entities: " + ukResult.getDetectionCount());
        for (PIIEntity entity : ukResult.getDetectedEntities()) {
            System.out.println("  - " + entity.getText());
        }
        
        // Without custom patterns, both numbers would be detected
        PhoneNumberDetector standardUKDetector = new PhoneNumberDetector(Locale.UK);
        Anonymizer standardUKAnonymizer = new Anonymizer.Builder()
                .withDetector(standardUKDetector)
                .withAnonymizerStrategy(new MaskAnonymizer())
                .withLocale(Locale.UK)
                .build();
                
        AnonymizationResult standardUKResult = standardUKAnonymizer.anonymize(ukText);
        
        System.out.println("\nFor comparison, using default UK patterns:");
        System.out.println("Original: " + ukText);
        System.out.println("Anonymized: " + standardUKResult.getAnonymizedText());
        System.out.println("Detected entities: " + standardUKResult.getDetectionCount());
        for (PIIEntity entity : standardUKResult.getDetectedEntities()) {
            System.out.println("  - " + entity.getText());
        }
        
        // 4. Creating a subclass for more complex customization
        System.out.println("\nExample 4: Creating a custom detector class");
        
        CustomPhoneDetector customDetector = new CustomPhoneDetector();
        Anonymizer customAnonymizer = new Anonymizer.Builder()
                .withDetector(customDetector)
                .withAnonymizerStrategy(new MaskAnonymizer())
                .build();
                
        String mixedText = "Contact via US number 555-123-4567, UK number 07911123456, " +
                          "or extension x1234.";
                          
        AnonymizationResult customResult = customAnonymizer.anonymize(mixedText);
        
        System.out.println("Original: " + mixedText);
        System.out.println("Anonymized: " + customResult.getAnonymizedText());
        System.out.println("Detected entities: " + customResult.getDetectionCount());
        for (PIIEntity entity : customResult.getDetectedEntities()) {
            System.out.println("  - " + entity.getText() + " (Confidence: " + entity.getConfidence() + ")");
        }
    }
    
    /**
     * Example of a custom phone detector that extends PhoneNumberDetector.
     */
    static class CustomPhoneDetector extends PhoneNumberDetector {
        public CustomPhoneDetector() {
            super(Locale.US);
            
            // Add extension pattern with lower confidence
            addPattern("\\b[xX]\\d{3,5}\\b");  // x1234 or X1234
            
            // Add UK mobile pattern as well (make it capture the full number)
            addPattern(Locale.UK, "\\b07\\d{9}\\b");
            
            // Add US pattern to ensure proper detection
            addPattern(Locale.US, "\\d{3}[-.]\\d{3}[-.]\\d{4}");
            
            // Override detect method to modify confidence for certain patterns
            // (Not needed in this case since we're just using the base implementation)
        }
        
        @Override
        public List<PIIEntity> detect(String text) {
            List<PIIEntity> results = super.detect(text);
            
            // Post-process results (e.g., modify confidence for certain patterns)
            for (int i = 0; i < results.size(); i++) {
                PIIEntity entity = results.get(i);
                
                // Lower confidence for extensions
                if (entity.getText().startsWith("x") || entity.getText().startsWith("X")) {
                    // Replace with a new entity that has lower confidence
                    results.set(i, new PIIEntity(
                            entity.getType(),
                            entity.getStartPosition(),
                            entity.getEndPosition(),
                            entity.getText(),
                            0.6  // Lower confidence for extensions
                    ));
                }
            }
            
            return results;
        }
    }
}