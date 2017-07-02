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
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A {@link ValueNode} based on {@link Supplier}.
 *
 * @param <R> return type of the node
 * @see Node
 * @see ValueNode
 */
public class SupplierValueNode<R> extends ValueNode<R> {

  static <T> SupplierValueNode<T> create(Supplier<T> valueSupplier, String name) {
    return new SupplierValueNode<>(valueSupplier, name);
  }

  private final Supplier<R> supplier;
  private volatile R suppliedValue;
  private volatile boolean alreadySupplied = false;

  /**
   * Create a {@link ValueNode} from a supplier, the supplier {@link Supplier#get()} will only will
   * called once.
   */
  protected SupplierValueNode(Supplier<R> supplier, String name) {
    super(null, name);
    this.supplier = requireNonNull(supplier);
  }

  @Override
  public String getResponseClassName() {
    R value = getValue();
    return value == null
      ? ""
      : value.getClass().getSimpleName();
  }

  @Override
  protected CompletableFuture<R> evaluate() {
    return CompletableFuture.completedFuture(getValue());
  }

  @Override
  public R emit() {
    return getValue();
  }

  /**
   * Get the value inside this node, if it's from a provider, the provider will only be called
   * once.
   */
  private R getValue() {
    if (!alreadySupplied) {
      synchronized (this) {
        if (!alreadySupplied) {
          suppliedValue = supplier.get();
          alreadySupplied = true;
        }
      }
    }
    return suppliedValue;
  }
}
