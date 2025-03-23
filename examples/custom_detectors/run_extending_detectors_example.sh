#!/bin/bash

# Create build directory
mkdir -p build/classes

# Compile all Java files
javac -d build/classes $(find src/main/java -name "*.java")

# Run the extending detectors example
java -cp build/classes com.anonymize.examples.ExtendingDetectorsExample