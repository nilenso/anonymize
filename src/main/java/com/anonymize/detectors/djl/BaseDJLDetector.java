package com.anonymize.detectors.djl;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Translator;
import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;
import com.anonymize.detectors.AbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Base class for all DJL-based detectors, providing common functionality for
 * loading models and processing text.
 */
public abstract class BaseDJLDetector extends AbstractDetector {
    private static final Logger logger = LoggerFactory.getLogger(BaseDJLDetector.class);
    
    // Default confidence threshold
    protected static final double DEFAULT_CONFIDENCE = 0.7;
    
    // The DJL model manager for loading and caching models
    protected final DJLModelManager modelManager;
    
    // The model ID to load
    protected final String modelId;
    
    // The confidence threshold for detections
    protected final double confidenceThreshold;
    
    // The loaded model and predictor
    @SuppressWarnings("rawtypes")
    protected ZooModel model;
    protected Predictor predictor;
    
    // Flag to indicate if the model is loaded
    protected boolean modelLoaded = false;
    
    /**
     * Internal class to represent entity detection results.
     */
    protected static class EntityResult {
        private String entity;
        private String type;
        private int startPosition;
        private int endPosition;
        private double confidence;
        
        public String getEntity() { return entity; }
        public void setEntity(String entity) { this.entity = entity; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public int getStartPosition() { return startPosition; }
        public void setStartPosition(int startPosition) { this.startPosition = startPosition; }
        
        public int getEndPosition() { return endPosition; }
        public void setEndPosition(int endPosition) { this.endPosition = endPosition; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
    
    /**
     * Creates a new BaseDJLDetector with the specified type, locale, and model ID.
     * 
     * @param type The PII type this detector handles
     * @param locale The locale this detector is configured for
     * @param supportedLocales Set of locales supported by this detector
     * @param modelId The model ID to load
     * @param confidenceThreshold The confidence threshold for detections
     * @param modelManager The model manager to use for loading models
     */
    protected BaseDJLDetector(String type, Locale locale, Set<Locale> supportedLocales,
                             String modelId, double confidenceThreshold,
                             DJLModelManager modelManager) {
        super(type, locale, supportedLocales);
        this.modelId = modelId;
        this.confidenceThreshold = confidenceThreshold;
        this.modelManager = modelManager;
    }
    
    /**
     * Creates a new BaseDJLDetector with the specified type, locale, and model ID using default
     * confidence threshold.
     * 
     * @param type The PII type this detector handles
     * @param locale The locale this detector is configured for
     * @param supportedLocales Set of locales supported by this detector
     * @param modelId The model ID to load
     * @param modelManager The model manager to use for loading models
     */
    protected BaseDJLDetector(String type, Locale locale, Set<Locale> supportedLocales,
                             String modelId, DJLModelManager modelManager) {
        this(type, locale, supportedLocales, modelId, DEFAULT_CONFIDENCE, modelManager);
    }
    
    /**
     * Load the DJL model if not already loaded.
     * 
     * @return true if the model was loaded successfully, false otherwise
     */
    protected boolean loadModelIfNeeded() {
        if (modelLoaded) {
            return true;
        }
        
        try {
            model = modelManager.loadModel(modelId);
            predictor = initializePredictor();
            modelLoaded = true;
            return true;
        } catch (ModelNotFoundException | MalformedModelException | IOException e) {
            logger.error("Failed to load model {}: {}", modelId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Package-private visibility for testing access.
     * 
     * @return true if the model is loaded, false otherwise
     */
    boolean isModelLoaded() {
        return modelLoaded;
    }
    
    /**
     * Initialize the model predictor. This method must be implemented by subclasses
     * to create the appropriate predictor for the specific model type.
     * 
     * @return The initialized predictor
     * @throws IOException If an I/O error occurs during predictor creation
     */
    @SuppressWarnings("rawtypes")
    protected abstract Predictor initializePredictor() throws IOException;
    
    /**
     * Maps the model's entity type to the PIIType used by the anonymization library.
     * 
     * @param modelEntityType The entity type from the model
     * @return The corresponding PIIType value
     */
    protected abstract String mapEntityType(String modelEntityType);
    
    @Override
    public List<PIIEntity> detect(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Ensure model is loaded
        if (!loadModelIfNeeded()) {
            logger.warn("Model not loaded, skipping detection");
            return Collections.emptyList();
        }
        
        try {
            // Perform prediction - create dummy results for now
            List<EntityResult> results = new ArrayList<>();
            
            // // For testing, add some dummy entity results if the text contains common names
            // if (text.contains("John") || text.contains("Jane") || text.contains("Smith")) {
            //     EntityResult result = new EntityResult();
            //     result.setEntity("John Smith");
            //     result.setType("PER");
            //     result.setStartPosition(text.indexOf("John"));
            //     result.setEndPosition(text.indexOf("John") + 10); // Approximate
            //     result.setConfidence(0.95);
            //     results.add(result);
            // }
            
            // Convert results to PIIEntity objects
            List<PIIEntity> entities = new ArrayList<>();
            for (EntityResult result : results) {
                // Skip entities below confidence threshold
                if (result.getConfidence() < confidenceThreshold) {
                    continue;
                }
                
                // Map the model's entity type to PIIType
                String piiType = mapEntityType(result.getType());
                if (piiType == null) {
                    // Skip unsupported entity types
                    continue;
                }
                
                // Create the PIIEntity
                PIIEntity entity = createEntity(
                        result.getStartPosition(),
                        result.getEndPosition(),
                        result.getEntity(),
                        result.getConfidence()
                );
                entities.add(entity);
            }
            
            return entities;
        } catch (Exception e) {
            logger.error("Error performing DJL detection: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Called when this detector is no longer needed.
     * Releases resources associated with the model.
     */
    public void close() {
        if (predictor != null) {
            predictor.close();
        }
        modelLoaded = false;
    }
}