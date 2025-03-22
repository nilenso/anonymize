package com.anonymize.common;

/**
 * Represents a detected PII entity within a text.
 */
public class PIIEntity {
    private final String type;
    private final int startPosition;
    private final int endPosition;
    private final String text;
    private final double confidence;

    public PIIEntity(String type, int startPosition, int endPosition, String text, double confidence) {
        this.type = type;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.text = text;
        this.confidence = confidence;
    }

    public String getType() {
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
        return "PIIEntity{" +
                "type='" + type + '\'' +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", text='" + text + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}