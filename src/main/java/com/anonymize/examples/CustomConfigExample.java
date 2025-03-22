package com.anonymize.examples;

import com.anonymize.common.PIIEntity;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.EmailDetector;
import com.anonymize.detectors.PhoneNumberDetector;
import com.anonymize.strategies.MaskRedactor;
import com.anonymize.strategies.TokenizeRedactor;

/**
 * Example demonstrating custom configurations for the Anonymize library.
 */
public class CustomConfigExample {

    public static void main(String[] args) {
        // Sample text with multiple PII entities
        String text = "Dear John Doe,\n\n" +
                "Thank you for contacting our support team. We have received your inquiry " +
                "sent from john.doe@example.com.\n\n" +
                "Our representative will call you back at (555) 123-4567 within 24 hours.\n\n" +
                "Regards,\n" +
                "Customer Support";
        
        // Example 1: Default configuration with basic detectors
        Anonymizer defaultAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .build(); // Default is masking
        
        AnonymizationResult defaultResult = defaultAnonymizer.anonymize(text);
        
        System.out.println("=== Default Configuration (Masking) ===");
        System.out.println(defaultResult.getRedactedText());
        System.out.println("Detected: " + defaultResult.getDetectionCount() + " entities\n");
        
        // Example 2: Custom masking text
        Anonymizer customMaskAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withRedactor(new MaskRedactor("[PII REMOVED]"))
                .build();
        
        AnonymizationResult customMaskResult = customMaskAnonymizer.anonymize(text);
        
        System.out.println("=== Custom Mask Text ===");
        System.out.println(customMaskResult.getRedactedText());
        System.out.println("Detected: " + customMaskResult.getDetectionCount() + " entities\n");
        
        // Example 3: Custom tokenization format
        Anonymizer customTokenizeAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withRedactor(new TokenizeRedactor("{%s_%d}"))
                .build();
        
        AnonymizationResult customTokenizeResult = customTokenizeAnonymizer.anonymize(text);
        
        System.out.println("=== Custom Tokenization Format ===");
        System.out.println(customTokenizeResult.getRedactedText());
        System.out.println("Detected: " + customTokenizeResult.getDetectionCount() + " entities\n");
        
        // Example 4: Display detailed entity information
        System.out.println("=== Detailed Entity Information ===");
        for (PIIEntity entity : defaultResult.getDetectedEntities()) {
            System.out.println(String.format(
                    "Type: %s, Text: %s, Position: %d-%d, Confidence: %.2f",
                    entity.getType(), 
                    entity.getText(),
                    entity.getStartPosition(),
                    entity.getEndPosition(),
                    entity.getConfidence()
            ));
        }
    }
}