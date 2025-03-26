package com.anonymize.common;

/** Enumeration of common PII entity types that can be detected. */
public enum PIIType {
  EMAIL("EMAIL"),
  PHONE_NUMBER("PHONE_NUMBER"),
  CREDIT_CARD("CREDIT_CARD"),
  SSN("SSN"),
  IP_ADDRESS("IP_ADDRESS"),
  PERSON_NAME("PERSON_NAME"),
  ORGANIZATION("ORGANIZATION"),
  LOCATION("LOCATION"),
  DATE_OF_BIRTH("DATE_OF_BIRTH"),
  ADDRESS("ADDRESS"),
  MISC("MISC"), // For miscellaneous entities detected by some models
  CUSTOM("CUSTOM");

  private final String value;

  PIIType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Converts a string value to the corresponding PIIType.
   *
   * @param value The string value to convert
   * @return The corresponding PIIType, or CUSTOM if no match is found
   */
  public static PIIType fromValue(String value) {
    if (value == null) {
      return CUSTOM;
    }

    // Check for direct matches with enum values
    for (PIIType type : PIIType.values()) {
      if (type.getValue().equalsIgnoreCase(value)) {
        return type;
      }
    }

    return CUSTOM;
  }
}
