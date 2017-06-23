package com.zbiljic.nodez;

import java.util.function.Function;

/**
 * A {@link Function} with a name.
 * <p>
 * Use this to create static constant functions to be used in node transformations, etc.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public abstract class NamedFunction<T, R> implements Function<T, R> {

  public static <T, R> NamedFunction<T, R> create(String name, Function<T, R> function) {
    return new NamedFunction<T, R>(name) {
      @Override
      public R apply(T t) {
        return function.apply(t);
      }
    };
  }

  private final String name;

  protected NamedFunction(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
