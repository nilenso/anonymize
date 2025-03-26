# NER-Based Detection

## Overview

The Anonymize library includes Named Entity Recognition (NER) capabilities for identifying PII entities in text using machine learning models. This feature provides more accurate detection for complex entities like person names, locations, and organizations that are difficult to identify with regex patterns alone.

## Supported Models

The library supports two types of models:

1. **OpenNLP Models**: Lightweight, traditional NER models for basic entity detection
2. **Hugging Face Models via DJL**: State-of-the-art transformer models for more accurate and comprehensive entity detection

## How It Works

1. **Model Loading**: The library downloads and caches pre-trained NER models.
2. **Text Processing**: When text is analyzed, it is tokenized and passed through the model.
3. **Entity Identification**: The model identifies named entities along with confidence scores.
4. **Filtering**: Entities with confidence scores below the threshold are filtered out.
5. **Entity Merging**: Multi-token entities are merged into single entities (e.g., "John Smith" instead of "John" and "Smith").
6. **Type Mapping**: Entity types from the model are mapped to PIIType enums.
7. **Integration**: Results are integrated with the core Anonymizer pipeline.

## Performance Considerations

- **First Run**: The first time a detector is used, it needs to download the model:
  - OpenNLP models: ~5-15MB per model
  - Hugging Face models: ~50-500MB depending on the model
- **Model Loading**: 
  - OpenNLP: 200-500ms the first time
  - Hugging Face/DJL: 1-5 seconds the first time
- **Detection Speed**: 
  - OpenNLP: Very fast (2-10ms for typical text)
  - Hugging Face: Slower (50-200ms for typical text) but more accurate
- **Memory Usage**: 
  - OpenNLP: ~20-30MB per model
  - Hugging Face: ~100-500MB per model
- **Confidence Threshold**: Adjust the confidence threshold to balance precision and recall.

## Available Detectors

### OpenNLP Detectors

- **PersonNameDetector**: Identifies personal names (first, last, or full names)
- **LocationDetector**: Identifies location names (cities, countries, landmarks)

### DJL Detectors

- **HuggingFacePIIDetector**: Multi-type detector that can identify persons, organizations, locations, and misc entities in a single pass using transformer models

## Usage Examples

### Basic OpenNLP Usage

```java
// Create a person name detector with default settings
PersonNameDetector personDetector = new PersonNameDetector();

// Detect person names in text
List<PIIEntity> entities = personDetector.detect("John Smith lives in New York");
```

### Using Hugging Face Models via DJL

```java
// Create a DJL model manager
DJLModelManager modelManager = new DJLModelManager();

// Create a Hugging Face PII detector
HuggingFacePIIDetector detector = new HuggingFacePIIDetector(modelManager);

// Detect multiple entity types in a single pass
List<PIIEntity> entities = detector.detect("John Smith works at Google in New York City");

// Output will contain person, organization, and location entities
```

### Custom Configuration

```java
// Create model managers (to share models between detectors)
ModelManager openNlpManager = new ModelManager();
DJLModelManager djlManager = new DJLModelManager();

// Create detectors with custom locale and confidence threshold
PersonNameDetector personDetector = new PersonNameDetector(Locale.US, 0.8, openNlpManager);
LocationDetector locationDetector = new LocationDetector(Locale.US, 0.7, openNlpManager);
HuggingFacePIIDetector huggingFaceDetector = new HuggingFacePIIDetector(djlManager);

// Use with the anonymizer
Anonymizer anonymizer = new Anonymizer.Builder()
    .withDetector(personDetector)
    .withDetector(locationDetector)
    .withDetector(huggingFaceDetector)
    .withAnonymizerStrategy(new MaskAnonymizer())
    .build();
```

### Customizing Model Location

By default, models are downloaded to:
- OpenNLP: `[working-dir]/models/opennlp/` if it exists, or `[user-home]/.anonymize/models/opennlp/` otherwise
- DJL/Hugging Face: `[user-home]/.djl.ai/` (managed by DJL)

You can specify a custom location for OpenNLP models:

```java
ModelManager modelManager = new ModelManager("/path/to/your/models");
PersonNameDetector detector = new PersonNameDetector(Locale.GENERIC, 0.8, modelManager);
```

## Extending with Custom Models

### OpenNLP

You can create custom OpenNLP detectors by:

1. Extending the `OpenNLPNERDetector` class
2. Specifying your own model files
3. Implementing the `getModelPath()` method
4. Optionally overriding the `mapEntityType()` method to customize type mapping

```java
public class CustomEntityDetector extends OpenNLPNERDetector {
    @Override
    protected String getModelPath() {
        return "/path/to/your/custom/model.bin";
    }
    
    @Override
    protected PIIType mapEntityType(String modelEntityType) {
        // Custom mapping from model entity types to PIIType
        if ("CUSTOM_TYPE".equals(modelEntityType)) {
            return PIIType.CUSTOM;
        }
        return super.mapEntityType(modelEntityType);
    }
}
```

### Hugging Face/DJL

You can use different Hugging Face models by customizing the DJL model manager:

```java
DJLModelManager modelManager = new DJLModelManager();
modelManager.addModelMapping("custom-model-id", "org-name/model-name");
HuggingFacePIIDetector detector = new HuggingFacePIIDetector(modelManager, "custom-model-id");
```

## Model Sources

The library uses models from:

### OpenNLP Project
- Person name model: https://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
- Location model: https://opennlp.sourceforge.net/models-1.5/en-ner-location.bin

### Hugging Face
- Default NER model: https://huggingface.co/dslim/bert-base-NER

These models are automatically downloaded when needed.

## Best Practices

- Share ModelManager instances between detectors to avoid duplicate model loading
- Pre-initialize detectors at application startup to avoid first-request latency
- For maximum accuracy, combine multiple detector types (regex for structured entities, NER for unstructured)
- Use OpenNLP for speed, Hugging Face for accuracy
- Consider running detection in a background thread for large texts
- Set appropriate confidence thresholds based on your use case requirements (higher for precision, lower for recall)