package com.zbiljic.nodez;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code Node} that allows null value output.
 * <p>
 * NOTE: right now this has a few constructors, add more here to match the parent {@code Node} class
 * if needed.
 *
 * @param <T> return type of the node
 * @see Node
 */
public abstract class NullableNode<T> extends Node<T> {

  /**
   * Wrap a {@link CompletableFuture} object into a nullable {@link Node}.
   */
  public static <T> Node wrapCompletableFuture(final CompletableFuture<T> future) {
    return new NullableNode<T>() {
      @Override
      protected CompletableFuture<T> evaluate() {
        return future;
      }
    };
  }

  protected NullableNode() {
    super(false, true);
  }

  protected NullableNode(String name) {
    super(name, false, true);
  }

  protected NullableNode(Node... nodes) {
    super(false, true, nodes);
  }

  protected NullableNode(String name, Node... nodes) {
    super(name, false, true, nodes);
  }

  protected NullableNode(String name, Collection<Node> dependentNodes) {
    super(name, false, true, dependentNodes);
  }
}
