# NER-Based Detection

## Overview

The Anonymize library includes Named Entity Recognition (NER) capabilities for identifying PII entities in text using machine learning models. This feature provides more accurate detection for complex entities like person names, locations, and organizations that are difficult to identify with regex patterns alone.

## How It Works

1. **Model Loading**: The library downloads and caches lightweight pre-trained NER models from Apache OpenNLP.
2. **Text Processing**: When text is analyzed, it is tokenized and passed through the model.
3. **Entity Identification**: The model identifies named entities along with confidence scores.
4. **Filtering**: Entities with confidence scores below the threshold are filtered out.
5. **Integration**: Results are integrated with the core Anonymizer pipeline.

## Performance Considerations

- **First Run**: The first time a detector is used, it needs to download the model (~5-15MB per model).
- **Model Loading**: Loading a model takes 200-500ms the first time.
- **Detection Speed**: Once loaded, detection is very fast (2-10ms for typical text).
- **Memory Usage**: Each loaded model uses ~20-30MB of memory.
- **Confidence Threshold**: Adjust the confidence threshold to balance precision and recall.

## Available Detectors

The library currently includes the following NER-based detectors:

- **PersonNameDetector**: Identifies personal names (first, last, or full names)
- **LocationDetector**: Identifies location names (cities, countries, landmarks)

## Usage Examples

### Basic Usage

```java
// Create a person name detector with default settings
PersonNameDetector personDetector = new PersonNameDetector();

// Detect person names in text
List<PIIEntity> entities = personDetector.detect("John Smith lives in New York");
```

### Custom Configuration

```java
// Create a model manager (to share models between detectors)
ModelManager modelManager = new ModelManager();

// Create detectors with custom locale and confidence threshold
PersonNameDetector personDetector = new PersonNameDetector(Locale.US, 0.8, modelManager);
LocationDetector locationDetector = new LocationDetector(Locale.US, 0.7, modelManager);

// Use with the anonymizer
Anonymizer anonymizer = Anonymizer.builder()
    .withDetector(personDetector)
    .withDetector(locationDetector)
    .withStrategy(new MaskAnonymizer())
    .build();
```

### Customizing Model Location

By default, models are downloaded to:
- `[working-dir]/models/opennlp/` if it exists, or
- `[user-home]/.anonymize/models/opennlp/` otherwise

You can specify a custom location:

```java
ModelManager modelManager = new ModelManager("/path/to/your/models");
PersonNameDetector detector = new PersonNameDetector(Locale.GENERIC, 0.8, modelManager);
```

## Extending with Custom Models

You can create custom detectors by:

1. Extending the `OpenNLPNERDetector` class
2. Specifying your own model files
3. Implementing the `getModelPath()` method

```java
public class CustomEntityDetector extends OpenNLPNERDetector {
    @Override
    protected String getModelPath() {
        return "/path/to/your/custom/model.bin";
    }
}
```

## Model Sources

The library uses models from the Apache OpenNLP project:
- Person name model: https://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
- Location model: https://opennlp.sourceforge.net/models-1.5/en-ner-location.bin

These models are automatically downloaded when needed.

## Best Practices

- Share the ModelManager instance between detectors to avoid duplicate model loading
- Pre-initialize detectors at application startup to avoid first-request latency
- Use domain-specific models for improved accuracy in specialized contexts
- Consider running detection in a background thread for large texts
- Set appropriate confidence thresholds based on your use case requirements