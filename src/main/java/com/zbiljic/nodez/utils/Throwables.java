package com.zbiljic.nodez.utils;

import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Static utility methods pertaining to instances of {@link Throwable}.
 */
public enum Throwables {
  ;

  /**
   * Returns the innermost cause of {@code throwable}, or null if the given throwable is null. If
   * the root cause is over 1000 level deep, the original throwable will be returned defensively as
   * this is heuristically considered a circular reference, however unlikely. The first throwable in
   * a chain provides context from when the error or exception was initially detected. Example
   * usage:
   * <pre>
   *   assertEquals("Unable to assign a customer id", Throwables.getRootCause(e).getMessage());
   * </pre>
   */
  public static Throwable getRootCause(Throwable throwable) {
    if (throwable == null) {
      return throwable;
    }
    Throwable t = throwable;
    // defend against (malicious?) circularity
    for (int i = 0; i < 1000; i++) {
      Throwable cause = t.getCause();
      if (cause == null) {
        return t;
      }
      t = cause;
    }
    // Too bad.  Return the original exception.
    LoggerFactory.getLogger(Throwables.class).debug(
      "Possible circular reference detected on " + throwable.getClass()
        + ": [" + throwable + "]");
    return throwable;
  }

  /**
   * Throws {@code throwable} if it is a {@link RuntimeException} or {@link Error}. Example usage:
   *
   * <pre>
   * for (Foo foo : foos) {
   *   try {
   *     foo.bar();
   *   } catch (RuntimeException | Error t) {
   *     failure = t;
   *   }
   * }
   * if (failure != null) {
   *   throwIfUnchecked(failure);
   *   throw new AssertionError(failure);
   * }
   * </pre>
   */
  public static void throwIfUnchecked(Throwable throwable) {
    if (throwable == null) {
      throw new NullPointerException();
    }
    if (throwable instanceof RuntimeException) {
      throw (RuntimeException) throwable;
    }
    if (throwable instanceof Error) {
      throw (Error) throwable;
    }
  }

  /**
   * Returns a string containing the result of {@link Throwable#toString() toString()}, followed by
   * the full, recursive stack trace of {@code throwable}. Note that you probably should not be
   * parsing the resulting string; if you need programmatic access to the stack frames, you can call
   * {@link Throwable#getStackTrace()}.
   */
  public static String getStackTraceAsString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

}
