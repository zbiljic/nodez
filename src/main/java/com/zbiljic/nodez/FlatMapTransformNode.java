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

import com.zbiljic.nodez.utils.DeciderSupplier;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Transforms a node using a function that returns a future of a new value.
 * <p>
 * See {@link Node#flatMap}.
 *
 * @param <SourceType> source node type
 * @param <R>          resulting node value type
 * @see Node
 * @see BaseTransformNode
 */
public class FlatMapTransformNode<SourceType, R> extends BaseTransformNode<SourceType, R> {

  /**
   * Create a new FlatMapTransformNode with decider key.
   * <p>
   * NOTE: Try not to use this directly, use {@link Node#flatMap} instead.
   */
  static <SourceType, R> FlatMapTransformNode<SourceType, R> create(
    Node<SourceType> node,
    Function<SourceType, CompletableFuture<R>> transform,
    String name,
    DeciderSupplier deciderSupplier) {
    return new FlatMapTransformNode<>(node, transform, name, deciderSupplier);
  }

  private final Function<SourceType, CompletableFuture<R>> transform;

  protected FlatMapTransformNode(Node<SourceType> node,
                                 Function<SourceType, CompletableFuture<R>> transform,
                                 @Nullable String name,
                                 @Nullable DeciderSupplier deciderSupplier) {
    super(node, name, deciderSupplier, false, true);
    this.transform = Preconditions.checkNotNull(transform);
  }

  @Override
  public String getResponseClassName() {
    return getLastTemplateType(this.transform.getClass());
  }

  @Override
  protected CompletableFuture<R> transform(SourceType source) {
    return transform.apply(source);
  }
}
