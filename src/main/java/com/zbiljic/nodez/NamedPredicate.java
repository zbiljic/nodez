package com.zbiljic.nodez;

import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * T named {@link Predicate}
 *
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

  private NamedPredicate(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(@Nullable Object object) {
    return false;
  }

  public Node<Boolean> apply(Node<T> inputNode) {
    return inputNode.predicate(this);
  }
}
