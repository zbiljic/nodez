package com.zbiljic.nodez;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A simple wrapper node converting success state of a node to a boolean.
 *
 * @see Node
 */
public class IfSuccessfulNode extends Node<Boolean> {

  /**
   * Create an {@code IfSuccessfulNode} based on any given node.
   */
  public static <T> IfSuccessfulNode create(Node<T> node) {
    return new IfSuccessfulNode(Node.optional(node));
  }

  private final Node node;

  IfSuccessfulNode(Node node) {
    super(String.format("SUCCESS::%s", node.getName()), node);
    this.node = node;
  }

  @Override
  protected CompletableFuture<Boolean> evaluate() throws Exception {
    Optional optional = (Optional) node.emit();
    return CompletableFuture.completedFuture(optional.isPresent());
  }
}
