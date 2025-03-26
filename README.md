# Anonymize

A Java library for automated detection and redaction of Personally Identifiable Information (PII) in text.

## Overview

Anonymize is a lightweight, extensible Java library that helps you identify and redact sensitive information in your text data. It's designed for easy integration into JVM-based applications, with a focus on performance, configurability, and developer-friendliness.

## Features

- **PII Detection**: Find common PII patterns like emails, phone numbers, credit cards, and more
- **Multiple Anonymization Strategies**: Mask, remove, or tag detected PII entities
- **Advanced NER Capabilities**: Use machine learning models to detect names, locations, and organizations
- **Multiple Model Support**: OpenNLP and Hugging Face/DJL model integration
- **Extensible Architecture**: Add custom detectors and anonymization strategies
- **Locale-Aware**: Configure detectors for different locales and language patterns
- **Easy Integration**: Simple API with builder pattern for configuration
- **Audit Trail**: Get detailed metadata about detected and anonymized entities

## Usage Example

```java
// Create an anonymizer with detectors and anonymization strategy
Anonymizer anonymizer = new Anonymizer.Builder()
        .withDetector(new EmailDetector())
        .withDetector(new PhoneNumberDetector())
        .withAnonymizerStrategy(new MaskAnonymizer())
        .build();

// Process text
String text = "Contact us at support@example.com or (555) 123-4567";
AnonymizationResult result = anonymizer.anonymize(text);

// Get the anonymized output
System.out.println(result.getAnonymizedText());
// Output: "Contact us at [REDACTED] or [REDACTED]"

// Get metadata about detected entities
for (PIIEntity entity : result.getDetectedEntities()) {
    System.out.println(entity.getType() + ": " + entity.getText());
}
```

## Advanced Usage

### Using NER-based Detectors

```java
// Create model managers (to share models between detectors)
ModelManager openNlpManager = new ModelManager();
DJLModelManager djlManager = new DJLModelManager();

// Create NER-based detectors
HuggingFacePIIDetector huggingFaceDetector = new HuggingFacePIIDetector(djlManager);

// Create an anonymizer with multiple detector types
Anonymizer anonymizer = new Anonymizer.Builder()
    .withDetector(new EmailDetector())
    .withDetector(huggingFaceDetector)
    .withAnonymizerStrategy(new TagAnonymizer())
    .build();

// Process text with multiple entity types
String text = "John Smith lives in New York and works at support@example.com";
AnonymizationResult result = anonymizer.anonymize(text);

// Output: "<PERSON_NAME_1> lives in <LOCATION_1> and works at <EMAIL_1>"
System.out.println(result.getAnonymizedText());
```

### Using Different Anonymization Strategies

```java
// Mask strategy (default replaces with [REDACTED])
MaskAnonymizer maskAnonymizer = new MaskAnonymizer();

// Custom mask pattern
MaskAnonymizer customMaskAnonymizer = new MaskAnonymizer("***PRIVATE***");

// Remove strategy (removes the entity completely)
RemoveAnonymizer removeAnonymizer = new RemoveAnonymizer();

// Tag strategy (replaces with typed tokens like <TYPE_1>)
TagAnonymizer tagAnonymizer = new TagAnonymizer();

// Custom tag format
TagAnonymizer customTagAnonymizer = new TagAnonymizer("[[%s-%d]]");
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
./run_cli.sh
```

## Models

The library supports two types of NER models:

1. **OpenNLP Models**: Lightweight models for basic named entity recognition
2. **DJL/Hugging Face Models**: More powerful transformer-based models for advanced entity detection

Models are automatically downloaded when first used and cached for future use.

## License

This project is licensed under the MIT License - see the LICENSE file for details.