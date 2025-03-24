package com.anonymize.detectors.opennlp;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * NER-based detector for person names using OpenNLP models.
 */
public class PersonNameDetector extends OpenNLPNERDetector {
    private static final Logger logger = LoggerFactory.getLogger(PersonNameDetector.class);
    
    // Map of locale-specific model file names
    private static final Map<String, String> MODEL_FILES = new HashMap<>();
    
    // Initialize with supported models and their file names
    static {
        MODEL_FILES.put(Locale.GENERIC.getCode(), "en-ner-person.bin");
        MODEL_FILES.put(Locale.US.getCode(), "en-ner-person.bin");
        MODEL_FILES.put(Locale.UK.getCode(), "en-ner-person.bin");
        // Add other locales as needed with their specific models
    }
    
    // Default confidence threshold for person name detection
    private static final double DEFAULT_PERSON_CONFIDENCE = 0.8;
    
    // Model manager instance
    private final ModelManager modelManager;
    
    /**
     * Creates a PersonNameDetector with a specific locale, confidence threshold, and model manager.
     * 
     * @param locale The locale to use for detection
     * @param confidenceThreshold The confidence threshold for detections
     * @param modelManager The model manager to use
     */
    public PersonNameDetector(Locale locale, double confidenceThreshold, ModelManager modelManager) {
        super(PIIType.PERSON_NAME.getValue(), locale, getStaticSupportedLocales(), confidenceThreshold);
        this.modelManager = modelManager; 
        // Force model load with the assigned model manager
        loadModel();
    }
    
    /**
     * Creates a PersonNameDetector with a specific locale and confidence threshold.
     * Uses a new default model manager.
     * 
     * @param locale The locale to use for detection
     * @param confidenceThreshold The confidence threshold for detections
     */
    public PersonNameDetector(Locale locale, double confidenceThreshold) {
        this(locale, confidenceThreshold, new ModelManager());
    }
    
    /**
     * Creates a PersonNameDetector with a specific locale.
     * Uses the default confidence threshold and a new model manager.
     * 
     * @param locale The locale to use for detection
     */
    public PersonNameDetector(Locale locale) {
        this(locale, DEFAULT_PERSON_CONFIDENCE);
    }
    
    /**
     * Creates a PersonNameDetector with the GENERIC locale.
     * Uses the default confidence threshold and a new model manager.
     */
    public PersonNameDetector() {
        this(Locale.GENERIC);
    }
    
    @Override
    protected String getModelPath() {
        String localeCode = getLocale().getCode();
        
        // If no model exists for the specific locale, fall back to GENERIC
        if (!MODEL_FILES.containsKey(localeCode)) {
            localeCode = Locale.GENERIC.getCode();
        }
        
        String modelFileName = MODEL_FILES.get(localeCode);
        
        if (modelManager == null) {
            logger.error("Model manager is null");
            return "models/opennlp/" + modelFileName; // Fallback path
        }
        
        try {
            // Ensure the model is available locally
            return modelManager.ensureModelAvailable(modelFileName);
        } catch (IOException e) {
            logger.error("Error ensuring model availability: {}", e.getMessage(), e);
            // Return a default path as fallback
            return "models/opennlp/" + modelFileName;
        }
    }
    
    /**
     * Gets the set of locales supported by this detector.
     * 
     * @return Set of supported locales
     */
    private static Set<Locale> getStaticSupportedLocales() {
        Set<Locale> locales = new HashSet<>();
        locales.add(Locale.GENERIC);
        locales.add(Locale.US);
        locales.add(Locale.UK);
        // Add other supported locales
        return locales;
    }
}