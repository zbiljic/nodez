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

import java.util.concurrent.CompletableFuture;

/**
 * ValueNode represents a {@code Node} with a fixed value.
 *
 * @param <R> return type of the node
 * @see Node
 * @see NullableNode
 */
public class ValueNode<R> extends NullableNode<R> {

  static <T> ValueNode<T> create(T value) {
    return new ValueNode<>(value, null);
  }

  static <T> ValueNode<T> create(T value, String name) {
    return new ValueNode<>(value, name);
  }

  private final R value;

  /**
   * Creates a {@link Node} with a fixed value.
   */
  protected ValueNode(R value, String name) {
    super(name != null
      ? name
      : String.format("value[%s]", valueStringInName(value)));
    this.value = value;
  }

  private static <R> String valueStringInName(R value) {
    if (value instanceof Boolean
      || value instanceof Number
      || value instanceof Enum) {
      return String.valueOf(value);
    }
    return value == null
      ? "null"
      : value.getClass().getSimpleName();
  }

  @Override
  public String getResponseClassName() {
    return value == null ? "" : value.getClass().getSimpleName();
  }

  @Override
  protected CompletableFuture<R> evaluate() {
    return CompletableFuture.completedFuture(value);
  }

  @Override
  public R emit() {
    return value;
  }

  @Override
  protected void logStart() {
    // print nothing as value node is too simple
  }

  @Override
  protected void logEnd() {
    // print nothing as value node is too simple
  }
}
