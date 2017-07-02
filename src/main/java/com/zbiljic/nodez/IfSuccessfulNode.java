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
