package com.zbiljic.nodez;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Optional wrapper for turning required nodes into {@link Optional} nodes.
 *
 * @param <T> return type of the node
 * @see Node
 */
final class OptionalNodeWrapper<T> extends Node<Optional<T>> {

  private final Node<T> wrappedNode;

  public OptionalNodeWrapper(Node<T> node) {
    super(String.format("~%s", node.getName()), true, false, node);
    this.wrappedNode = node;
  }

  Node<T> getWrappedNode() {
    return wrappedNode;
  }

  @Override
  protected CompletableFuture<Optional<T>> evaluate() {
    T emitted = wrappedNode.emit();
    return CompletableFuture.completedFuture(emitted == null
      ? Optional.<T>empty()
      : Optional.of(emitted));
  }
}
