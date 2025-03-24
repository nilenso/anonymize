# Java Version Compatibility Guide

## Supported Java Versions

The Anonymize library is designed to be compatible with multiple Java versions:

- **Java 11** (LTS): Primary target version for core functionality
- **Java 17** (LTS): Fully supported
- **Java 21** (LTS): Fully supported

## Testing Across Java Versions

The project includes Gradle tasks to verify compatibility with each supported Java version:

```bash
# Test with Java 11 (default)
./gradlew test

# Test with Java 17
./gradlew testWithJava17

# Test with Java 21
./gradlew testWithJava21

# Test with all supported Java versions
./gradlew testAllJavaVersions
```

## Development Guidelines

When contributing to this library, please follow these guidelines to maintain cross-version compatibility:

1. **Core Functionality**: Use only Java 11 features for core library functionality
2. **Optional Features**: Use Java 17+ features only for optional components
3. **Documentation**: Clearly document when a feature requires a specific Java version
4. **Conditional Features**: Use reflection or optional dependencies for Java 17+ specific features
5. **Testing**: Ensure tests pass on all supported Java versions

## Version-Specific APIs

### Java 11 Baseline Features

All core functionality is implemented using Java 11 APIs to ensure maximum compatibility.

### Java 17 Optional Features

- None currently implemented

### Java 21 Optional Features

- None currently implemented

## Toolchain Configuration

The project uses Gradle Toolchains to simplify testing across multiple Java versions. You can inspect the configuration in the `build.gradle` file.