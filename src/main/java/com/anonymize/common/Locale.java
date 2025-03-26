package com.anonymize.common;

/**
 * Represents geographic locales supported by the library. Used to determine region-specific
 * detection patterns for PII entities.
 */
public enum Locale {
  /** United States locale. */
  US("US", "United States"),

  /** United Kingdom locale. */
  UK("UK", "United Kingdom"),

  /** European Union locale. */
  EU("EU", "European Union"),

  /** India locale. */
  INDIA("IN", "India"),

  /** Canada locale. */
  CANADA("CA", "Canada"),

  /** Australia locale. */
  AUSTRALIA("AU", "Australia"),

  /** Generic locale for default/fallback patterns. */
  GENERIC("GEN", "Generic/International");

  private final String code;
  private final String displayName;

  Locale(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  /**
   * Gets the locale code.
   *
   * @return The locale code
   */
  public String getCode() {
    return code;
  }

  /**
   * Gets the display name of the locale.
   *
   * @return The display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Gets a locale by its code.
   *
   * @param code The locale code
   * @return The corresponding Locale or GENERIC if not found
   */
  public static Locale fromCode(String code) {
    for (Locale locale : values()) {
      if (locale.getCode().equalsIgnoreCase(code)) {
        return locale;
      }
    }
    return GENERIC;
  }
}
