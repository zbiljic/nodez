package com.zbiljic.nodez.utils;

import java.util.function.Supplier;

/**
 * A simple proxy for decider.
 *
 * @see Supplier
 */
public abstract class DeciderSupplier implements Supplier<Boolean> {

  /**
   * Always true {@link DeciderSupplier}.
   */
  public static final DeciderSupplier ALWAYS_TRUE = new DeciderSupplier("always_true") {
    @Override
    public Boolean get() {
      return true;
    }
  };

  /**
   * Always false {@link DeciderSupplier}.
   */
  public static final DeciderSupplier ALWAYS_FALSE = new DeciderSupplier("always_false") {
    @Override
    public Boolean get() {
      return false;
    }
  };

  private final String deciderKey;

  public DeciderSupplier(String deciderKey) {
    this.deciderKey = deciderKey;
  }

  public String getDeciderKey() {
    return deciderKey;
  }

  @Override
  public abstract Boolean get();

  public boolean isFeatureAvailable() {
    return get();
  }
}
