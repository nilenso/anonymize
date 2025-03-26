package com.anonymize.core;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.detectors.Detector;
import com.anonymize.strategies.AnonymizerStrategy;
import com.anonymize.strategies.MaskAnonymizer;
import java.util.ArrayList;
import java.util.List;

/** Core class for PII anonymization that orchestrates detection and anonymization processes. */
public class Anonymizer {
  private final List<Detector> detectors;
  private final AnonymizerStrategy anonymizerStrategy;
  private final Locale locale;

  private Anonymizer(Builder builder) {
    this.detectors = builder.detectors;
    this.anonymizerStrategy = builder.anonymizerStrategy;
    this.locale = builder.locale;
  }

  /**
   * Anonymizes text by first detecting PII entities and then applying the anonymization strategy.
   *
   * @param text The text to anonymize
   * @return The result containing both anonymized text and metadata
   */
  public AnonymizationResult anonymize(String text) {
    if (text == null || text.isEmpty()) {
      return new AnonymizationResult(
          "", "", new ArrayList<>(), anonymizerStrategy.getStrategyName(), locale);
    }

    List<PIIEntity> allEntities = new ArrayList<>();

    // Run all detectors to find PII entities
    for (Detector detector : detectors) {
      // Skip detectors that don't support the current locale
      if (!detector.supportsLocale(locale)) {
        continue;
      }

      List<PIIEntity> entities = detector.detect(text);
      if (entities != null && !entities.isEmpty()) {
        allEntities.addAll(entities);
      }
    }

    // Apply anonymization strategy to the detected entities
    String anonymizedText = anonymizerStrategy.anonymize(text, allEntities);

    return new AnonymizationResult(
        text, anonymizedText, allEntities, anonymizerStrategy.getStrategyName(), locale);
  }

  /**
   * Gets the locale this anonymizer is configured for.
   *
   * @return The locale
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Creates a new builder for configuring an Anonymizer instance.
   *
   * @return A new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for configuring and creating an Anonymizer instance. */
  public static class Builder {
    private final List<Detector> detectors = new ArrayList<>();
    private AnonymizerStrategy anonymizerStrategy = new MaskAnonymizer();
    private Locale locale = Locale.GENERIC;

    /**
     * Adds a detector to the anonymizer.
     *
     * @param detector The detector to add
     * @return The builder instance for method chaining
     */
    public Builder withDetector(Detector detector) {
      if (detector != null) {
        this.detectors.add(detector);
      }
      return this;
    }

    /**
     * Sets multiple detectors to be used by the anonymizer.
     *
     * @param detectors The list of detectors to use
     * @return The builder instance for method chaining
     */
    public Builder withDetectors(List<Detector> detectors) {
      if (detectors != null) {
        this.detectors.addAll(detectors);
      }
      return this;
    }

    /**
     * Sets the anonymization strategy to be used.
     *
     * @param anonymizerStrategy The anonymization strategy implementation to use
     * @return The builder instance for method chaining
     */
    public Builder withAnonymizerStrategy(AnonymizerStrategy anonymizerStrategy) {
      if (anonymizerStrategy != null) {
        this.anonymizerStrategy = anonymizerStrategy;
      }
      return this;
    }

    /**
     * Sets the locale to be used for detection.
     *
     * @param locale The locale to use
     * @return The builder instance for method chaining
     */
    public Builder withLocale(Locale locale) {
      if (locale != null) {
        this.locale = locale;
      }
      return this;
    }

    /**
     * Builds and returns a configured Anonymizer instance.
     *
     * @return The configured Anonymizer
     */
    public Anonymizer build() {
      return new Anonymizer(this);
    }
  }
}
