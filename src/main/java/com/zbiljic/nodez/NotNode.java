package com.zbiljic.nodez;

import java.util.concurrent.CompletableFuture;

/**
 * The {@code NotNode} represents a logic negation of a predicate node.
 * <p>
 * This is typically used for async predicates.
 *
 * @see Node
 */
public class NotNode extends Node<Boolean> {

  public static NotNode of(Node<Boolean> node) {
    return new NotNode(node);
  }

  private final Node<Boolean> node;

  public NotNode(Node<Boolean> node) {
    super("NOT::" + node.getName(), node);
    this.node = node;
  }

  @Override
  protected final CompletableFuture<Boolean> evaluate() {
    return CompletableFuture.completedFuture(!node.emit());
  }
}
