# PIIType System

## Overview

The `PIIType` enum system provides a standardized way to categorize and handle different types of personally identifiable information (PII) within the Anonymize library. This system ensures consistent classification across different detectors and simplifies entity handling in anonymization strategies.

## Available PII Types

The library defines the following standard PII types:

| PIIType        | Description                               | Example                     |
|----------------|-------------------------------------------|----------------------------|
| EMAIL          | Email addresses                           | john.doe@example.com       |
| PHONE_NUMBER   | Phone numbers in various formats          | (555) 123-4567             |
| CREDIT_CARD    | Credit card numbers                       | 4111-1111-1111-1111        |
| SSN            | Social Security Numbers                   | 123-45-6789                |
| IP_ADDRESS     | IP addresses (v4 and v6)                  | 192.168.0.1                |
| PERSON_NAME    | Person names                              | John Smith                 |
| ORGANIZATION   | Company or organization names             | Acme Corporation           |
| LOCATION       | Place names or addresses                  | New York City              |
| DATE_OF_BIRTH  | Dates that indicate birth                 | 01/15/1990                 |
| ADDRESS        | Physical addresses                        | 123 Main St                |
| MISC           | Miscellaneous entities detected by models | Various unclassified PIIs  |
| CUSTOM         | Custom PII types defined by users         | User-defined PIIs          |

## Using PIITypes

### In Detectors

Detectors return `PIIEntity` objects that include a `PIIType`:

```java
public class EmailDetector extends BaseRegexDetector {
    // Constructor specifies which PIIType this detector handles
    public EmailDetector() {
        super(PIIType.EMAIL, Locale.GENERIC);
    }
    
    // detect() method returns entities with the specified type
    @Override
    public List<PIIEntity> detect(String text) {
        // ...detection logic...
        return new PIIEntity(PIIType.EMAIL, startPos, endPos, match, 1.0);
    }
}
```

### Advanced Model Integration

The library maps entity types from machine learning models to the standard `PIIType` system:

```java
// In OpenNLPNERDetector
protected PIIType mapEntityType(String modelEntityType) {
    switch (modelEntityType.toLowerCase()) {
        case "person": return PIIType.PERSON_NAME;
        case "organization": return PIIType.ORGANIZATION;
        case "location": return PIIType.LOCATION;
        // ...more mappings...
        default: return PIIType.MISC;
    }
}
```

### In Anonymization Strategies

Anonymization strategies can use the `PIIType` to customize handling:

```java
// In TagAnonymizer
public String anonymize(String text, List<PIIEntity> entities) {
    // ...
    for (PIIEntity entity : entities) {
        String type = entity.getType().getValue(); // Get the type name
        int counter = typeCounters.getOrDefault(type, 0) + 1;
        typeCounters.put(type, counter);
        
        // Create type-specific tags, like <EMAIL_1>, <PERSON_NAME_2>
        String token = String.format(tokenFormat, type, counter);
        result.replace(entity.getStartPosition(), entity.getEndPosition(), token);
    }
    // ...
}
```

## Working with PIIType

### Converting Between String and Enum

The `PIIType` enum provides methods to convert between string values and enum constants:

```java
// String to enum
PIIType type = PIIType.fromValue("EMAIL"); // Returns PIIType.EMAIL
PIIType fallback = PIIType.fromValue("UNKNOWN_TYPE"); // Returns PIIType.CUSTOM

// Enum to string
String value = PIIType.EMAIL.getValue(); // Returns "EMAIL"
```

### Creating PIIEntity Objects

```java
// Create with PIIType enum
PIIEntity entity1 = new PIIEntity(
    PIIType.EMAIL,         // The type enum
    10,                    // Start position
    30,                    // End position
    "john.doe@example.com", // The detected text
    0.95                   // Confidence score
);

// Create with string type (converted to enum internally)
PIIEntity entity2 = new PIIEntity(
    "EMAIL",               // String type (converted to PIIType.EMAIL)
    10, 
    30, 
    "john.doe@example.com", 
    0.95
);
```

## Extending with Custom Types

While the library provides common PII types, you can handle custom types by:

1. Using `PIIType.CUSTOM` for your specializations
2. Adding metadata to the entity text or using naming conventions
3. Creating custom anonymization strategies that recognize your specific formats

## Best Practices

- Use the standard `PIIType` enum values whenever possible for consistency
- When creating custom detectors, choose the most appropriate existing type
- For truly custom entity types that don't fit the standard categories, use `PIIType.CUSTOM`
- Consider the `PIIType` when designing anonymization strategies - different types may need different handling