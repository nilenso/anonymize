package com.anonymize.core;

import com.anonymize.common.PIIEntity;
import com.anonymize.detectors.Detector;
import com.anonymize.strategies.MaskRedactor;
import com.anonymize.strategies.RedactionStrategy;
import com.anonymize.strategies.Redactor;
import com.anonymize.strategies.RemoveRedactor;
import com.anonymize.strategies.TokenizeRedactor;

import java.util.ArrayList;
import java.util.List;

/**
 * Core class for PII anonymization that orchestrates detection and redaction processes.
 */
public class Anonymizer {
    private final List<Detector> detectors;
    private final Redactor redactor;

    private Anonymizer(Builder builder) {
        this.detectors = builder.detectors;
        this.redactor = builder.redactor;
    }

    /**
     * Anonymizes text by first detecting PII entities and then applying the redaction strategy.
     *
     * @param text The text to anonymize
     * @return The result containing both redacted text and metadata
     */
    public AnonymizationResult anonymize(String text) {
        if (text == null || text.isEmpty()) {
            return new AnonymizationResult("", "", new ArrayList<>(), redactor.getStrategy());
        }

        List<PIIEntity> allEntities = new ArrayList<>();
        
        // Run all detectors to find PII entities
        for (Detector detector : detectors) {
            List<PIIEntity> entities = detector.detect(text);
            if (entities != null && !entities.isEmpty()) {
                allEntities.addAll(entities);
            }
        }
        
        // Apply redaction strategy to the detected entities
        String redactedText = redactor.redact(text, allEntities);
        
        return new AnonymizationResult(text, redactedText, allEntities, redactor.getStrategy());
    }

    /**
     * Builder for configuring and creating an Anonymizer instance.
     */
    public static class Builder {
        private final List<Detector> detectors = new ArrayList<>();
        private Redactor redactor = new MaskRedactor();

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
         * Sets the redaction strategy to be used.
         *
         * @param strategy The redaction strategy to use
         * @return The builder instance for method chaining
         */
        public Builder withRedactionStrategy(RedactionStrategy strategy) {
            switch (strategy) {
                case MASK:
                    this.redactor = new MaskRedactor();
                    break;
                case REMOVE:
                    this.redactor = new RemoveRedactor();
                    break;
                case TOKENIZE:
                    this.redactor = new TokenizeRedactor();
                    break;
                default:
                    this.redactor = new MaskRedactor();
                    break;
            }
            return this;
        }

        /**
         * Sets a custom redactor to be used.
         *
         * @param redactor The custom redactor to use
         * @return The builder instance for method chaining
         */
        public Builder withRedactor(Redactor redactor) {
            if (redactor != null) {
                this.redactor = redactor;
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