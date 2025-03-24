package com.anonymize.examples;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.opennlp.LocationDetector;
import com.anonymize.detectors.opennlp.ModelManager;
import com.anonymize.detectors.opennlp.PersonNameDetector;
import com.anonymize.strategies.MaskAnonymizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Example demonstrating the use of NER-based detection for identifying person names and locations.
 */
public class NERDetectionExample {

    public static void main(String[] args) {
        System.out.println("Anonymize Library - NER Detection Example");
        System.out.println("--------------------------------------");
        
        try {
            // Make sure models directory exists
            File modelsDir = new File("models/opennlp");
            if (!modelsDir.exists()) {
                modelsDir.mkdirs();
            }
            
            // Initialize the model manager (downloads models if needed)
            System.out.println("Initializing model manager...");
            ModelManager modelManager = new ModelManager();
            
            // Explicitly download models before creating detectors
            System.out.println("Downloading NER models (if needed)...");
            modelManager.ensureModelAvailable("en-ner-person.bin");
            modelManager.ensureModelAvailable("en-ner-location.bin");
            System.out.println("Models are ready.");
            
            // Create detectors with custom confidence thresholds
            System.out.println("Creating NER detectors...");
            PersonNameDetector personDetector = new PersonNameDetector(Locale.US, 0.7, modelManager);
            LocationDetector locationDetector = new LocationDetector(Locale.US, 0.65, modelManager);
            
            // Sample text with person names and locations
            String text = "John Smith and Alice Johnson met with Dr. Sezal in Jaipur to discuss " +
                         "the project. They also invited Sarah Brown and Robert Davis from the Chicago office " +
                         "to join them. Later, they all traveled to San Francisco for the conference.";
            
            System.out.println("\nOriginal text:");
            System.out.println(text);
        
        // Performance testing
        System.out.println("\n===== Performance Testing =====");
        
        // Person detection
        long startTime = System.nanoTime();
        List<PIIEntity> personEntities = personDetector.detect(text);
        long personDetectionTime = (System.nanoTime() - startTime) / 1_000_000;
        
        // Location detection
        startTime = System.nanoTime();
        List<PIIEntity> locationEntities = locationDetector.detect(text);
        long locationDetectionTime = (System.nanoTime() - startTime) / 1_000_000;
        
        System.out.println("Person detection time: " + personDetectionTime + "ms");
        System.out.println("Location detection time: " + locationDetectionTime + "ms");
        
        // Display detected entities
        System.out.println("\n===== Detected Entities =====");
        
        System.out.println("\nPerson names (" + personEntities.size() + "):");
        for (PIIEntity entity : personEntities) {
            System.out.println("  - " + entity.getText() + 
                               " (confidence: " + String.format("%.2f", entity.getConfidence()) + ")");
        }
        
        System.out.println("\nLocations (" + locationEntities.size() + "):");
        for (PIIEntity entity : locationEntities) {
            System.out.println("  - " + entity.getText() + 
                               " (confidence: " + String.format("%.2f", entity.getConfidence()) + ")");
        }
        
        // Use the detectors with the anonymizer
        System.out.println("\n===== Anonymization =====");
        
        Anonymizer anonymizer = Anonymizer.builder()
                .withDetector(personDetector)
                .withDetector(locationDetector)
                .withAnonymizerStrategy(new MaskAnonymizer())
                .build();
        
        // Anonymize the text with timing
        System.out.println("\nAnonymized text:");
        startTime = System.nanoTime();
        String anonymizedText = anonymizer.anonymize(text).getAnonymizedText();
        long anonymizationTime = (System.nanoTime() - startTime) / 1_000_000;
        System.out.println(anonymizedText);
        
        // Display performance metrics
        System.out.println("\nTotal anonymization time: " + anonymizationTime + "ms");
        System.out.println("Processing rate: " + (text.length() * 1000 / anonymizationTime) + " characters/second");
        } catch (Exception e) {
            System.err.println("Error running NER example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}