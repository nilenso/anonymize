package com.anonymize.detectors;

import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.strategies.MaskAnonymizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for email detection functionality.
 */
public class EmailDetectionTest {

    private Anonymizer anonymizer;
    
    @BeforeEach
    public void setup() {
        // Create an anonymizer with email detector
        anonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withAnonymizerStrategy(new MaskAnonymizer())
                .build();
    }
    
    @Test
    public void testSimpleEmailDetection() {
        // Test with standard email format
        String text = "Please contact me at user@example.com";
        AnonymizationResult result = anonymizer.anonymize(text);
        
        // Verify detection count
        assertEquals(1, result.getDetectedEntities().size(), "Should detect exactly one email");
        
        // Verify the detected entity is the correct email
        PIIEntity entity = result.getDetectedEntities().get(0);
        assertEquals("user@example.com", entity.getText(), "Should extract the exact email text");
        assertEquals(PIIType.EMAIL, entity.getType(), "Should identify as EMAIL type");
        assertEquals(text.indexOf("user@example.com"), entity.getStartPosition(), "Should have correct start position");
        assertEquals(text.indexOf("user@example.com") + "user@example.com".length(), 
                    entity.getEndPosition(), "Should have correct end position");
        
        // Verify masking (masking should replace the email with asterisks of same length)
        assertFalse(result.getAnonymizedText().contains("user@example.com"), "Email should be masked");
        
        // Verify the overall text structure is preserved (text before/after email)
        assertTrue(result.getAnonymizedText().startsWith("Please contact me at "), 
                 "Text before email should be preserved");
    }
    
    @Test
    public void testMultipleEmails() {
        // Test with multiple email addresses
        String text = "Contact info: primary@example.com, secondary@example.org, third@test.net";
        AnonymizationResult result = anonymizer.anonymize(text);
        
        // Verify all emails were detected
        assertEquals(3, result.getDetectedEntities().size(), "Should detect all three emails");
        
        // Verify each email entity has the correct type
        for (PIIEntity entity : result.getDetectedEntities()) {
            assertEquals(PIIType.EMAIL, entity.getType(), "Entity should be EMAIL type");
            assertTrue(entity.getText().contains("@"), "Detected entity should contain @ symbol");
        }
        
        // Verify all email domains were masked
        assertFalse(result.getAnonymizedText().contains("@example.com"), "First email should be masked");
        assertFalse(result.getAnonymizedText().contains("@example.org"), "Second email should be masked");
        assertFalse(result.getAnonymizedText().contains("@test.net"), "Third email should be masked");
        
        // Verify entity positions don't overlap
        for (int i = 0; i < result.getDetectedEntities().size() - 1; i++) {
            PIIEntity current = result.getDetectedEntities().get(i);
            PIIEntity next = result.getDetectedEntities().get(i + 1);
            assertTrue(current.getEndPosition() <= next.getStartPosition(), 
                     "Entity positions should not overlap");
        }
    }
    
    @Test
    public void testComplexEmail() {
        // Test with a more complex email format - including pluses and subdomains
        String text = "My work email is john.doe+label@company-name.co.uk";
        AnonymizationResult result = anonymizer.anonymize(text);
        
        // Verify complex email was detected
        assertEquals(1, result.getDetectedEntities().size(), "Should detect the complex email");
        
        // Verify entity details
        PIIEntity entity = result.getDetectedEntities().get(0);
        assertEquals("john.doe+label@company-name.co.uk", entity.getText(), "Should extract exact email");
        assertEquals(PIIType.EMAIL, entity.getType(), "Should be identified as EMAIL type");
        
        // Verify proper confidence level (should be high for email detection)
        assertTrue(entity.getConfidence() > 0.8, "Email detection should have high confidence");
        
        // Verify complex email components were masked
        assertFalse(result.getAnonymizedText().contains("john.doe+label"), "Username should be masked");
        assertFalse(result.getAnonymizedText().contains("company-name"), "Domain should be masked");
        assertFalse(result.getAnonymizedText().contains(".co.uk"), "TLD should be masked");
        
        // Verify text before email is preserved
        assertTrue(result.getAnonymizedText().startsWith("My work email is "), 
                 "Text before email should be preserved");
    }
}