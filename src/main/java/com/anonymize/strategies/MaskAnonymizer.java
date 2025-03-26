package com.anonymize.strategies;

import com.anonymize.common.PIIEntity;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Implements an anonymization strategy that replaces PII entities with a fixed mask. */
public class MaskAnonymizer implements AnonymizerStrategy {
  private final String mask;

  /** Creates a new MaskAnonymizer with the default mask. */
  public MaskAnonymizer() {
    this("[REDACTED]");
  }

  /**
   * Creates a new MaskAnonymizer with a custom mask.
   *
   * @param mask The mask to use for anonymization
   */
  public MaskAnonymizer(String mask) {
    this.mask = mask;
  }

  @Override
  public String anonymize(String text, List<PIIEntity> entities) {
    if (text == null || text.isEmpty() || entities == null || entities.isEmpty()) {
      return text;
    }

    // Sort entities by start position in reverse order to avoid offset issues when replacing
    List<PIIEntity> sortedEntities =
        entities.stream()
            .sorted(Comparator.comparing(PIIEntity::getStartPosition).reversed())
            .collect(Collectors.toList());

    StringBuilder result = new StringBuilder(text);
    for (PIIEntity entity : sortedEntities) {
      result.replace(entity.getStartPosition(), entity.getEndPosition(), mask);
    }

    return result.toString();
  }

  @Override
  public String getStrategyName() {
    return "MASK";
  }
}
