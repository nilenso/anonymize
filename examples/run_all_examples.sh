#!/bin/bash

# Create build directory
mkdir -p build/classes

# Compile all Java files
javac -d build/classes $(find src/main/java -name "*.java")

echo "=== Running Basic Example ==="
java -cp build/classes com.anonymize.examples.BasicExample
echo

echo "=== Running Tokenization Example ==="
java -cp build/classes com.anonymize.examples.TokenizationExample
echo

echo "=== Running Custom Configuration Example ==="
java -cp build/classes com.anonymize.examples.CustomConfigExample
echo

echo "=== Running Phone Detection Example ==="
java -cp build/classes com.anonymize.examples.PhoneDetectionExample