package com.anonymize.detectors;

import com.anonymize.common.Locale;
import com.anonymize.common.PIIEntity;
import com.anonymize.common.PIIType;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for all detectors that use regular expressions to identify PII entities. Provides a
 * framework for locale-aware, extensible pattern matching.
 */
public abstract class BaseRegexDetector extends AbstractDetector {
  private final PIIType type;

  // Map of locale-specific patterns
  private final Map<Locale, List<String>> localePatterns = new HashMap<>();

  // Compiled patterns for the current locale
  private final List<Pattern> compiledPatterns = new ArrayList<>();

  // Default confidence level for matches
  private final double defaultConfidence;

  /**
   * Creates a new BaseRegexDetector with the specified type, locale, supported locales, and
   * confidence level.
   *
   * @param type The type of PII this detector handles
   * @param locale The locale this detector is configured for
   * @param supportedLocales Set of locales supported by this detector
   * @param defaultConfidence Default confidence level for detected entities
   * @param defaultPatterns Map of default patterns for different locales
   */
  protected BaseRegexDetector(
      PIIType type,
      Locale locale,
      Set<Locale> supportedLocales,
      double defaultConfidence,
      Map<Locale, List<String>> defaultPatterns) {
    super(type.getValue(), locale, supportedLocales);
    this.type = type;
    this.defaultConfidence = defaultConfidence;

    // Initialize with default patterns
    if (defaultPatterns != null) {
      for (Map.Entry<Locale, List<String>> entry : defaultPatterns.entrySet()) {
        localePatterns.put(entry.getKey(), new ArrayList<>(entry.getValue()));
      }
    }

    initializePatterns();
  }

  /**
   * Creates a new BaseRegexDetector with default locale (GENERIC).
   *
   * @param type The type of PII this detector handles
   * @param defaultConfidence Default confidence level for detected entities
   * @param defaultPatterns Map of default patterns for different locales
   */
  protected BaseRegexDetector(
      PIIType type, double defaultConfidence, Map<Locale, List<String>> defaultPatterns) {
    this(
        type,
        Locale.GENERIC,
        getSupportedLocalesFrom(defaultPatterns),
        defaultConfidence,
        defaultPatterns);
  }

  /**
   * Helper method to extract the set of supported locales from the patterns map.
   *
   * @param patternsMap Map of locale-specific patterns
   * @return Set of locales from the map keys
   */
  private static Set<Locale> getSupportedLocalesFrom(Map<Locale, List<String>> patternsMap) {
    return patternsMap != null ? new HashSet<>(patternsMap.keySet()) : new HashSet<>();
  }

  /** Initialize patterns based on the configured locale. */
  protected void initializePatterns() {
    compiledPatterns.clear();

    // Add patterns for the current locale
    List<String> currentLocalePatterns = localePatterns.get(getLocale());
    if (currentLocalePatterns != null) {
      for (String pattern : currentLocalePatterns) {
        try {
          compiledPatterns.add(Pattern.compile(pattern));
        } catch (Exception e) {
          // Skip invalid patterns
          System.err.println("Invalid pattern: " + pattern + " - " + e.getMessage());
        }
      }
    }

    // Also add generic patterns if not already using the GENERIC locale
    if (getLocale() != Locale.GENERIC && localePatterns.containsKey(Locale.GENERIC)) {
      List<String> genericPatterns = localePatterns.get(Locale.GENERIC);
      if (genericPatterns != null) {
        for (String pattern : genericPatterns) {
          try {
            compiledPatterns.add(Pattern.compile(pattern));
          } catch (Exception e) {
            // Skip invalid patterns
            System.err.println("Invalid pattern: " + pattern + " - " + e.getMessage());
          }
        }
      }
    }
  }

  @Override
  public List<PIIEntity> detect(String text) {
    List<PIIEntity> results = new ArrayList<>();
    if (text == null || text.isEmpty()) {
      return results;
    }

    for (Pattern pattern : compiledPatterns) {
      Matcher matcher = pattern.matcher(text);
      while (matcher.find()) {
        String match = matcher.group();
        int start = matcher.start();
        int end = matcher.end();

        // Check if this match overlaps with any existing match
        boolean overlaps = false;
        for (PIIEntity existing : results) {
          if ((start >= existing.getStartPosition() && start < existing.getEndPosition())
              || (end > existing.getStartPosition() && end <= existing.getEndPosition())) {
            overlaps = true;
            break;
          }
        }

        if (!overlaps) {
          // Allow subclasses to modify confidence based on the pattern match if needed
          double confidence = calculateConfidence(match, pattern.pattern());

          results.add(new PIIEntity(type, start, end, match, confidence));
        }
      }
    }

    return results;
  }

