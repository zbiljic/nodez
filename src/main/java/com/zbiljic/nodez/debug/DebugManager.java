package com.zbiljic.nodez.debug;

import java.util.concurrent.atomic.AtomicReference;

/**
 * DebugManager controls debug message building (via {@link DebugMessageBuilder}) on a per-request
 * basis.
 */
public final class DebugManager {

  private DebugManager() { /* No instance methods */ }

  private static final AtomicReference<DebugMessageBuilder> DEBUG_MSG_BUILDER = new AtomicReference<>();

  static {
    DEBUG_MSG_BUILDER.set(DebugNoneMessageBuilder.getInstance());
  }

  /**
   * Update the request thread local with the builder to use for this request.
   */
  public static void update(final DebugMessageBuilder builder) {
    DEBUG_MSG_BUILDER.set(builder);
  }

  /**
   * Clear all request specific thread local. This should be called after the request is complete.
   */
  public static void clear() {
    DEBUG_MSG_BUILDER.set(DebugNoneMessageBuilder.getInstance());
  }

  // for testing
  public static void resetForTest() {
    update(DebugNoneMessageBuilder.getInstance());
  }

  // for testing
  public static void resetForTest(DebugLevel level) {
    update(new DebugMessageBuilder(level));
  }

  /**
   * This builder is a thread-local, which will be automatically preserved across threads.
   *
   * @return the {@code DebugMessageBuilder} for the current thread.
   */
  public static DebugMessageBuilder getDebugMessageBuilder() {
    return DEBUG_MSG_BUILDER.get();
  }

  /**
   * Test if debug messages with the defined level will be output.
   *
   * @param level Debug level to test
   * @return {@code true} if defined debug level is enabled, otherwise {@code false}
   */
  private static boolean isDebugLevelEnabled(final DebugLevel level) {
    return getDebugMessageBuilder().getLevel().ordinal() >= level.ordinal();
  }

  /**
   * Test if debug messages with the level basic will be output.
   *
   * @return {@code true} if basic is enabled, otherwise {@code false}
   */
  public static boolean isBasicEnabled() {
    return isDebugLevelEnabled(DebugLevel.BASIC);
  }

  /**
   * Test if debug messages with the level detailed will be output.
   *
   * @return {@code true} if detailed is enabled, otherwise {@code false}
   */
  public static boolean isDetailedEnabled() {
    return isDebugLevelEnabled(DebugLevel.DETAILED);
  }

  /**
   * Test if debug messages with the level verbose will be output.
   *
   * @return {@code true} if verbose is enabled, otherwise {@code false}
   */
  public static boolean isVerboseEnabled() {
    return isDebugLevelEnabled(DebugLevel.VERBOSE);
  }

  //
  // Some helpers for convenience
  //

  public static boolean isEnabled() {
    return getDebugMessageBuilder().getLevel() != DebugLevel.NONE;
  }

  public static DebugMessageBuilder basic(final String message, Object... args) {
    return getDebugMessageBuilder().basic(message, args);
  }

  public static DebugMessageBuilder detailed(final String message, Object... args) {
    return getDebugMessageBuilder().detailed(message, args);
  }

  public static DebugMessageBuilder verbose(final String message, Object... args) {
    return getDebugMessageBuilder().verbose(message, args);
  }

  public static String getDebugMessage() {
    return getDebugMessageBuilder().toString();
  }

}
