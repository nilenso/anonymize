package com.anonymize.detectors.opennlp;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.detectors.BaseNERDetector;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of NER detection using OpenNLP models.
 * This detector loads and uses OpenNLP models to identify named entities in text.
 */
public abstract class OpenNLPNERDetector extends BaseNERDetector {
    private static final Logger logger = LoggerFactory.getLogger(OpenNLPNERDetector.class);
    
    // Singleton cache of loaded models to improve performance
    private static final Map<String, TokenNameFinderModel> modelCache = new ConcurrentHashMap<>();
    
    // The OpenNLP name finder for the current model
    private NameFinderME nameFinder;
    
    // The tokenizer used to split text into tokens
    private final Tokenizer tokenizer;
    
    // Default confidence threshold
    private static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.85;
    
    // Custom confidence threshold (if specified)
    private final double confidenceThreshold;

    /**
     * Creates a new OpenNLP NER detector with the specified type, locale, and supported locales.
     *
     * @param type The type of PII this detector handles
     * @param locale The locale this detector is configured for
     * @param supportedLocales Set of locales supported by this detector
     * @param confidenceThreshold Confidence threshold for entity detection (0.0-1.0)
     */
    protected OpenNLPNERDetector(String type, Locale locale, Set<Locale> supportedLocales, double confidenceThreshold) {
        super(type, locale, supportedLocales);
        this.confidenceThreshold = confidenceThreshold;
        this.tokenizer = SimpleTokenizer.INSTANCE;
        // Don't load model here - derived classes will call loadModel() after setting up model manager
    }

    /**
     * Creates a new OpenNLP NER detector with the specified type, locale, and supported locales.
     * Uses the default confidence threshold.
     *
     * @param type The type of PII this detector handles
     * @param locale The locale this detector is configured for
     * @param supportedLocales Set of locales supported by this detector
     */
    protected OpenNLPNERDetector(String type, Locale locale, Set<Locale> supportedLocales) {
        this(type, locale, supportedLocales, DEFAULT_CONFIDENCE_THRESHOLD);
    }

    /**
     * Creates a new OpenNLP NER detector with the specified type and locale.
     *
     * @param type The type of PII this detector handles
     * @param locale The locale this detector is configured for
     * @param confidenceThreshold Confidence threshold for entity detection (0.0-1.0)
     */
    protected OpenNLPNERDetector(String type, Locale locale, double confidenceThreshold) {
        super(type, locale);
        this.confidenceThreshold = confidenceThreshold;
        this.tokenizer = SimpleTokenizer.INSTANCE;
        loadModel();
    }

    /**
     * Creates a new OpenNLP NER detector with the specified type and locale.
     * Uses the default confidence threshold.
     *
     * @param type The type of PII this detector handles
     * @param locale The locale this detector is configured for
     */
    protected OpenNLPNERDetector(String type, Locale locale) {
        this(type, locale, DEFAULT_CONFIDENCE_THRESHOLD);
    }

    /**
     * Creates a new OpenNLP NER detector with the specified type and the GENERIC locale.
     *
     * @param type The type of PII this detector handles
     * @param confidenceThreshold Confidence threshold for entity detection (0.0-1.0)
     */
    protected OpenNLPNERDetector(String type, double confidenceThreshold) {
        super(type);
        this.confidenceThreshold = confidenceThreshold;
        this.tokenizer = SimpleTokenizer.INSTANCE;
        loadModel();
    }

    /**
     * Creates a new OpenNLP NER detector with the specified type and the GENERIC locale.
     * Uses the default confidence threshold.
     *
     * @param type The type of PII this detector handles
     */
    protected OpenNLPNERDetector(String type) {
        this(type, DEFAULT_CONFIDENCE_THRESHOLD);
    }

