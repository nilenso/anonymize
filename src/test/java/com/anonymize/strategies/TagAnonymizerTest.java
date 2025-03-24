package com.anonymize.strategies;

import com.anonymize.common.Locale;
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
 * Test class for the TagAnonymizer strategy, which replaces PII entities with tags.
 */
public class TagAnonymizerTest {

    private Anonymizer anonymizer;
    
    @BeforeEach
    public void setup() {
        // Create an anonymizer with TagAnonymizer strategy
        anonymizer = new Anonymizer.Builder()
                .withDetector(new EmailDetector())
                .withDetector(new PhoneNumberDetector(Locale.US))
                .withAnonymizerStrategy(new TagAnonymizer())
                .build();
    }
    
    @Test
    public void testSimpleTagReplacement() {
        // Test with simple email text
        String text = "Please contact me at john.doe@example.com";
        AnonymizationResult result = anonymizer.anonymize(text);
        
        // Print results for debugging
        System.out.println("Original: " + text);
        System.out.println("Tagged: " + result.getAnonymizedText());
        System.out.println("Entities detected: " + result.getDetectedEntities().size());
        
        // Verify at least one entity is detected
        assertTrue(result.getDetectedEntities().size() > 0, 
                 "Should detect at least one entity");
        
        // Verify the entity is of correct type
        if (!result.getDetectedEntities().isEmpty()) {
            PIIEntity entity = result.getDetectedEntities().get(0);
            assertEquals(PIIType.EMAIL.getValue(), entity.getType(), 
                       "Entity should be an EMAIL");
            assertEquals("john.doe@example.com", entity.getText(), 
                       "Entity should contain the exact email");
        }
        
        // Verify original email is not present in output
        assertFalse(result.getAnonymizedText().contains("john.doe@example.com"), 
                  "Original email should be replaced");
    }
    
    @Test
    public void testMultipleTagReplacements() {
        // Test with multiple emails
        String text = "Email addresses: primary@example.com, secondary@example.com, backup@example.com";
        AnonymizationResult result = anonymizer.anonymize(text);
        
        // Print results for debugging
        System.out.println("Original: " + text);
        System.out.println("Tagged: " + result.getAnonymizedText());
        System.out.println("Entities detected: " + result.getDetectedEntities().size());
        
        // Verify at least one email was detected and properly typed
        assertTrue(result.getDetectedEntities().size() > 0, 
                 "Should detect at least one email");
        
        // Verify entities are correctly typed
        for (PIIEntity entity : result.getDetectedEntities()) {
            assertEquals(PIIType.EMAIL.getValue(), entity.getType(),
                       "All entities should be of type EMAIL");
            assertTrue(entity.getText().contains("@"),
                     "All entities should contain @ symbol");
        }
        
        // Verify no original emails remain in output
        assertFalse(result.getAnonymizedText().contains("@example.com"),
                  "No original email domains should be present");
    }
    
    @Test
    public void testMixedEntityTagging() {
        // Test with email and phone number 
        String text = "Contact: email1@example.com or 555-123-4567";
        AnonymizationResult result = anonymizer.anonymize(text);
        
        // Print results for debugging
        System.out.println("Original: " + text);
        System.out.println("Tagged: " + result.getAnonymizedText());
        System.out.println("Entities detected: " + result.getDetectedEntities().size());
        
        // Verify at least one entity was detected
        assertTrue(result.getDetectedEntities().size() > 0, 
                 "Should detect at least one entity");
        
        // Verify the original sensitive information is not present
        assertFalse(result.getAnonymizedText().contains("email1@example.com"),
                  "Original email should not be present");
        
        // Verify entity types are preserved
        boolean hasEmailEntity = false;
        
        for (PIIEntity entity : result.getDetectedEntities()) {
            if (entity.getType().equals(PIIType.EMAIL.getValue())) {
                hasEmailEntity = true;
                assertEquals("email1@example.com", entity.getText(),
                           "Email entity should preserve original text");
            }
        }
        
        assertTrue(hasEmailEntity, "Should have detected at least one EMAIL entity");
    }
    
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}