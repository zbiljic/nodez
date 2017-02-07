package com.zbiljic.nodez;

import java.util.concurrent.CompletableFuture;

/**
 * Base class for implementation of predicate nodes which make use of Decider.
 *
 * @see Node
 */
public abstract class AbstractDeciderNode extends Node<Boolean> {

  private final String featureName;

  protected AbstractDeciderNode(String name, String featureName) {
    super(name);
    this.featureName = Preconditions.checkNotNull(featureName);
  }

  @Override
  protected final CompletableFuture<Boolean> evaluate() {
    return isAvailable(featureName) ? Node.TRUE_FUTURE : Node.FALSE_FUTURE;
  }

  /**
   * Retrieves decider state for the given feature
   *
   * @param featureName name of decider.
   * @return decider result for given feature and recipient.
   */
  protected abstract boolean isAvailable(String featureName);
}
