package com.anonymize.examples;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.EmailDetector;
import com.anonymize.detectors.PhoneNumberDetector;
import com.anonymize.strategies.MaskAnonymizer;

/**
 * Example demonstrating locale-specific detection.
 */
public class LocaleExample {

    public static void main(String[] args) {
        // Example texts with different phone number formats for various locales
        String usText = "Contact our US office: (555) 123-4567 or email us at info@example.com";
        String ukText = "Contact our UK office: +44 7911 123456 or email us at uk-info@example.com";
        String indiaText = "Contact our India office: +91 98765 43210 or email us at india-info@example.com";
        String euText = "Contact our Germany office: +49 30 12345678 or email us at eu-info@example.com";
        
        // Try each text with different locale configs
        System.out.println("===== Testing US Phone Number with Different Locales =====");
        processWithLocale(usText, Locale.US);
        processWithLocale(usText, Locale.UK);
        processWithLocale(usText, Locale.INDIA);
        processWithLocale(usText, Locale.GENERIC);
        
        System.out.println("\n===== Testing UK Phone Number with Different Locales =====");
        processWithLocale(ukText, Locale.UK);
        processWithLocale(ukText, Locale.US);
        processWithLocale(ukText, Locale.GENERIC);
        
        System.out.println("\n===== Testing India Phone Number with Different Locales =====");
        processWithLocale(indiaText, Locale.INDIA);
        processWithLocale(indiaText, Locale.US);
        processWithLocale(indiaText, Locale.GENERIC);
        
        System.out.println("\n===== Testing EU Phone Number with Different Locales =====");
        processWithLocale(euText, Locale.EU);
        processWithLocale(euText, Locale.US);
        processWithLocale(euText, Locale.GENERIC);
    }
    
    private static void processWithLocale(String text, Locale locale) {
        Anonymizer anonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector(locale))
                .withDetector(new PhoneNumberDetector(locale))
                .withAnonymizerStrategy(new MaskAnonymizer())
                .withLocale(locale)
                .build();
                
        AnonymizationResult result = anonymizer.anonymize(text);
        
        System.out.println("Locale: " + locale.getDisplayName());
        System.out.println("Original: " + text);
        System.out.println("Anonymized: " + result.getAnonymizedText());
        System.out.println("Detected Entities: " + result.getDetectionCount());
        
        for (PIIEntity entity : result.getDetectedEntities()) {
            System.out.println("  - " + entity.getType() + ": " + entity.getText());
        }
        
        System.out.println();
    }
}