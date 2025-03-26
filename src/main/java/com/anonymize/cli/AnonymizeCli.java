package com.anonymize.cli;

import com.anonymize.common.PIIEntity;
import com.anonymize.core.AnonymizationResult;
import com.anonymize.core.Anonymizer;
import com.anonymize.detectors.EmailDetector;
import com.anonymize.detectors.PhoneNumberDetector;
import com.anonymize.strategies.MaskAnonymizer;
import com.anonymize.strategies.RemoveAnonymizer;
import com.anonymize.strategies.TagAnonymizer;
import java.util.Scanner;

/** Command-line interface application for demonstrating the Anonymize library. */
public class AnonymizeCli {

  public static void main(String[] args) {
    System.out.println("===== Anonymize - PII Detection and Anonymization Tool =====");

    // Create an anonymizer with default detectors and strategy
    Anonymizer anonymizer =
        new Anonymizer.Builder()
            .withDetector(new EmailDetector())
            .withDetector(new PhoneNumberDetector())
            .withAnonymizerStrategy(new MaskAnonymizer())
            .build();

    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.println("\nEnter text to anonymize (or 'quit' to exit):");
      String input = scanner.nextLine();

      if ("quit".equalsIgnoreCase(input) || "exit".equalsIgnoreCase(input)) {
        break;
      }

      // Process the input
      AnonymizationResult result = anonymizer.anonymize(input);

      // Display results
      System.out.println("\nAnonymized Text:");
      System.out.println(result.getAnonymizedText());

      if (result.hasDetectedEntities()) {
        System.out.println("\nDetected PII Entities:");
        for (PIIEntity entity : result.getDetectedEntities()) {
          System.out.println(
              "Type: "
                  + entity.getType()
                  + " | Text: "
                  + entity.getText()
                  + " | Position: "
                  + entity.getStartPosition()
                  + "-"
                  + entity.getEndPosition()
                  + " | Confidence: "
                  + entity.getConfidence());
        }
        System.out.println("\nTotal entities found: " + result.getDetectionCount());
      } else {
        System.out.println("\nNo PII entities detected in the input.");
      }
    }

    scanner.close();
    System.out.println("Goodbye!");
  }

  /** Demonstrates the usage of different anonymization strategies. */
  private static void demonstrateAnonymizationStrategies() {
    String sampleText = "Contact John Doe at john.doe@example.com or call at (555) 123-4567.";

    // Create anonymizers with different strategies
    Anonymizer maskAnonymizer =
        new Anonymizer.Builder()
            .withDetector(new EmailDetector())
            .withDetector(new PhoneNumberDetector())
            .withAnonymizerStrategy(new MaskAnonymizer())
            .build();

    Anonymizer removeAnonymizer =
        new Anonymizer.Builder()
            .withDetector(new EmailDetector())
            .withDetector(new PhoneNumberDetector())
            .withAnonymizerStrategy(new RemoveAnonymizer())
            .build();

    Anonymizer tokenizeAnonymizer =
        new Anonymizer.Builder()
            .withDetector(new EmailDetector())
            .withDetector(new PhoneNumberDetector())
            .withAnonymizerStrategy(new TagAnonymizer())
            .build();

    // Display results with different strategies
    System.out.println("Original: " + sampleText);
    System.out.println("Masked: " + maskAnonymizer.anonymize(sampleText).getAnonymizedText());
    System.out.println("Removed: " + removeAnonymizer.anonymize(sampleText).getAnonymizedText());
    System.out.println(
        "Tokenized: " + tokenizeAnonymizer.anonymize(sampleText).getAnonymizedText());
  }
}
