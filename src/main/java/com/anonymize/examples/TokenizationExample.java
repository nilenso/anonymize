package com.anonymize.examples;

import com.anonymize.common.PIIEntity;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.EmailDetector;
import com.anonymize.detectors.PhoneNumberDetector;
import com.anonymize.strategies.TagAnonymizer;

/**
 * Example demonstrating tokenization anonymization strategy.
 */
public class TokenizationExample {

    public static void main(String[] args) {
        // Create an anonymizer with tokenization strategy
        Anonymizer anonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new TagAnonymizer())
                .build();
        
        // Example 1: Simple text
        String text1 = "Please contact me at john.doe@example.com or call (555) 123-4567";
        AnonymizationResult result1 = anonymizer.anonymize(text1);
        System.out.println("=== Example 1: Simple Text ===");
        System.out.println("Original: " + text1);
        System.out.println("Tokenized: " + result1.getAnonymizedText());
        System.out.println();
        
        // Example 2: Multiple instances of same type
        String text2 = "Email addresses: primary@example.com, secondary@example.com, backup@example.com";
        AnonymizationResult result2 = anonymizer.anonymize(text2);
        System.out.println("=== Example 2: Multiple Instances of Same Type ===");
        System.out.println("Original: " + text2);
        System.out.println("Tokenized: " + result2.getAnonymizedText());
        System.out.println();
        
        // Example 3: Mixed types
        String text3 = "Contact options: email1@example.com, (123) 456-7890, email2@example.com, (987) 654-3210";
        AnonymizationResult result3 = anonymizer.anonymize(text3);
        System.out.println("=== Example 3: Mixed Types ===");
        System.out.println("Original: " + text3);
        System.out.println("Tokenized: " + result3.getAnonymizedText());
        
        // Display entity mapping for example 3
        System.out.println("\nEntity Mapping:");
        for (PIIEntity entity : result3.getDetectedEntities()) {
            System.out.println(entity.getText() + " -> " + entity.getType() + " (" + 
                    entity.getStartPosition() + "-" + entity.getEndPosition() + ")");
        }
    }
}