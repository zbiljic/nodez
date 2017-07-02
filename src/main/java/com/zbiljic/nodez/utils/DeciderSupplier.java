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
