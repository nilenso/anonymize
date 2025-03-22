# Anonymize

A Java library for automated detection and redaction of Personally Identifiable Information (PII) in text.

## Overview

Anonymize is a lightweight, extensible Java library that helps you identify and redact sensitive information in your text data. It's designed for easy integration into JVM-based applications, with a focus on performance, configurability, and developer-friendliness.

## Features

- **PII Detection**: Find common PII patterns like emails, phone numbers, credit cards, and more
- **Multiple Redaction Strategies**: Mask, remove, or tokenize detected PII entities
- **Extensible Architecture**: Add custom detectors and redaction strategies
- **Easy Integration**: Simple API with builder pattern for configuration
- **Audit Trail**: Get detailed metadata about detected and redacted entities

## Usage Example

```java
// Create an anonymizer with detectors and redaction strategy
Anonymizer anonymizer = new Anonymizer.Builder()
        .withDetector(new EmailDetector())
        .withDetector(new PhoneNumberDetector())
        .withRedactionStrategy(RedactionStrategy.MASK)
        .build();

// Process text
String text = "Contact us at support@example.com or (555) 123-4567";
AnonymizationResult result = anonymizer.anonymize(text);

// Get the redacted output
System.out.println(result.getRedactedText());
// Output: "Contact us at [REDACTED] or [REDACTED]"

// Get metadata about detected entities
for (PIIEntity entity : result.getDetectedEntities()) {
    System.out.println(entity.getType() + ": " + entity.getText());
}
```

## Getting Started

### Prerequisites

- Java 11 or later

### Building from Source

```bash
./gradlew build
```

### Running the CLI Demo

```bash
./gradlew run
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.