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

import com.zbiljic.nodez.utils.CompletableFutures;
import com.zbiljic.nodez.utils.DeciderSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for nodes that transforms a node value, producing a new one.
 *
 * @param <SourceType> source node type
 * @param <R>          resulting node value type
 * @see Node
 */
public abstract class BaseTransformNode<SourceType, R> extends Node<R> {

  private static final Logger log = LoggerFactory.getLogger(BaseTransformNode.class);

  protected final Node<SourceType> node;

  protected BaseTransformNode(Node<SourceType> node,
                              @Nullable String name,
                              @Nullable DeciderSupplier deciderSupplier) {
    this(node, name, deciderSupplier, false, false);
  }

  protected BaseTransformNode(Node<SourceType> node,
                              @Nullable String name,
                              @Nullable DeciderSupplier deciderSupplier,
                              boolean optional,
                              boolean canEmitNull) {
    super(name != null ? name : String.format("Transform[%s]", node.getName()),
      optional,
      canEmitNull,
      node);
    this.node = node;
    setDeciderSupplier(deciderSupplier);
  }

  @Override
  public abstract String getResponseClassName();

  @Override
  protected CompletableFuture<R> evaluate() throws Exception {
    SourceType source = node.emit();

    CompletableFuture<R> response;

    try {
      response = transform(source);
    } catch (Exception e) {
      String msg = String.format(
        "TransformNode [%s] on [%s] threw an exception while transforming (%s): %s",
        getName(), node.getName(), e, String.valueOf(source));
      log.error(msg, e);
      return CompletableFutures.exceptionallyCompletedFuture(new RuntimeException(msg, e));
    }

    return response;
  }

  protected abstract CompletableFuture<R> transform(SourceType source);
}
