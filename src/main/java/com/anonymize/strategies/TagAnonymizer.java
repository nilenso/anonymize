package com.anonymize.strategies;

import com.anonymize.common.PIIEntity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements an anonymization strategy that replaces PII entities with tokens like <TYPE_N>.
 */
public class TagAnonymizer implements AnonymizerStrategy {
    private final Map<String, Integer> typeCounters = new HashMap<>();
    private final String tokenFormat;

    /**
     * Creates a new TokenizeAnonymizer with the default token format.
     */
    public TagAnonymizer() {
        this("<%s_%d>");
    }

    /**
     * Creates a new TokenizeAnonymizer with a custom token format.
     * Format should include %s for the type and %d for the counter.
     *
     * @param tokenFormat The format to use for tokens
     */
    public TagAnonymizer(String tokenFormat) {
        this.tokenFormat = tokenFormat;
    }

    @Override
    public String anonymize(String text, List<PIIEntity> entities) {
        if (text == null || text.isEmpty() || entities == null || entities.isEmpty()) {
            return text;
        }

        // Reset counters for consistent tokenization within one anonymization operation
        typeCounters.clear();
        
        // Sort entities by start position in reverse order to avoid offset issues when replacing
        List<PIIEntity> sortedEntities = entities.stream()
                .sorted(Comparator.comparing(PIIEntity::getStartPosition).reversed())
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder(text);
        for (PIIEntity entity : sortedEntities) {
            String type = entity.getType().getValue();
            int counter = typeCounters.getOrDefault(type, 0) + 1;
            typeCounters.put(type, counter);
            
            String token = String.format(tokenFormat, type, counter);
            result.replace(entity.getStartPosition(), entity.getEndPosition(), token);
        }

        return result.toString();
    }

    @Override
    public String getStrategyName() {
        return "TOKENIZE";
    }
}