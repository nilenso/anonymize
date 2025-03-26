package com.anonymize.detectors.djl;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.translator.NamedEntity;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
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
    protected ZooModel<String, NamedEntity[]> model;
    protected Predictor<String, NamedEntity[]> predictor;
    
    // Flag to indicate if the model is loaded
    protected boolean modelLoaded = false;
    
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
        } catch (ModelException | TranslateException | IOException e) {
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
    
 
    public abstract List<PIIEntity> detect(String text);
    // @Override
    // public List<PIIEntity> detect(String text) {
    //     if (text == null || text.isEmpty()) {
    //         return Collections.emptyList();
    //     }
        
    //     // Ensure model is loaded
    //     if (!loadModelIfNeeded()) {
    //         logger.warn("Model not loaded, skipping detection");
    //         return Collections.emptyList();
    //     }
        
    //     try {
    //         NamedEntity[] results = predictor.predict(text);
    //         return processNamedEntities(results);
    //     } catch (Exception e) {
    //         logger.error("Error performing DJL detection: {}", e.getMessage());
    //         return Collections.emptyList();
    //     }
    // }
    
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
