package com.anonymize.strategies;

import static org.junit.jupiter.api.Assertions.*;

import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.EmailDetector;
import com.anonymize.detectors.PhoneNumberDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the RemoveAnonymizer strategy, which completely removes PII entities from text. */
public class RemoveAnonymizerTest {

  private Anonymizer removeAnonymizer;

  @BeforeEach
  public void setup() {
    // Create anonymizer with RemoveAnonymizer strategy
    removeAnonymizer =
        new Anonymizer.Builder()
            .withDetector(new EmailDetector())
            .withDetector(new PhoneNumberDetector())
            .withAnonymizerStrategy(new RemoveAnonymizer())
            .build();
  }

  @Test
  public void testBasicRemoval() {
    // Test basic removal of PII
    String text = "Please contact me at user@example.com";
    AnonymizationResult result = removeAnonymizer.anonymize(text);

    // Print results for debugging
    System.out.println("Original: " + text);
    System.out.println("After removal: " + result.getAnonymizedText());

    // Verify PII was detected
    assertTrue(result.getDetectedEntities().size() > 0, "Should detect at least one entity");

    // Verify PII was removed completely
    assertFalse(
        result.getAnonymizedText().contains("user@example.com"),
        "Email should be removed from text");

    // Verify expected result - PII should be completely removed
    String expected = "Please contact me at ";
    assertEquals(expected, result.getAnonymizedText(), "PII should be completely removed");
  }

  @Test
  public void testRemovingMultipleEntities() {
    // Test removing multiple entities
    String text = "Email me at info@example.com or call (555) 123-4567";
    AnonymizationResult result = removeAnonymizer.anonymize(text);

    // Print results for debugging
    System.out.println("Original: " + text);
    System.out.println("After removal: " + result.getAnonymizedText());

    // Verify some entities were detected
    assertTrue(result.getDetectedEntities().size() > 0, "Should detect at least one entity");

    // Verify PII was removed completely
    assertFalse(result.getAnonymizedText().contains("info@example.com"), "Email should be removed");

    // If phone was detected, it should be removed
    boolean phoneDetected = false;
    for (PIIEntity entity : result.getDetectedEntities()) {
      if (entity.getType().equals(PIIType.PHONE_NUMBER)) {
        phoneDetected = true;
        assertFalse(
            result.getAnonymizedText().contains(entity.getText()),
            "Phone number should be removed");
      }
    }
  }

  @Test
  public void testRemovingPreservesContext() {
    // Test removal within a larger context
    String text = "This is an email address: contact@test.com and some more text.";
    AnonymizationResult result = removeAnonymizer.anonymize(text);

    // Print results for debugging
    System.out.println("Original: " + text);
    System.out.println("After removal: " + result.getAnonymizedText());

    // Verify context is preserved
    assertTrue(
        result.getAnonymizedText().startsWith("This is an email address: "),
        "Text before email should be preserved");
    assertTrue(
        result.getAnonymizedText().endsWith(" and some more text."),
        "Text after email should be preserved");
  }

  @Test
  public void testRemovingInSentence() {
    // Test removal in middle of sentence
    String text = "If you need help, email support@company.com today.";
    AnonymizationResult result = removeAnonymizer.anonymize(text);

    // Verify PII was detected and removed
    assertTrue(result.getDetectedEntities().size() > 0, "Should detect the email");
    assertFalse(
        result.getAnonymizedText().contains("support@company.com"), "Email should be removed");

    // Verify sentence structure
    assertTrue(
        result.getAnonymizedText().contains("email  today"),
        "Email should be removed but context preserved");
  }
}
