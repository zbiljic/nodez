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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * OrNode represents a disjunction of a list of boolean nodes.
 * <p>
 * The parallelism model chosen (eager or lazy) determines how the dependencies execute.
 *
 * @see Node
 * @see BooleanOperationNode
 */
public final class OrNode extends BooleanOperationNode {

  /**
   * Creates an eagerly evaluated disjunction where after all dependencies complete successfully,
   * the disjunction is evaluated left to right.
   */
  @SafeVarargs
  public static OrNode create(Node<Boolean>... disjunctionNodes) {
    return create("OR", disjunctionNodes);
  }

  @SafeVarargs
  public static OrNode create(String name, Node<Boolean>... disjunctionNodes) {
    Preconditions.checkState(disjunctionNodes.length >= 2);
    return new OrNode(name, false, Arrays.asList(disjunctionNodes));
  }

  /**
   * Creates a lazily evaluated disjunction where lazily implies that both the async and boolean
   * evaluation of node dependencies occurs sequentially, left to right.
   */
  @SafeVarargs
  public static OrNode createLazy(Node<Boolean>... disjunctionNodes) {
    return createLazy("OR-lazy", disjunctionNodes);
  }

  @SafeVarargs
  public static OrNode createLazy(String name, Node<Boolean>... disjunctionNodes) {
    Preconditions.checkState(disjunctionNodes.length >= 2);
    return new OrNode(true, Arrays.asList(disjunctionNodes));
  }

  private OrNode(boolean lazy, List<Node<Boolean>> disjunctionNodes) {
    super("OR", lazy, disjunctionNodes);
  }

  private OrNode(String name, boolean lazy, List<Node<Boolean>> disjunctionNodes) {
    super(name, lazy, disjunctionNodes);
  }

  /**
   * Evaluate the operands left to right executing according to the parallelism mode specified.
   * <p>
   * Call {@link #apply()} on each operand in sequence and evaluate the boolean condition.
   * <p>
   * If the evaluation should be continued, then {@link #apply()} is called on the next operand by
   * recursively calling evaluate on the rest of the operands. If the evaluation hasn't prematurely
   * terminated, then we arrive at the last operand, the response is the final state of the
   * evaluation.
   * <p>
   * NOTE: Calling {@link #apply()} on a {@link Node} is idempotent and will always give you back
   * the same {@link CompletableFuture}.
   * <p>
   * For lazy evaluation, each apply may kick off the task sequentially, causing serial execution of
   * the operands.
   * <p>
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
        ? TRUE_FUTURE
        : evaluate(operands.subList(1, operands.size())));
  }
}
