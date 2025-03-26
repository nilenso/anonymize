package com.anonymize.detectors.djl;

import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.translator.NamedEntity;
import ai.djl.util.JsonUtils;
import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuggingFacePIIDetector extends BaseDJLDetector {
  private static final Logger logger = LoggerFactory.getLogger(HuggingFacePIIDetector.class);

  public HuggingFacePIIDetector(DJLModelManager modelManager) {
    super(
        "PII",
        Locale.GENERIC,
        Set.of(Locale.GENERIC),
        "base-bert-NER", // maps to dslim/bert-base-NER in DJLModelManager
        modelManager);
  }

  @Override
  protected Predictor<String, NamedEntity[]> initializePredictor() throws IOException {
    // return model.newPredictor(new PIITranslator());
    return model.newPredictor();
  }

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
      long startTime = System.nanoTime();
      // Perform prediction using the model's predictor
      NamedEntity[] results = predictor.predict(text);
      long endTime = System.nanoTime();
      double duration = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds

      if (logger.isDebugEnabled()) {
        logger.debug("Prediction results: {}", JsonUtils.GSON_PRETTY.toJson(results));
        logger.debug("Prediction time: {} ms", String.format("%.2f", duration));
      }
      List<PIIEntity> entities = processNamedEntities(results);

      return entities;
    } catch (Exception e) {
      logger.error("Error performing DJL detection: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  @Override
  protected PIIType mapEntityType(String modelEntityType) {
    if (modelEntityType == null) {
      logger.debug("Null entity type provided");
      return PIIType.MISC;
    }

    switch (modelEntityType) {
      case "B-PER":
      case "I-PER":
        return PIIType.PERSON_NAME;
      case "B-ORG":
      case "I-ORG":
        return PIIType.ORGANIZATION;
      case "B-LOC":
      case "I-LOC":
        return PIIType.LOCATION;
      case "B-MISC":
      case "I-MISC":
        return PIIType.MISC;
      default:
        logger.debug("Unmapped entity type: {}", modelEntityType);
        return PIIType.MISC; // Return MISC instead of null to prevent NullPointerExceptions
    }
  }

  /**
   * Process named entities from model output into PIIEntity objects. Combines B-* and I-* tagged
   * entities into single entities.
   */
  public List<PIIEntity> processNamedEntities(NamedEntity[] entities) {
    if (entities == null || entities.length == 0) {
      return Collections.emptyList();
    }

    List<PIIEntity> piiEntities = new ArrayList<>();
    StringBuilder currentText = new StringBuilder();
    double confidenceSum = 0;
    int entityCount = 0;
    int startPos = -1;
    PIIType currentType = null;

    for (int i = 0; i < entities.length; i++) {
      NamedEntity entity = entities[i];
      if (entity.getScore() < confidenceThreshold) {
        continue;
      }

      String entityType = entity.getEntity();
      PIIType mappedType = mapEntityType(entityType);

      if (entityType.startsWith("B-")) {
        // If we were building an entity, finish it
        if (currentText.length() > 0 && currentType != null && i > 0) {
          // Use currentType for the entity being finished, not the new entity's type
          piiEntities.add(
              new PIIEntity(
                  currentType,
                  startPos,
                  entities[i - 1].getEnd(),
                  currentText.toString().trim(),
                  confidenceSum / entityCount));
        }
        // Start new entity
        currentText = new StringBuilder(entity.getWord());
        startPos = entity.getStart();
        confidenceSum = entity.getScore();
        entityCount = 1;
        currentType = mappedType;
      } else if (entityType.startsWith("I-") && currentText.length() > 0 && currentType != null) {

        if (mappedType.equals(currentType)) {
          currentText.append(" ").append(entity.getWord());
          confidenceSum += entity.getScore();
          entityCount++;
        }
      }
    }

    // Add final entity if exists
    if (currentText.length() > 0 && currentType != null && entities.length > 0) {
      piiEntities.add(
          new PIIEntity(
              currentType,
              startPos,
              entities[entities.length - 1].getEnd(),
              currentText.toString().trim(),
              confidenceSum / entityCount));
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Detected {} entities: {}", piiEntities.size(), piiEntities);
    }

    return piiEntities;
  }
}
