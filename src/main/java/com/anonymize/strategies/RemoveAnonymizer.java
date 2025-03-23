package com.anonymize.strategies;

import com.anonymize.common.PIIEntity;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements an anonymization strategy that removes PII entities from the text entirely.
 */
public class RemoveAnonymizer implements AnonymizerStrategy {

    @Override
    public String anonymize(String text, List<PIIEntity> entities) {
        if (text == null || text.isEmpty() || entities == null || entities.isEmpty()) {
            return text;
        }

        // Sort entities by start position in reverse order to avoid offset issues when replacing
        List<PIIEntity> sortedEntities = entities.stream()
                .sorted(Comparator.comparing(PIIEntity::getStartPosition).reversed())
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder(text);
        for (PIIEntity entity : sortedEntities) {
            result.delete(entity.getStartPosition(), entity.getEndPosition());
        }

        return result.toString();
    }

    @Override
    public String getStrategyName() {
        return "REMOVE";
    }
}