package com.anonymize.strategies;

import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.EmailDetector;
import com.anonymize.detectors.PhoneNumberDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MaskAnonymizer strategy, which replaces PII entities with asterisks or custom mask text.
 */
public class MaskAnonymizerTest {

    private Anonymizer defaultMaskAnonymizer;
    private Anonymizer customMaskAnonymizer;
    
    @BeforeEach
    public void setup() {
        // Create anonymizers with different mask configurations
        defaultMaskAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new MaskAnonymizer()) // Default is asterisks
                .build();
                
        customMaskAnonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector())
                .withAnonymizerStrategy(new MaskAnonymizer("[REDACTED]"))
                .build();
    }
    
    @Test
    public void testDefaultMasking() {
        // Test with a simple text containing email
        String text = "Contact us at support@example.com";
        AnonymizationResult result = defaultMaskAnonymizer.anonymize(text);
        
        // Print results for debugging
        System.out.println("Original: " + text);
        System.out.println("Masked: " + result.getAnonymizedText());
        
        // Verify the email was detected
        assertTrue(result.getDetectedEntities().size() > 0, 
                 "Should detect at least one entity");
        
        // Verify masking (should replace with some form of masking)
        assertFalse(result.getAnonymizedText().contains("support@example.com"),
                  "Original email should be masked");
    }
    
    @Test
    public void testCustomMaskText() {
        // Test with custom mask text
        String text = "Please email me at test@example.com";
        AnonymizationResult result = customMaskAnonymizer.anonymize(text);
        
        // Print results for debugging
        System.out.println("Original: " + text);
        System.out.println("Custom masked: " + result.getAnonymizedText());
        
        // Verify the email was detected
        assertTrue(result.getDetectedEntities().size() > 0,
                 "Should detect at least one entity");
                 
        // Verify custom masking
        assertFalse(result.getAnonymizedText().contains("test@example.com"),
                  "Original email should be masked");
        assertTrue(result.getAnonymizedText().contains("[REDACTED]"),
                 "Email should be replaced with custom mask text");
        
        // Expected result with custom mask
        String expected = "Please email me at [REDACTED]";
        assertEquals(expected, result.getAnonymizedText(),
                   "Text should be properly masked with custom text");
    }
    
    @Test
    public void testMaskingMultipleEntities() {
        // Test with multiple PII entities
        String text = "Contact info: john@example.com or (555) 123-4567";
        AnonymizationResult result = defaultMaskAnonymizer.anonymize(text);
        
        // Print results for debugging
        System.out.println("Original: " + text);
        System.out.println("Masked: " + result.getAnonymizedText());
        
        // Verify entities are detected
        assertTrue(result.getDetectedEntities().size() > 0,
                 "Should detect at least one entity");
        
        // Check that original values are masked
        assertFalse(result.getAnonymizedText().contains("john@example.com"),
                  "Email should be masked");
                  
        // Original entities should be preserved for reference
        boolean hasEmailEntity = false;
        for (PIIEntity entity : result.getDetectedEntities()) {
            if (entity.getType().equals(PIIType.EMAIL)) {
                hasEmailEntity = true;
                assertEquals("john@example.com", entity.getText(),
                           "Original email text should be preserved in entity");
            }
        }
        
        assertTrue(hasEmailEntity, "Should have preserved email entity information");
    }
    
    @Test
    public void testMaskingPreservesContext() {
        // Test that masking preserves surrounding context
        String text = "Please contact user@example.com for help.";
        AnonymizationResult result = defaultMaskAnonymizer.anonymize(text);
        
        // Check that context is preserved
        assertTrue(result.getAnonymizedText().startsWith("Please contact "),
                 "Text before the PII should be preserved");
        assertTrue(result.getAnonymizedText().endsWith(" for help."),
                 "Text after the PII should be preserved");
    }
}