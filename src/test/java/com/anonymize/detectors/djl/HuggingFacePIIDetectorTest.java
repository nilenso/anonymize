package com.anonymize.detectors.djl;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;

import ai.djl.modality.nlp.translator.NamedEntity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for the HuggingFacePIIDetector class.
 * These tests are enabled only when the models directory exists.
 */
public class HuggingFacePIIDetectorTest {

    private static boolean modelsAvailable = false;
    private static DJLModelManager modelManager;
    
    @BeforeAll
    public static void checkModels() {
        System.out.println("\n=== HUGGINGFACE PII DETECTOR TEST SETUP ===");
        
        // Check if models directory exists
        File modelsDir = new File("models/djl");
        
        // Create models directory if it doesn't exist
        if (!modelsDir.exists()) {
            modelsDir.mkdirs();
            System.out.println("Created models directory at: " + modelsDir.getAbsolutePath());
        } else {
            System.out.println("Models directory exists at: " + modelsDir.getAbsolutePath());
        }
        
        // Try to initialize the model manager and download required models
        try {
            System.out.println("Initializing DJL model manager...");
            modelManager = new DJLModelManager();
            
            // Attempt to download the NER model needed for tests
            String modelId = "ner-bert-base";
            System.out.println("Checking/downloading model: " + modelId);
            boolean modelDownloaded = modelManager.ensureModelDownloaded(modelId);
            
            if (modelDownloaded) {
                System.out.println("✓ Model successfully downloaded/available: " + modelId);
                modelsAvailable = true;
            } else {
                System.out.println("✗ Failed to download model: " + modelId);
                modelsAvailable = false;
            }
            
            System.out.println("DJL test environment setup complete");
            System.out.println("Java version: " + System.getProperty("java.version"));
            System.out.println("Test configuration:");
            System.out.println("- Models available: " + modelsAvailable);
            System.out.println("- Tests requiring models will " + (modelsAvailable ? "run" : "be skipped"));
            System.out.println("=========================================\n");
        } catch (Exception e) {
            System.out.println("DJL setup failed - tests will be skipped: " + e.getMessage());
            e.printStackTrace(System.out);
            modelsAvailable = false;
        }
    }
    
    /**
     * Helper method to check if models are available.
     * Used by the @EnabledIf annotation to conditionally run tests.
     */
    static boolean areModelsAvailable() {
        return modelsAvailable;
    }
    
    @Test
    @EnabledIf("areModelsAvailable")
    public void testPersonDetection() {
        // Create detector with the shared model manager
        HuggingFacePIIDetector detector = new HuggingFacePIIDetector(modelManager);
        
        try {
            // Test text with person names
            String text = "John Smith and Alice Johnson met with Michael Brown in New York City.";
            System.out.println("\nRunning person detection test with text: '" + text + "'");
            
            // Run detection
            List<PIIEntity> entities = detector.detect(text);
            
            // Print results for debugging
            // System.out.println("Person Detection Results:");
            // if (entities.isEmpty()) {
            //     System.out.println("  No entities detected");
            // } else {
            //     for (PIIEntity entity : entities) {
            //         System.out.println("  - " + entity.getText() + " (" + entity.getType() + ", " + entity.getConfidence() + ")");
            //     }
            // }
            
            // Check if any person entities were detected
            boolean foundPerson = entities.stream()
                    .anyMatch(e -> e.getType() == PIIType.PERSON_NAME);
            
            assertTrue(foundPerson, "Should detect at least one person name");
        } finally {
            // Clean up resources
            detector.close();
        }
    }
    
    @Test
    public void testEntityTypeMapping() {
        // Create detector with any model manager - it won't be used for this test
        DJLModelManager mockManager = Mockito.mock(DJLModelManager.class);
        HuggingFacePIIDetector detector = new HuggingFacePIIDetector(mockManager);
        
        try {
            // Test the entity type mapping
            assertEquals(PIIType.PERSON_NAME, detector.mapEntityType("B-PER"), "PER should map to PERSON");
            assertEquals(PIIType.ORGANIZATION, detector.mapEntityType("I-ORG"), "ORG should map to ORGANIZATION");
            assertEquals(PIIType.LOCATION, detector.mapEntityType("B-LOC"), "LOC should map to LOCATION");
            assertEquals(PIIType.MISC, detector.mapEntityType("I-MISC"), "MISC should remain MISC");
            assertNull(detector.mapEntityType("UNKNOWN"), "Unknown type should map to null");
        } finally {
            detector.close();
        }
    }
    
