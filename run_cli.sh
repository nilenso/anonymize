#!/bin/bash

# Create build directory
mkdir -p build/classes

# Compile all Java files
javac -d build/classes $(find src/main/java -name "*.java")

# Run the CLI application
java -cp build/classes com.anonymize.cli.AnonymizeCli