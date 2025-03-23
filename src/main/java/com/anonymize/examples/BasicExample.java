package com.anonymize.examples;

import com.anonymize.common.PIIEntity;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.EmailDetector;
import com.anonymize.detectors.PhoneNumberDetector;
import com.anonymize.strategies.MaskAnonymizer;
import com.anonymize.strategies.RemoveAnonymizer;
import com.anonymize.strategies.TagAnonymizer;

/**
 * Simple example demonstrating the basic usage of the Anonymize library.
 */
public class BasicExample {

    public static void main(String[] args) {
        // Create an anonymizer with email and phone number detection
        Anonymizer anonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new MaskAnonymizer()) // Default is MaskAnonymizer
                .build();
        
        // Sample text with PII
        String text = "Please contact support at support@example.com or call our helpline at (800) 123-4567.";
        
        // Anonymize the text
        AnonymizationResult result = anonymizer.anonymize(text);
        
        // Display results
        System.out.println("Original Text: " + result.getOriginalText());
        System.out.println("Anonymized Text: " + result.getAnonymizedText());
        System.out.println("Strategy Used: " + result.getStrategyUsed());
        System.out.println("Number of PII Entities Detected: " + result.getDetectionCount());
        
        // Display detailed information about detected entities
        System.out.println("\nDetected Entities:");
        for (PIIEntity entity : result.getDetectedEntities()) {
            System.out.println("  Type: " + entity.getType());
            System.out.println("  Value: " + entity.getText());
            System.out.println("  Position: " + entity.getStartPosition() + "-" + entity.getEndPosition());
            System.out.println("  Confidence: " + entity.getConfidence());
            System.out.println();
        }
        
        // Demonstrate different anonymization strategies
        demonstrateAnonymizationStrategies(text);
    }
    
    private static void demonstrateAnonymizationStrategies(String text) {
        System.out.println("\n=== Different Anonymization Strategies ===");
        System.out.println("Original Text: " + text);
        
        // MASK strategy
        Anonymizer maskAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new MaskAnonymizer())
                .build();
        System.out.println("MASK: " + maskAnonymizer.anonymize(text).getAnonymizedText());
        
        // REMOVE strategy
        Anonymizer removeAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new RemoveAnonymizer())
                .build();
        System.out.println("REMOVE: " + removeAnonymizer.anonymize(text).getAnonymizedText());
        
        // TOKENIZE strategy
        Anonymizer tokenizeAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new TagAnonymizer())
                .build();
        System.out.println("TOKENIZE: " + tokenizeAnonymizer.anonymize(text).getAnonymizedText());
    }
}