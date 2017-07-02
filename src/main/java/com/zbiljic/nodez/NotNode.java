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