    @Test
    @EnabledIf("areModelsAvailable")
    public void testLocationDetection() {
        // Create detector with the shared model manager
        HuggingFacePIIDetector detector = new HuggingFacePIIDetector(modelManager);
        
        try {
            // Test text with locations
            String text = "The meeting will take place in San Francisco, California, not too far from Seattle.";
            System.out.println("\nRunning location detection test with text: '" + text + "'");
            
            // Run detection
            List<PIIEntity> entities = detector.detect(text);
            
            // Print results for debugging
            System.out.println("Location Detection Results:");
            if (entities.isEmpty()) {
                System.out.println("  No entities detected");
            } else {
                for (PIIEntity entity : entities) {
                    System.out.println("  - " + entity.getText() + " (" + entity.getType() + ", " + entity.getConfidence() + ")");
                }
            }
            
            // Check if any location entities were detected
            boolean foundLocation = entities.stream()
                    .anyMatch(e -> e.getType().equals(PIIType.LOCATION));
            
            assertTrue(foundLocation, "Should detect at least one location");
        } finally {
            // Clean up resources
            detector.close();
        }
    }
    
    @Test
    public void testConstructorAndParameters() {
        // Create mock model manager
        DJLModelManager mockManager = Mockito.mock(DJLModelManager.class);
        
        // Create detector with the mock manager
        HuggingFacePIIDetector detector = new HuggingFacePIIDetector(mockManager);
        
        try {
            // Verify the detector was initialized with the expected parameters
            assertEquals("PII", detector.getType(), "Detector type should be PII");
            assertEquals(Locale.GENERIC, detector.getLocale(), "Detector locale should be GENERIC");
            assertTrue(detector.getSupportedLocales().contains(Locale.GENERIC), 
                    "Supported locales should include GENERIC");
            assertFalse(detector.isModelLoaded(), "Model should not be loaded initially");
        } finally {
            detector.close();
        }
    }
    
    @Test
    @EnabledIf("areModelsAvailable")
    public void testMixedEntityDetection() {
        // Create detector with the shared model manager
        HuggingFacePIIDetector detector = new HuggingFacePIIDetector(modelManager);
        
        try {
            // Test text with mixed entity types
            String text = "Sarah Johnson is traveling to London next month for a conference at Microsoft headquarters.";
            System.out.println("\nRunning mixed entity detection test with text: '" + text + "'");
            
            // Run detection
            List<PIIEntity> entities = detector.detect(text);
            
            // Print results for debugging
            System.out.println("Mixed Entity Detection Results:");
            if (entities.isEmpty()) {
                System.out.println("  No entities detected");
            } else {
                for (PIIEntity entity : entities) {
                    System.out.println("  - " + entity.getText() + " (" + entity.getType() + ", " + entity.getConfidence() + ")");
                }
            }
            
            // Check that we have at least one entity
            assertFalse(entities.isEmpty(), "Should detect at least one entity");
            
            // Verify entity types are mapped correctly
            for (PIIEntity entity : entities) {
                String type = entity.getType().getValue();
                assertTrue(
                    type.equals("PERSON_NAME") ||
                    type.equals("LOCATION") ||
                    type.equals("ORGANIZATION") ||
                    type.equals("MISC"),
                    "Entity type should be mapped correctly: " + type
                );
            }
        } finally {
            // Clean up resources
            detector.close();
        }
    }
    
    // @Disabled("Mock functionality needs to be updated")
    // @Test
    // public void testConfidenceThreshold() {
    //     // Create mock model manager
    //     DJLModelManager mockManager = Mockito.mock(DJLModelManager.class);
        
    //     // Create the detector with the mock manager
    //     HuggingFacePIIDetector detector = Mockito.spy(new HuggingFacePIIDetector(mockManager));
        
    //     try {
    //         // Mock the loadModelIfNeeded method to return true
    //         Mockito.doReturn(true).when(detector).loadModelIfNeeded();
            
    //         // Create entity results with different confidence levels
    //         List<BaseDJLDetector.EntityResult> mockResults = new ArrayList<>();
            
    //         // Entity with confidence above threshold
    //         BaseDJLDetector.EntityResult highConfidence = new BaseDJLDetector.EntityResult();
    //         highConfidence.setEntity("John Smith");
    //         highConfidence.setType("PER");
    //         highConfidence.setStartPosition(0);
    //         highConfidence.setEndPosition(10);
    //         highConfidence.setConfidence(0.95); // Above the default threshold
    //         mockResults.add(highConfidence);
            
    //         // Entity with confidence below threshold
    //         BaseDJLDetector.EntityResult lowConfidence = new BaseDJLDetector.EntityResult();
    //         lowConfidence.setEntity("ABC Corp");
    //         lowConfidence.setType("ORG");
    //         lowConfidence.setStartPosition(20);
    //         lowConfidence.setEndPosition(28);
    //         lowConfidence.setConfidence(0.5); // Below the default threshold
    //         mockResults.add(lowConfidence);
            
