package com.anonymize.core;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import java.util.List;

/**
 * Represents the result of an anonymization operation, including both the anonymized text and
 * metadata about detected PII entities.
 */
public class AnonymizationResult {
  private final String originalText;
  private final String anonymizedText;
  private final List<PIIEntity> detectedEntities;
  private final String strategyUsed;
  private final Locale locale;

  /**
   * Creates a new anonymization result.
   *
   * @param originalText The original text before anonymization
   * @param anonymizedText The anonymized text
   * @param detectedEntities List of detected PII entities
   * @param strategyUsed The name of the anonymization strategy used
   */
  public AnonymizationResult(
      String originalText,
      String anonymizedText,
      List<PIIEntity> detectedEntities,
      String strategyUsed) {
    this(originalText, anonymizedText, detectedEntities, strategyUsed, Locale.GENERIC);
  }

  /**
   * Creates a new anonymization result with locale information.
   *
   * @param originalText The original text before anonymization
   * @param anonymizedText The anonymized text
   * @param detectedEntities List of detected PII entities
   * @param strategyUsed The name of the anonymization strategy used
   * @param locale The locale used for detection
   */
  public AnonymizationResult(
      String originalText,
      String anonymizedText,
      List<PIIEntity> detectedEntities,
      String strategyUsed,
      Locale locale) {
    this.originalText = originalText;
    this.anonymizedText = anonymizedText;
    this.detectedEntities = detectedEntities;
    this.strategyUsed = strategyUsed;
    this.locale = locale;
  }

  /**
   * Gets the original text before anonymization.
   *
   * @return The original text
   */
  public String getOriginalText() {
    return originalText;
  }

  /**
   * Gets the anonymized text after applying the anonymization strategy.
   *
   * @return The anonymized text
   */
  public String getAnonymizedText() {
    return anonymizedText;
  }

  /**
   * Gets the redacted text (alias for getAnonymizedText() for backward compatibility).
   *
   * @return The anonymized text
   * @deprecated Use getAnonymizedText() instead
   */
  @Deprecated
  public String getRedactedText() {
    return anonymizedText;
  }

  /**
   * Gets a list of all detected PII entities in the original text.
   *
   * @return List of detected PII entities
   */
  public List<PIIEntity> getDetectedEntities() {
    return detectedEntities;
  }

  /**
   * Gets the anonymization strategy that was applied.
   *
   * @return The name of the anonymization strategy used
   */
  public String getStrategyUsed() {
    return strategyUsed;
  }

  /**
   * Gets the locale used for detection.
   *
   * @return The locale
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Checks if any PII entities were detected.
   *
   * @return true if PII entities were found, false otherwise
   */
  public boolean hasDetectedEntities() {
    return detectedEntities != null && !detectedEntities.isEmpty();
  }

  /**
   * Gets the count of detected PII entities.
   *
   * @return Number of detected entities
   */
  public int getDetectionCount() {
    return detectedEntities != null ? detectedEntities.size() : 0;
  }
}
