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
    return new ValueNode<>(null, value);
  }

  static <T> ValueNode<T> create(String name, T value) {
    return new ValueNode<>(name, value);
  }

  private final R value;

  /**
   * Creates a {@link Node} with a fixed value.
   */
  protected ValueNode(String name, R value) {
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
