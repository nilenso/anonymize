# Anonymize Development Guidelines

## Build & Test Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Run single test: `./gradlew test --tests com.anonymize.path.to.TestClass`
- Test with specific Java version: `./gradlew test -Pjava.version=11|17|21`
- Lint: `./gradlew spotlessApply`
- Code quality check: `./gradlew checkstyleMain`

## Java Compatibility
- Target compatibility: Java 11, 17, 21
- Set in build.gradle: `sourceCompatibility = 11`
- CI runs tests on all supported Java versions
- Avoid Java 17+ APIs when writing core functionality
- Document Java version requirements for optional features

## Code Style
- **Formatting**: Follow Google Java Style Guide
- **Naming**: CamelCase for classes, lowerCamelCase for methods/variables
- **Imports**: No wildcard imports; organize imports alphabetically
- **Type Safety**: Prefer strong typing; use Optional<T> for nullable returns
- **Error Handling**: Use custom exceptions; no empty catch blocks
- **Logging**: SLF4J with meaningful context; no sensitive data in logs
- **Documentation**: Javadoc for public APIs; include examples for detectors

## Project Structure
- Modular packages by feature (detectors, strategies, processors)
- Unit tests with JUnit 5; mock external dependencies
- YAML configs in src/main/resources
- Use Builder pattern for configuration objects