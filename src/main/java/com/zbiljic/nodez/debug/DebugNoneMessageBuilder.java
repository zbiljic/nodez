package com.zbiljic.nodez.debug;

/**
 * A variant of {@code DebugMessageBuilder} that has no effect and could be safely shared.
 *
 * @see DebugMessageBuilder
 */
public final class DebugNoneMessageBuilder extends DebugMessageBuilder {

  private static final DebugNoneMessageBuilder SINGLETON = new DebugNoneMessageBuilder();

  public static DebugMessageBuilder getInstance() {
    return SINGLETON;
  }

  private DebugNoneMessageBuilder() {
    super(DebugLevel.NONE);
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public void reset() {
  }

  @Override
  public void setIndentationLevel(int indentationLevel) {
  }

  @Override
  public DebugMessageBuilder indent() {
    return this;
  }

  @Override
  public DebugMessageBuilder unindent() {
    return this;
  }

  @Override
  public DebugMessageBuilder basic(String message, Object... args) {
    return this;
  }

  @Override
  public DebugMessageBuilder detailed(String message, Object... args) {
    return this;
  }

  @Override
  public DebugMessageBuilder verbose(String message, Object... args) {
    return this;
  }
}
