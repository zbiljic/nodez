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
 * Optional wrapper for turning required nodes into {@link Optional} nodes.
 *
 * @param <T> return type of the node
 * @see Node
 */
final class OptionalNodeWrapper<T> extends Node<Optional<T>> {

  private final Node<T> wrappedNode;

  OptionalNodeWrapper(Node<T> node) {
    super(String.format("~%s", node.getName()), true, false, node);
    this.wrappedNode = node;
    setDeciderSupplier(node.deciderSupplier);
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
