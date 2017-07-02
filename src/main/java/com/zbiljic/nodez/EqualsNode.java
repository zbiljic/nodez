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