    @Override
    protected double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    @Override
    protected void loadModel() {
        String modelPath = getModelPath();
        String cacheKey = getType() + "_" + getLocale().getCode() + "_" + modelPath;
        
        try {
            // Check if the model is already in the cache
            if (!modelCache.containsKey(cacheKey)) {
                logger.info("Loading OpenNLP model for {} ({}): {}", getType(), getLocale().getCode(), modelPath);
                
                TokenNameFinderModel model;
                
                // Check if model path is a resource or a file
                if (modelPath.startsWith("classpath:")) {
                    String resourcePath = modelPath.substring("classpath:".length());
                    try (InputStream modelIn = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                        if (modelIn == null) {
                            logger.warn("Model resource not found: {}. Using mock model for demonstration.", resourcePath);
                            // Create a mock model for demonstration if resource not found
                            model = MockModelGenerator.createMockModel();
                        } else {
                            model = new TokenNameFinderModel(modelIn);
                        }
                    }
                } else {
                    Path path = Paths.get(modelPath);
                    if (!Files.exists(path)) {
                        logger.warn("Model file not found: {}. Using mock model for demonstration.", modelPath);
                        // Create a mock model for demonstration if file not found
                        model = MockModelGenerator.createMockModel();
                    } else {
                        try (InputStream modelIn = new FileInputStream(new File(modelPath))) {
                            model = new TokenNameFinderModel(modelIn);
                        }
                    }
                }
                
                // Add the model to the cache
                modelCache.put(cacheKey, model);
            }
            
            // Create a new name finder using the model from the cache
            nameFinder = new NameFinderME(modelCache.get(cacheKey));
            
        } catch (IOException e) {
            logger.error("Error loading OpenNLP model: {}", e.getMessage(), e);
            logger.info("Using mock model for demonstration");
            try {
                // Use a mock model as fallback
                TokenNameFinderModel mockModel = MockModelGenerator.createMockModel();
                modelCache.put(cacheKey, mockModel);
                nameFinder = new NameFinderME(mockModel);
            } catch (Exception ex) {
                logger.error("Failed to create mock model: {}", ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Helper class to generate mock models for demonstration without real model files.
     * Only for demonstration - in production, real models should be used.
     */
    private static class MockModelGenerator {
        public static TokenNameFinderModel createMockModel() throws IOException {
            // Create a very basic mock model for demonstration
            // This is just a placeholder - in production, real models should be downloaded
            return new TokenNameFinderModel(InputStream.nullInputStream());
        }
    }

    /**
     * Gets the path to the OpenNLP model file for the current locale.
     * Implementations should return the appropriate model path.
     *
     * @return The path to the model file
     */
    protected abstract String getModelPath();

    @Override
    public List<PIIEntity> detect(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<PIIEntity> entities = new ArrayList<>();
        
        try {
            // Check if nameFinder was initialized successfully
            if (nameFinder == null) {
                logger.error("NameFinder not initialized properly. Returning empty result.");
                return entities;
            }
            
            // Tokenize the input text
            String[] tokens = tokenizer.tokenize(text);
            
            // Skip processing if there are no tokens
            if (tokens.length == 0) {
                return entities;
            }
            
            // Find names in the tokenized text
            Span[] spans = nameFinder.find(tokens);
            double[] probabilities = nameFinder.probs();
            
            // Calculate token start positions for mapping spans back to original text
            int[] tokenStarts = new int[tokens.length];
            int currentPos = 0;
            for (int i = 0; i < tokens.length; i++) {
                // Find the token in the original text
                currentPos = text.indexOf(tokens[i], currentPos);
                tokenStarts[i] = currentPos;
                currentPos += tokens[i].length();
            }
            
            // Process each found span
            for (int i = 0; i < spans.length; i++) {
                Span span = spans[i];
                double confidence = probabilities[i];
                
                // Skip entities with confidence below threshold
                if (confidence < getConfidenceThreshold()) {
                    continue;
                }
                
                // Calculate start and end positions in the original text
                int startPos = tokenStarts[span.getStart()];
                int endPos;
                if (span.getEnd() - 1 < tokens.length) {
                    // End position is the end of the last token in the span
                    endPos = tokenStarts[span.getEnd() - 1] + tokens[span.getEnd() - 1].length();
                } else {
                    // This should not happen, but just in case
                    endPos = text.length();
                }
                
                // Extract the exact entity text from the original
                String entityText = text.substring(startPos, endPos);
                
                // Create and add the entity
                entities.add(createEntity(startPos, endPos, entityText, confidence));
            }
            
            // Clear adaptive data from the name finder to prepare for the next call
            nameFinder.clearAdaptiveData();
            
        } catch (Exception e) {
            logger.error("Error during NER detection: {}", e.getMessage(), e);
        }
        
        return entities;
    }
}