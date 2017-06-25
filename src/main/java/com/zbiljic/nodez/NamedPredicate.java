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
