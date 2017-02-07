package com.zbiljic.nodez;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AndNode represents a conjunction of a list of boolean nodes.
 *
 * The parallelism model chosen (eager or lazy) determines how the dependencies execute.
 *
 * @see Node
 * @see BooleanOperationNode
 */
public final class AndNode extends BooleanOperationNode {

  /**
   * Creates an eagerly evaluated conjunction where after all dependencies complete successfully,
   * the conjunction is evaluated left to right.
   */
  public static AndNode create(Node<Boolean>... conjunctionNodes) {
    return create("AND", conjunctionNodes);
  }

  public static AndNode create(String name, Node<Boolean>... conjunctionNodes) {
    Preconditions.checkState(conjunctionNodes.length >= 2);
    return new AndNode(name, false, Arrays.asList(conjunctionNodes));
  }

  /**
   * Creates a lazily evaluated conjunction where lazily implies that both the async and boolean
   * evaluation of node dependencies occurs sequentially, left to right.
   */
  public static AndNode createLazy(Node<Boolean>... conjunctionNodes) {
    return createLazy("AND-lazy", conjunctionNodes);
  }

  public static AndNode createLazy(String name, Node<Boolean>... conjunctionNodes) {
    Preconditions.checkState(conjunctionNodes.length >= 2);
    return new AndNode(name, true, Arrays.asList(conjunctionNodes));
  }

  private AndNode(boolean lazy, List<Node<Boolean>> conjunctionNodes) {
    super("AND", lazy, conjunctionNodes);
  }

  private AndNode(String name, boolean lazy, List<Node<Boolean>> conjunctionNodes) {
    super(name, lazy, conjunctionNodes);
  }

  @Override
  protected CompletableFuture<Boolean> evaluate() {
    return evaluate(operands);
  }

  /**
   * Evaluate the operands left to right executing according to the parallelism mode specified.
   *
   * Call {@link #apply()} on each operand in sequence and evaluate the boolean condition.
   *
   * If the evaluation should be continued, then {@link #apply()} is called on the next operand by
   * recursively calling evaluate on the rest of the operands. If the evaluation hasn't prematurely
   * terminated, then we arrive at the last operand, the response is the final state of the
   * evaluation.
   *
   * NOTE: Calling {@link #apply()} on a {@link Node} is idempotent and will always give you back
   * the same {@link CompletableFuture}.
   *
   * For lazy evaluation, each apply may kick off the task sequentially, causing serial execution of
   * the operands.
   *
   * For eager evaluation, all of the nodes have been kicked-off already, so we are effectively
   * evaluating them left-to-right as they complete.
   */
  @Override
  protected CompletableFuture<Boolean> evaluate(final List<Node<Boolean>> operands) {
    if (operands.size() == 1) {
      return operands.get(0).apply();
    }

    return operands.get(0).apply()
      .thenCompose(value -> value
        ? evaluate(operands.subList(1, operands.size()))
        : FALSE_FUTURE);
  }
}
