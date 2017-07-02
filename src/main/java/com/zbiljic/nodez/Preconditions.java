/*
 * Copyright 2017 Nemanja Zbiljić
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zbiljic.nodez;

import javax.annotation.Nullable;

import static java.lang.String.format;

/**
 * Static convenience methods that help a method or constructor check whether it was invoked
 * correctly (whether its <i>preconditions</i> have been met).
 */
final class Preconditions {

  private Preconditions() { /* No instance methods */ }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * @param expression           a boolean expression
   * @param errorMessageTemplate a template for the exception message should the check fail. The
   *                             message is formed by replacing each {@code %s} placeholder in the
   *                             template with an argument. These are matched by position - the
   *                             first {@code %s} gets {@code errorMessageArgs[0]}, etc. Unmatched
   *                             arguments will be appended to the formatted message in square
   *                             braces. Unmatched placeholders will be left as-is.
   * @param errorMessageArgs     the arguments to be substituted into the message template.
   *                             Arguments are converted to strings using {@link
   *                             String#valueOf(Object)}.
   * @throws IllegalArgumentException if {@code expression} is false
   * @throws NullPointerException     if the check fails and either {@code errorMessageTemplate} or
   *                                  {@code errorMessageArgs} is null (don't let this happen)
   */
  public static void checkArgument(
    boolean expression,
    @Nullable String errorMessageTemplate,
    @Nullable Object... errorMessageArgs) {
    if (!expression) {
      throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * @param expression a boolean expression
   * @throws IllegalStateException if {@code expression} is false
   */
  public static void checkState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @param reference an object reference
   * @return the non-null reference that was validated
   * @throws NullPointerException if {@code reference} is null
   */
  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @param reference    an object reference
   * @param errorMessage the exception message to use if the check fails; will be converted to a
   *                     string using {@link String#valueOf(Object)}
   * @return the non-null reference that was validated
   * @throws NullPointerException if {@code reference} is null
   */
  public static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }

}
