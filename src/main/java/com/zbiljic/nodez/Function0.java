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

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a function with no arguments.
 *
 * @param <R> return type of the function
 */
@FunctionalInterface
public interface Function0<R> extends Serializable, Supplier<R> {

  /**
   * The <a href="https://docs.oracle.com/javase/8/docs/api/index.html">serial version uid</a>.
   */
  long serialVersionUID = 1L;

  //@formatter:off
  /**
   * Creates a {@code Function0} based on
   * <ul>
   * <li><a href="https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html">method reference</a></li>
   * <li><a href="https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html#syntax">lambda expression</a></li>
   * </ul>
   *
   * @param methodReference (typically) a method reference, e.g. {@code Type::method}
   * @param <R>             return type
   * @return a {@code Function0}
   */
  //@formatter:on
  static <R> Function0<R> of(Function0<R> methodReference) {
    return methodReference;
  }

  /**
   * Narrows the given {@code Supplier<? extends R>} to {@code Function0<R>}.
   *
   * @param f   A {@code Function0}
   * @param <R> return type
   * @return the given {@code f} instance as narrowed type {@code Function0<R>}
   */
  @SuppressWarnings("unchecked")
  static <R> Function0<R> narrow(Supplier<? extends R> f) {
    return (Function0<R>) f;
  }

  /**
   * Applies this function to no arguments and returns the result.
   *
   * @return the result of function application
   */
  R apply();

  /**
   * Implementation of {@linkplain Supplier#get()}, just calls {@linkplain #apply()}.
   *
   * @return the result of {@code apply()}
   */
  @Override
  default R get() {
    return apply();
  }

  /**
   * Returns a function that always returns the constant value that you give in parameter.
   *
   * @param <R>   the result type
   * @param value the value to be returned
   * @return a function always returning the given value
   */
  static <R> Function0<R> constant(R value) {
    return () -> value;
  }

  /**
   * Returns a composed function that first applies this Function0 to the given argument and then
   * applies {@linkplain Function} {@code after} to the result.
   *
   * @param <V>   return type of after
   * @param after the function applied after this
   * @return a function composed of this and after
   * @throws NullPointerException if after is null
   */
  default <V> Function0<V> andThen(Function<? super R, ? extends V> after) {
    Objects.requireNonNull(after, "after is null");
    return () -> after.apply(apply());
  }

}
