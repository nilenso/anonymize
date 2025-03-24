package com.anonymize.core;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;
import com.anonymize.detectors.EmailDetector;
import com.anonymize.detectors.PhoneNumberDetector;
import com.anonymize.strategies.MaskAnonymizer;
import com.anonymize.strategies.RemoveAnonymizer;
import com.anonymize.strategies.TagAnonymizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the core Anonymizer class and its builder pattern.
 */
public class AnonymizerTest {

    @Test
    public void testBasicBuilderConfiguration() {
        // Test creating an anonymizer with basic configuration
        Anonymizer anonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new MaskAnonymizer())
                .build();
        
        // Anonymizer should not be null
        assertNotNull(anonymizer, "Anonymizer should be built successfully");
        
        // Test basic anonymization works
        String testText = "Contact: test@example.com";
        AnonymizationResult result = anonymizer.anonymize(testText);
        
        // Should produce a valid result
        assertNotNull(result, "Should produce a non-null result");
        assertNotNull(result.getAnonymizedText(), "Should produce anonymized text");
        assertEquals(testText, result.getOriginalText(), "Original text should be preserved");
    }
    
    @Test
    public void testDefaultStrategy() {
        // Test that default strategy is MaskAnonymizer if none specified
        Anonymizer anonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .build(); // No strategy specified
        
        String testText = "Email: info@test.com";
        AnonymizationResult result = anonymizer.anonymize(testText);
        
        // Should use mask strategy by default (some form of masking)
        assertFalse(result.getAnonymizedText().contains("info@test.com"), 
                  "Email should be masked");
    }
    
    @Test
    public void testLocaleConfiguration() {
        // Test locale configuration is properly applied
        Anonymizer usAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withLocale(Locale.US)
                .build();
                
        // Test with US text
        String usText = "Contact: (555) 123-4567";
        AnonymizationResult usResult = usAnonymizer.anonymize(usText);
        
        // Verify result contains detection information
        assertNotNull(usResult.getDetectedEntities(), 
                    "Result should contain entity detection information");
    }
    
    @Test
    public void testMultipleDetectors() {
        // Test that multiple detectors work together
        Anonymizer multiDetectorAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new TagAnonymizer())
                .build();
                
        // Text with multiple PII types
        String multiText = "Contact me at user@example.com or call 555-123-4567";
        AnonymizationResult result = multiDetectorAnonymizer.anonymize(multiText);
        
        // Verify email detection
        boolean hasEmailEntity = false;
        for (PIIEntity entity : result.getDetectedEntities()) {
            if (entity.getType().equals(PIIType.EMAIL.getValue())) {
                hasEmailEntity = true;
                break;
            }
        }
        
        assertTrue(hasEmailEntity, "Should detect email entity");
    }
    
    @Test
    public void testStrategySwapping() {
        // Test using different strategies with the same detectors
        EmailDetector emailDetector = new EmailDetector();
        
        Anonymizer maskAnonymizer = new Anonymizer.Builder()
                .withDetector(emailDetector)
                .withAnonymizerStrategy(new MaskAnonymizer())
                .build();
                
        Anonymizer removeAnonymizer = new Anonymizer.Builder()
                .withDetector(emailDetector)
                .withAnonymizerStrategy(new RemoveAnonymizer())
                .build();
                
        Anonymizer tagAnonymizer = new Anonymizer.Builder()
                .withDetector(emailDetector)
                .withAnonymizerStrategy(new TagAnonymizer())
                .build();
                
        // Text with an email
        String testText = "Email: info@example.com";
        
        // Test different strategies on same text
        String masked = maskAnonymizer.anonymize(testText).getAnonymizedText();
        String removed = removeAnonymizer.anonymize(testText).getAnonymizedText();
        String tagged = tagAnonymizer.anonymize(testText).getAnonymizedText();
        
        // Each should handle the email appropriately
        assertFalse(masked.contains("info@example.com"), "Masked output should not contain original email");
        assertFalse(removed.contains("info@example.com"), "Removed output should not contain original email");
        assertFalse(tagged.contains("info@example.com"), "Tagged output should not contain original email");
    }
}