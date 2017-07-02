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
