package com.anonymize.examples;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.BaseRegexDetector;
import com.anonymize.detectors.EmailDetector;
import com.anonymize.detectors.PhoneNumberDetector;
import com.anonymize.strategies.MaskAnonymizer;
import com.anonymize.strategies.TagAnonymizer;

import java.util.*;

/**
 * Example demonstrating the detector class hierarchy and how to create custom detectors.
 */
public class DetectorHierarchyExample {

    public static void main(String[] args) {
        System.out.println("===== Detector Hierarchy Example =====\n");
        
        // Example 1: Using built-in detectors with their default patterns
        System.out.println("Example 1: Using built-in detectors with default patterns");
        
        Anonymizer defaultAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector(Locale.US))
                .withAnonymizerStrategy(new MaskAnonymizer())
                .withLocale(Locale.US)
                .build();
                
        String text = "Contact John at john.doe@example.com or call (555) 123-4567";
        
        AnonymizationResult result = defaultAnonymizer.anonymize(text);
        System.out.println("Original: " + text);
        System.out.println("Anonymized: " + result.getAnonymizedText());
        System.out.println("Detected entities: " + result.getDetectionCount());
        for (PIIEntity entity : result.getDetectedEntities()) {
            System.out.println("  - " + entity.getType() + ": " + entity.getText());
        }
        
        // Example 2: Creating a custom detector using RegexDetector
        System.out.println("\nExample 2: Creating a custom Credit Card detector");
        
        // Create a custom detector for credit card numbers
        CreditCardDetector ccDetector = new CreditCardDetector();
        
        Anonymizer customAnonymizer = new Anonymizer.Builder()
                .withDetector(ccDetector)
                .withAnonymizerStrategy(new MaskAnonymizer())
                .build();
                
        String ccText = "Payment methods: VISA 4111-1111-1111-1111, AMEX 3714 496353 98431";
        
        AnonymizationResult ccResult = customAnonymizer.anonymize(ccText);
        System.out.println("Original: " + ccText);
        System.out.println("Anonymized: " + ccResult.getAnonymizedText());
        System.out.println("Detected entities: " + ccResult.getDetectionCount());
        for (PIIEntity entity : ccResult.getDetectedEntities()) {
            System.out.println("  - " + entity.getType() + ": " + entity.getText() + 
                    " (Confidence: " + entity.getConfidence() + ")");
        }
        
        // Example 3: Combining multiple detectors
        System.out.println("\nExample 3: Combining multiple detectors");
        
        Anonymizer combinedAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector(Locale.US))
                .withDetector(ccDetector)
                .withAnonymizerStrategy(new TagAnonymizer())
                .build();
                
        String combinedText = "Customer: john.doe@example.com, Phone: (555) 123-4567, " +
                "Card: 4111-1111-1111-1111";
                
        AnonymizationResult combinedResult = combinedAnonymizer.anonymize(combinedText);
        System.out.println("Original: " + combinedText);
        System.out.println("Anonymized: " + combinedResult.getAnonymizedText());
        System.out.println("Detected entities: " + combinedResult.getDetectionCount());
        for (PIIEntity entity : combinedResult.getDetectedEntities()) {
            System.out.println("  - " + entity.getType() + ": " + entity.getText());
        }
    }
    
    /**
     * Example custom detector for credit card numbers extending BaseRegexDetector.
     */
    static class CreditCardDetector extends BaseRegexDetector {
        private static final double DEFAULT_CONFIDENCE = 0.9;
        
        // Initialize pattern map for different card types
        private static Map<Locale, List<String>> initializeCardPatterns() {
            Map<Locale, List<String>> patterns = new HashMap<>();
            
            // Credit card patterns work across all locales
            List<String> cardPatterns = Arrays.asList(
                // Visa
                "\\b4[0-9]{12}(?:[0-9]{3})?\\b",
                "\\b4[0-9]{3}[-\\s]?[0-9]{4}[-\\s]?[0-9]{4}[-\\s]?[0-9]{4}\\b",
                
                // Mastercard
                "\\b5[1-5][0-9]{14}\\b",
                "\\b5[1-5][0-9]{2}[-\\s]?[0-9]{4}[-\\s]?[0-9]{4}[-\\s]?[0-9]{4}\\b",
                
                // American Express
                "\\b3[47][0-9]{13}\\b",
                "\\b3[47][0-9]{2}[-\\s]?[0-9]{6}[-\\s]?[0-9]{5}\\b"
            );
            
            // Add the same patterns for all locales
            for (Locale locale : Locale.values()) {
                patterns.put(locale, cardPatterns);
            }
            
            return patterns;
        }
        
        public CreditCardDetector() {
            super(PIIType.CREDIT_CARD.getValue(), DEFAULT_CONFIDENCE, initializeCardPatterns());
        }
        
        /**
         * Customized confidence calculation based on the type of card and presence of validation.
         */
        @Override
        protected double calculateConfidence(String match, String patternString) {
            // Remove spaces and dashes for processing
            String normalized = match.replaceAll("[-\\s]", "");
            
            // Apply Luhn algorithm to validate the card number
            if (isValidCardNumber(normalized)) {
                // Higher confidence for validated card numbers
                return 0.95;
            }
            
            // Default confidence for pattern matches
            return DEFAULT_CONFIDENCE;
        }
        
        /**
         * Validates a credit card number using the Luhn algorithm.
         * 
         * @param cardNumber The card number to validate
         * @return true if the card number passes the Luhn check
         */
        private boolean isValidCardNumber(String cardNumber) {
            int sum = 0;
            boolean alternate = false;
            
            for (int i = cardNumber.length() - 1; i >= 0; i--) {
                int digit = Character.getNumericValue(cardNumber.charAt(i));
                if (alternate) {
                    digit *= 2;
                    if (digit > 9) {
                        digit -= 9;
                    }
                }
                sum += digit;
                alternate = !alternate;
            }
            
            return sum % 10 == 0;
        }
    }
}