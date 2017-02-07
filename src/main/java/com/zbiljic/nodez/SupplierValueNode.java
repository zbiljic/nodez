package com.zbiljic.nodez;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A {@link ValueNode} based on supplier.
 *
 * @param <R> return type of the node
 * @see Node
 * @see ValueNode
 */
public class SupplierValueNode<R> extends ValueNode<R> {

  static <T> SupplierValueNode<T> create(String name, Supplier<T> valueSupplier) {
    return new SupplierValueNode<>(name, valueSupplier);
  }

  private final Supplier<R> supplier;
  private R suppliedValue;
  private volatile boolean alreadySupplied = false;

  /**
   * Create a {@link ValueNode} from a supplier, the supplier {@link Supplier#get()} will only will
   * called once.
   */
  protected SupplierValueNode(String name, Supplier<R> supplier) {
    super(name, null);
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
