package com.anonymize.examples;

import com.anonymize.common.PIIEntity;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.PhoneNumberDetector;
import com.anonymize.strategies.MaskAnonymizer;

/**
 * Example demonstrating phone number detection with various formats.
 */
public class PhoneDetectionExample {

    public static void main(String[] args) {
        // Create an anonymizer with only phone number detection
        Anonymizer anonymizer = new Anonymizer.Builder()
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new MaskAnonymizer())
                .build();
        
        // Test various phone number formats
        String[] phoneExamples = {
            "Standard format: (123) 456-7890",
            "Dashed format: 123-456-7890",
            "Dotted format: 123.456.7890",
            "Simple format: 1234567890",
            "With country code: +1 (123) 456-7890",
            "Mixed format 1: (123)456-7890",
            "Mixed format 2: (123) 456 7890",
            "Not a phone: 12-34-56"
        };
        
        for (String example : phoneExamples) {
            AnonymizationResult result = anonymizer.anonymize(example);
            
            System.out.println("Original: " + example);
            System.out.println("Anonymized: " + result.getAnonymizedText());
            
            if (result.hasDetectedEntities()) {
                System.out.println("Detected Entities:");
                for (PIIEntity entity : result.getDetectedEntities()) {
                    System.out.println("  • " + entity.getText() + " (" + entity.getStartPosition() + "-" + entity.getEndPosition() + ")");
                }
            } else {
                System.out.println("No phone numbers detected");
            }
            
            System.out.println("-----------------------");
        }
    }
}