package com.anonymize.common;

/** Represents a detected PII entity within a text. */
public class PIIEntity {
  private final PIIType type;
  private final int startPosition;
  private final int endPosition;
  private final String text;
  private final double confidence;

  public PIIEntity(
      PIIType type, int startPosition, int endPosition, String text, double confidence) {
    this.type = type;
    this.startPosition = startPosition;
    this.endPosition = endPosition;
    this.text = text;
    this.confidence = confidence;
  }

  /**
   * Creates a PIIEntity from a string type value. This is a convenience constructor that handles
   * the conversion from String to PIIType.
   *
   * @param typeValue The string value of the PIIType
   * @param startPosition The starting position of the entity in the text
   * @param endPosition The ending position of the entity in the text
   * @param text The detected text
   * @param confidence The confidence level of the detection
   */
  public PIIEntity(
      String typeValue, int startPosition, int endPosition, String text, double confidence) {
    this(PIIType.fromValue(typeValue), startPosition, endPosition, text, confidence);
  }

  public PIIType getType() {
    return type;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public int getEndPosition() {
    return endPosition;
  }

  public String getText() {
    return text;
  }

  public double getConfidence() {
    return confidence;
  }

  @Override
  public String toString() {
    return "PIIEntity{"
        + "type='"
        + type
        + '\''
        + ", startPosition="
        + startPosition
        + ", endPosition="
        + endPosition
        + ", text='"
        + text
        + '\''
        + ", confidence="
        + confidence
        + '}';
  }
}
