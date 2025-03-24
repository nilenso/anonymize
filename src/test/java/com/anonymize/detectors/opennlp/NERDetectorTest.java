package com.anonymize.detectors.opennlp;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.strategies.MaskAnonymizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OpenNLP-based Named Entity Recognition (NER) detectors.
 * These tests are conditional and will be skipped if models aren't available.
 */
@EnabledIfSystemProperty(named = "test.opennlp", matches = "true")
public class NERDetectorTest {

    private static ModelManager modelManager;
    private static boolean modelsAvailable = false;
    
    @BeforeAll
    public static void setupModels() {
        try {
            // Check if models directory exists
            File modelsDir = new File("models/opennlp");
            modelsAvailable = modelsDir.exists();
            
            if (modelsAvailable) {
                // Initialize model manager
                modelManager = new ModelManager();
                
                // Check for specific model files - use file existence as proxy
                File personModel = new File("models/opennlp/en-ner-person.bin");
                File locationModel = new File("models/opennlp/en-ner-location.bin");
                modelsAvailable = personModel.exists() && locationModel.exists();
            }
            
            if (!modelsAvailable) {
                System.out.println("OpenNLP models not available - NER tests will be skipped");
            }
        } catch (Exception e) {
            System.err.println("Error checking OpenNLP models: " + e.getMessage());
            modelsAvailable = false;
        }
    }
    
    @Test
    public void testPersonNameDetection() {
        // Skip if models aren't available
        if (!modelsAvailable) {
            System.out.println("Skipping test: testPersonNameDetection - models not available");
            return;
        }
        
        try {
            // Create person name detector
            PersonNameDetector personDetector = new PersonNameDetector(Locale.US, 0.5, modelManager);
            
            // Test text with person names
            String text = "John Smith and Alice Johnson are attending the meeting.";
            
            // Detect person names
            List<PIIEntity> entities = personDetector.detect(text);
            
            // Print results for debugging
            System.out.println("Person names detected: " + entities.size());
            for (PIIEntity entity : entities) {
                System.out.println("  - " + entity.getText() + " (confidence: " + entity.getConfidence() + ")");
            }
            
            // Verify at least one name was detected
            assertTrue(entities.size() > 0, "Should detect at least one person name");
            
            // Verify detected entities are of correct type
            for (PIIEntity entity : entities) {
                assertEquals(PIIType.PERSON_NAME.getValue(), entity.getType(),
                          "Entity should be PERSON_NAME type");
                assertTrue(entity.getConfidence() > 0, 
                         "Confidence should be greater than 0");
            }
        } catch (Exception e) {
            fail("Exception in person name detection test: " + e.getMessage());
        }
    }
    
    @Test
    public void testLocationDetection() {
        // Skip if models aren't available
        if (!modelsAvailable) {
            System.out.println("Skipping test: testLocationDetection - models not available");
            return;
        }
        
        try {
            // Create location detector
            LocationDetector locationDetector = new LocationDetector(Locale.US, 0.5, modelManager);
            
            // Test text with locations
            String text = "We're opening new offices in New York and San Francisco next month.";
            
            // Detect locations
            List<PIIEntity> entities = locationDetector.detect(text);
            
            // Print results for debugging
            System.out.println("Locations detected: " + entities.size());
            for (PIIEntity entity : entities) {
                System.out.println("  - " + entity.getText() + " (confidence: " + entity.getConfidence() + ")");
            }
            
            // Verify at least one location was detected
            assertTrue(entities.size() > 0, "Should detect at least one location");
            
            // Verify detected entities are of correct type
            for (PIIEntity entity : entities) {
                assertEquals(PIIType.LOCATION.getValue(), entity.getType(),
                          "Entity should be LOCATION type");
                assertTrue(entity.getConfidence() > 0, 
                         "Confidence should be greater than 0");
            }
        } catch (Exception e) {
            fail("Exception in location detection test: " + e.getMessage());
        }
    }
    
    @Test
    public void testNERWithAnonymizer() {
        // Skip if models aren't available
        if (!modelsAvailable) {
            System.out.println("Skipping test: testNERWithAnonymizer - models not available");
            return;
        }
        
        try {
            // Create detectors
            PersonNameDetector personDetector = new PersonNameDetector(Locale.US, 0.5, modelManager);
            LocationDetector locationDetector = new LocationDetector(Locale.US, 0.5, modelManager);
            
            // Create anonymizer with NER detectors
            Anonymizer anonymizer = new Anonymizer.Builder()
                    .withDetector(personDetector)
                    .withDetector(locationDetector)
                    .withAnonymizerStrategy(new MaskAnonymizer())
                    .build();
                    
            // Test text with person names and locations
            String text = "Jane Doe is traveling to Chicago next week.";
            
            // Anonymize the text
            AnonymizationResult result = anonymizer.anonymize(text);
            
            // Print results for debugging
            System.out.println("Original: " + text);
            System.out.println("Anonymized: " + result.getAnonymizedText());
            System.out.println("Entities detected: " + result.getDetectedEntities().size());
            
            // Verify detection worked
            assertTrue(result.getDetectedEntities().size() > 0,
                     "Should detect at least one entity");
                     
            // Try to verify the original entities were masked
            for (PIIEntity entity : result.getDetectedEntities()) {
                assertFalse(result.getAnonymizedText().contains(entity.getText()),
                          "Entity '" + entity.getText() + "' should be masked in output");
            }
        } catch (Exception e) {
            fail("Exception in NER anonymizer test: " + e.getMessage());
        }
    }
}