  /**
   * Calculate confidence for a specific match. Can be overridden by subclasses to provide
   * pattern-specific confidence levels.
   *
   * @param match The matched text
   * @param patternString The pattern that matched
   * @return Confidence level for this match (0.0-1.0)
   */
  protected double calculateConfidence(String match, String patternString) {
    return defaultConfidence;
  }

  /**
   * Gets the patterns for a specific locale.
   *
   * @param locale The locale to get patterns for
   * @return List of pattern strings for the locale, or empty list if none exist
   */
  public List<String> getPatternsForLocale(Locale locale) {
    List<String> patterns = localePatterns.get(locale);
    return patterns != null ? new ArrayList<>(patterns) : new ArrayList<>();
  }

  /**
   * Gets all patterns for the current locale.
   *
   * @return List of pattern strings for the current locale
   */
  public List<String> getPatterns() {
    return getPatternsForLocale(getLocale());
  }

  /**
   * Add a custom pattern for the current locale.
   *
   * @param pattern The regex pattern string to add
   * @return This detector instance for method chaining
   */
  public BaseRegexDetector addPattern(String pattern) {
    return addPattern(getLocale(), pattern);
  }

  /**
   * Add a custom pattern for a specific locale.
   *
   * @param locale The locale to add the pattern for
   * @param pattern The regex pattern string to add
   * @return This detector instance for method chaining
   */
  public BaseRegexDetector addPattern(Locale locale, String pattern) {
    if (pattern == null || pattern.trim().isEmpty()) {
      return this;
    }

    // Make sure the locale exists in our map
    if (!localePatterns.containsKey(locale)) {
      localePatterns.put(locale, new ArrayList<>());
    }

    localePatterns.get(locale).add(pattern);

    // Reinitialize patterns since we've made changes
    initializePatterns();

    return this;
  }

  /**
   * Add multiple custom patterns for the current locale.
   *
   * @param patterns The list of regex pattern strings to add
   * @return This detector instance for method chaining
   */
  public BaseRegexDetector addPatterns(List<String> patterns) {
    return addPatterns(getLocale(), patterns);
  }

  /**
   * Add multiple custom patterns for a specific locale.
   *
   * @param locale The locale to add patterns for
   * @param patterns The list of regex pattern strings to add
   * @return This detector instance for method chaining
   */
  public BaseRegexDetector addPatterns(Locale locale, List<String> patterns) {
    if (patterns == null || patterns.isEmpty()) {
      return this;
    }

    // Make sure the locale exists in our map
    if (!localePatterns.containsKey(locale)) {
      localePatterns.put(locale, new ArrayList<>());
    }

    localePatterns.get(locale).addAll(patterns);

    // Reinitialize patterns since we've made changes
    initializePatterns();

    return this;
  }

  /**
   * Clear all patterns for a specific locale.
   *
   * @param locale The locale to clear patterns for
   * @return This detector instance for method chaining
   */
  public BaseRegexDetector clearPatterns(Locale locale) {
    if (localePatterns.containsKey(locale)) {
      localePatterns.get(locale).clear();

      // Reinitialize patterns since we've made changes
      initializePatterns();
    }

    return this;
  }

  /**
   * Replace all patterns for a specific locale.
   *
   * @param locale The locale to set patterns for
   * @param patterns The new patterns to use
   * @return This detector instance for method chaining
   */
  public BaseRegexDetector setPatterns(Locale locale, List<String> patterns) {
    if (patterns == null) {
      return this;
    }

    localePatterns.put(locale, new ArrayList<>(patterns));

    // Reinitialize patterns since we've made changes
    initializePatterns();

    return this;
  }

  /**
   * Create a new locale with custom patterns.
   *
   * @param locale The new locale to create
   * @param patterns The patterns for this locale
   * @return This detector instance for method chaining
   */
  public BaseRegexDetector addLocale(Locale locale, List<String> patterns) {
    if (locale == null || patterns == null || patterns.isEmpty()) {
      return this;
    }

    localePatterns.put(locale, new ArrayList<>(patterns));

    // Reinitialize patterns since we've made changes
    initializePatterns();

    return this;
  }

  /**
   * Get the current set of compiled patterns.
   *
   * @return List of compiled patterns
   */
  protected List<Pattern> getCompiledPatterns() {
    return new ArrayList<>(compiledPatterns);
  }
}
