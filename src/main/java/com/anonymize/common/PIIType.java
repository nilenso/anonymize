package com.anonymize.common;

/**
 * Enumeration of common PII entity types that can be detected.
 */
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
    CUSTOM("CUSTOM");

    private final String value;

    PIIType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PIIType fromValue(String value) {
        for (PIIType type : PIIType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return CUSTOM;
    }
}