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

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * A {@link Predicate} with a name.
 * <p>
 * Use this to create static constant predicates to be used in node predicate switches.
 *
 * @param <T> the type of the input to the predicate
 */
public abstract class NamedPredicate<T> implements Predicate<T> {

  public static <T> NamedPredicate<T> create(String name, Predicate<T> predicate) {
    return new NamedPredicate<T>(name) {
      @Override
      public boolean test(@Nullable T t) {
        return predicate.test(t);
      }
    };
  }

  private final String name;

  protected NamedPredicate(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Node<Boolean> apply(Node<T> inputNode) {
    return inputNode.predicate(this);
  }
}
