package com.zbiljic.nodez;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Checks if the values stored in two nodes are equal.
 *
 * @see Node
 */
public class EqualsNode<T> extends Node<Boolean> {

  public static <T> EqualsNode<T> create(Node<T> nodeA, Node<T> nodeB) {
    return new EqualsNode<>(nodeA, nodeB);
  }

  private final Node<T> nodeA;
  private final Node<T> nodeB;

  private EqualsNode(Node<T> nodeA, Node<T> nodeB) {
    super("Equals", nodeA, nodeB);
    this.nodeA = nodeA;
    this.nodeB = nodeB;
  }

  @Override
  protected CompletableFuture<Boolean> evaluate() throws Exception {
    return CompletableFuture.completedFuture(Objects.equals(nodeA.emit(), nodeB.emit()));
  }
}
