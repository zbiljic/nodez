package com.zbiljic.nodez.debug;

/**
 * A class to build debug messages with different levels. We also duplicate debug messages with a
 * level > 2 to debug logs using slf4j.
 */
public class DebugMessageBuilder {

  // We use an indentation step of 2 blank spaces.
  private static final String INDENTATION_STEP = "  ";

  private final DebugLevel level;
  private final StringBuilder builder;
  private int currentIndentationLevel;

  /** Creates a new DebugMessageBuilder instance. */
  public DebugMessageBuilder() {
    this(new StringBuilder(), DebugLevel.NONE);
  }

  /** Creates a new DebugMessageBuilder instance. */
  public DebugMessageBuilder(DebugLevel debugLevel) {
    this(new StringBuilder(), debugLevel);
  }

  /** Creates a new DebugMessageBuilder instance. */
  public DebugMessageBuilder(String initString, DebugLevel debugLevel) {
    this(new StringBuilder(initString), debugLevel);
  }

  /** Creates a new DebugMessageBuilder instance. */
  public DebugMessageBuilder(StringBuilder builder, DebugLevel level) {
    this.builder = (builder == null) ? new StringBuilder() : builder;
    this.level = level;
    setIndentationLevel(0);
  }

  /** Returns the current debug level. */
  public DebugLevel getLevel() {
    return level;
  }

  public boolean isEmpty() {
    return builder.length() == 0;
  }

  /** Resets this builder. */
  public void reset() {
    builder.setLength(0);
    setIndentationLevel(0);
  }

  public void setIndentationLevel(int indentationLevel) {
    this.currentIndentationLevel = Math.max(0, indentationLevel);
  }

  public DebugMessageBuilder indent() {
    setIndentationLevel(currentIndentationLevel + 1);
    return this;
  }

  public DebugMessageBuilder unindent() {
    setIndentationLevel(currentIndentationLevel - 1);
    return this;
  }

  private DebugMessageBuilder appendDebug(DebugLevel msgLevel, final String message, Object... args) {
    if (!message.isEmpty()) {
      if (getLevel().ordinal() >= msgLevel.ordinal()
        || getLevel().ordinal() >= DebugLevel.DETAILED.ordinal()) {
        String formattedMessage = args.length == 0 ? message : String.format(message, args);
        if (getLevel().ordinal() >= msgLevel.ordinal()) {
          appendIndentation();
          builder.append(formattedMessage);
          builder.append("\n");
        }
      }
    }
    return this;
  }

  private void appendIndentation() {
    for (int i = 0; i < currentIndentationLevel; ++i) {
      builder.append(INDENTATION_STEP);
    }
  }

  public DebugMessageBuilder basic(final String message, Object... args) {
    return appendDebug(DebugLevel.BASIC, message, args);
  }

  public DebugMessageBuilder detailed(final String message, Object... args) {
    return appendDebug(DebugLevel.DETAILED, message, args);
  }

  public DebugMessageBuilder verbose(final String message, Object... args) {
    return appendDebug(DebugLevel.VERBOSE, message, args);
  }

  public boolean isEnabled() {
    return getLevel().ordinal() >= DebugLevel.BASIC.ordinal();
  }

  public boolean isDetailed() {
    return getLevel().ordinal() >= DebugLevel.DETAILED.ordinal();
  }

  public boolean isVerbose() {
    return getLevel().ordinal() >= DebugLevel.VERBOSE.ordinal();
  }

  @Override
  public String toString() {
    return builder.toString();
  }
}