    //         // Mock the predictor's predict method to return our mock results
    //         Mockito.when(detector.getPredictorForTesting(anyString())).thenReturn(mockResults);
            
    //         // Run detection
    //         List<PIIEntity> entities = detector.detect("John Smith works at ABC Corp");
            
    //         // Verify that only the high confidence entity was included
    //         assertEquals(1, entities.size(), "Should only detect high confidence entities");
    //         assertEquals("John Smith", entities.get(0).getText(), "Should detect John Smith");
    //         assertEquals("PERSON", entities.get(0).getType(), "Type should be PERSON");
    //         assertEquals(0.95, entities.get(0).getConfidence(), 0.01, "Confidence should be preserved");
    //     } finally {
    //         detector.close();
    //     }
    // }
    
    @Test
    public void testEmptyInput() {
        // Create detector with any model manager - it won't be used
        DJLModelManager mockManager = Mockito.mock(DJLModelManager.class);
        HuggingFacePIIDetector detector = new HuggingFacePIIDetector(mockManager);
        
        try {
            // Test with empty text
            List<PIIEntity> entities = detector.detect("");
            
            // Should return an empty list
            assertTrue(entities.isEmpty(), "Empty input should return empty list");
            
            // Test with null input
            entities = detector.detect(null);
            
            // Should return an empty list
            assertTrue(entities.isEmpty(), "Null input should return empty list");
        } finally {
            // Clean up resources
            detector.close();
        }
    }
    
    @Test
    public void testProcessNamedEntitiesWithMultiTokens() {
        // Create mock named entities with multi-token examples
        // Simulate what would be returned from the DJL model for a sentence like:
        // "John Smith works at Google Inc in New York City"
        
        NamedEntity[] mockEntities = new ai.djl.modality.nlp.translator.NamedEntity[] {
            // Person name - multi-token
            createNamedEntity("B-PER", "John", 0, 4, 0.95f),
            createNamedEntity("I-PER", "Smith", 5, 10, 0.93f),
            
            // Skip "works at" (would be "O" label)
            
            // Organization - multi-token
            createNamedEntity("B-ORG", "Google", 18, 24, 0.90f),
            createNamedEntity("I-ORG", "Inc", 25, 28, 0.88f),
            
            // Skip "in" (would be "O" label)
            
            // Location - multi-token
            createNamedEntity("B-LOC", "New", 32, 35, 0.92f),
            createNamedEntity("I-LOC", "York", 36, 40, 0.91f),
            createNamedEntity("I-LOC", "City", 41, 45, 0.89f)
        };
        
        // Create a simple subclass for testing
        // This avoids needing to instantiate the actual detector with models
        HuggingFacePIIDetector testDetector = new HuggingFacePIIDetector(null) {
            // @Override
            // public List<PIIEntity> detect(String text) {
            //     return Collections.emptyList(); // Not used in this test
            // }
            
            // @Override
            // protected Predictor<String, NamedEntity[]> initializePredictor() {
            //     return null; // Not used in this test
            // }
        };
        
        // Process the named entities
        List<PIIEntity> results = testDetector.processNamedEntities(mockEntities);
        
        // Verify results
        assertEquals(3, results.size(), "Should detect 3 multi-token entities");
        
        // Verify person entity
        PIIEntity person = results.get(0);
        assertEquals("John Smith", person.getText(), "Should combine tokens for person name");
        assertEquals(PIIType.PERSON_NAME, person.getType(), "Should map to PERSON_NAME type");
        assertEquals(0, person.getStartPosition(), "Start position should match first token");
        assertEquals(10, person.getEndPosition(), "End position should match last token");
        
        // Verify organization entity
        PIIEntity org = results.get(1);
        assertEquals("Google Inc", org.getText(), "Should combine tokens for organization");
        assertEquals(PIIType.ORGANIZATION, org.getType(), "Should map to ORGANIZATION type");
        assertEquals(18, org.getStartPosition(), "Start position should match first token");
        assertEquals(28, org.getEndPosition(), "End position should match last token");
        
        // Verify location entity
        PIIEntity location = results.get(2);
        assertEquals("New York City", location.getText(), "Should combine three tokens for location");
        assertEquals(PIIType.LOCATION, location.getType(), "Should map to LOCATION type");
        assertEquals(32, location.getStartPosition(), "Start position should match first token");
        assertEquals(45, location.getEndPosition(), "End position should match last token");
    }
    
    /**
     * Helper method to create a mock NamedEntity for testing
     */
    private ai.djl.modality.nlp.translator.NamedEntity createNamedEntity(
            String entityType, String word, int start, int end, float score) {
        return new ai.djl.modality.nlp.translator.NamedEntity(
                entityType, score, 0, word, start, end);
    }
}