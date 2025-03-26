package com.anonymize.detectors.djl;

import ai.djl.MalformedModelException;
import ai.djl.ModelException;
import ai.djl.huggingface.translator.TokenClassificationTranslatorFactory;
import ai.djl.modality.nlp.translator.NamedEntity;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manages loading and caching of DJL models from Hugging Face or other model repositories. */
public class DJLModelManager {
  private static final Logger logger = LoggerFactory.getLogger(DJLModelManager.class);

  // Default model repository directory
  private static final String DEFAULT_MODEL_DIR = "models/djl";

  // Cache to avoid reloading the same models
  private final Map<String, ZooModel<String, NamedEntity[]>> modelCache = new ConcurrentHashMap<>();

  // Model repository path
  private final Path modelRepository;

  // Model URLs (model ID to URL mapping)
  private final Map<String, String> modelUrls = new HashMap<>();

  /** Creates a DJL model manager with the default model directory. */
  public DJLModelManager() {
    this(Paths.get(DEFAULT_MODEL_DIR));
  }

  /**
   * Creates a DJL model manager with a custom model directory.
   *
   * @param modelRepository The path to store and load models from
   */
  public DJLModelManager(Path modelRepository) {
    this.modelRepository = modelRepository;

    // Ensure model directory exists
    try {
      Files.createDirectories(modelRepository);
    } catch (IOException e) {
      logger.warn("Failed to create model directory: {}", e.getMessage());
    }

    // Initialize model URLs for Hugging Face models
    initializeModelUrls();
  }

  /** Initializes the mapping of model IDs to Hugging Face repository URLs. */
  private void initializeModelUrls() {
    // Setup commonly used NER/PII detection model IDs and their HF URLs
    modelUrls.put("base-bert-NER", "djl://ai.djl.huggingface.pytorch/dslim/bert-base-NER");
    modelUrls.put("pii-bert", "vietai/phobert-base-v2");
    // Add more models as needed
  }

  /**
   * Loads a DJL model by its ID.
   *
   * @param modelId The model ID (corresponds to a Hugging Face model ID or a local model)
   * @return A ZooModel object ready for prediction
   * @throws ModelNotFoundException If the model could not be found
   * @throws MalformedModelException If the model is invalid
   * @throws IOException If an I/O error occurs during model loading
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public ZooModel loadModel(String modelId)
      throws ModelNotFoundException,
          MalformedModelException,
          IOException,
          ModelException,
          TranslateException {

    // Check if model is already loaded
    if (modelCache.containsKey(modelId)) {
      return modelCache.get(modelId);
    }

    // Determine the model URL or path
    String modelUrl = modelUrls.getOrDefault(modelId, modelId);

    logger.info("Loading DJL model: {}", modelId);

    long startTime = System.currentTimeMillis();

    Criteria<String, NamedEntity[]> criteria =
        Criteria.builder()
            .setTypes(String.class, NamedEntity[].class)
            .optModelName("bert-base-NER.pt")
            .optModelUrls(modelUrl)
            .optEngine("PyTorch")
            .optOption("mapLocation", "cpu") // Use CPU for inference
            .optTranslatorFactory(new TokenClassificationTranslatorFactory())
            .optProgress(new ProgressBar())
            .build();
    ZooModel<String, NamedEntity[]> model = criteria.loadModel();

    long duration = System.currentTimeMillis() - startTime;
    logger.info("Model loading took {} ms", duration);

    // Cache the loaded model
    modelCache.put(modelId, model);
    return model;
  }

  /**
   * Checks if a model is available locally.
   *
   * @param modelId The model ID to check
   * @return true if the model is available locally, false otherwise
   */
  public boolean isModelAvailable(String modelId) {
    // Check if model is in cache
    if (modelCache.containsKey(modelId)) {
      return true;
    }

    // Check if model exists in repository
    Path modelPath = modelRepository.resolve(modelId);
    return Files.exists(modelPath);
  }

  /** Closes all loaded models and clears the cache. */
  @SuppressWarnings("rawtypes")
  public void close() {
    for (ZooModel model : modelCache.values()) {
      model.close();
    }
    modelCache.clear();
  }
}
