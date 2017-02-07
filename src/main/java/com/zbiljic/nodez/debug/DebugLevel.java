package com.zbiljic.nodez.debug;

/**
 * Debug levels of {@link DebugManager}.
 */
public enum DebugLevel {

  /**
   * Disable debug logging (no log entries will be output).
   */
  NONE,

  /**
   * Output only basic debug log entries.
   */
  BASIC,

  /**
   * Output detailed debug log entries.
   */
  DETAILED,

  /**
   * Output all debug log entries.
   */
  VERBOSE;

}
