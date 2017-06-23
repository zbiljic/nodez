package com.zbiljic.nodez;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Boolean operation node.
 *
 * @see Node
 */
public abstract class BooleanOperationNode extends Node<Boolean> {

  protected final List<Node<Boolean>> operands;
  protected final boolean lazy;

  protected BooleanOperationNode(String name, boolean lazy, List<Node<Boolean>> operandNodes) {
    super(name + "::(" + mergeName(operandNodes) + ")",
      lazy
        ? getFirstNodeAsList(operandNodes)
        : (List<Node>) (List) operandNodes);
    operands = Collections.unmodifiableList(operandNodes);
    this.lazy = lazy;
  }

  private static String mergeName(List<Node<Boolean>> operands) {
    return operands.stream()
      .map(node -> node == null ? "null" : node.getName())
      .collect(Collectors.joining(", "));
  }

  private static List<Node> getFirstNodeAsList(List<Node<Boolean>> nodes) {
    return Collections.singletonList(nodes.get(0));
  }

  public List<Node<Boolean>> getOperands() {
    return operands;
  }

  public boolean isLazy() {
    return lazy;
  }

  @Override
  Map<String, Node> getInputsByName() {
    final Map<String, Node> inputs = new HashMap<>();
    for (int i = 0; i < getOperands().size(); i++) {
      inputs.put("OP" + i, operands.get(i));
    }
    return Collections.unmodifiableMap(inputs);
  }

  @Override
  CompletableFuture<Void> futureFromDependencies() {
    if (!lazy) {
      // kick-off all the dependent nodes so they execute async.
      for (Node<Boolean> operand : operands) {
        operand.apply();
      }
    }

    // Note: calling apply on a Node is idempotent and will always give you back the same Future.
    return CompletableFuture.allOf(operands.get(0).apply());
  }

  @Override
  protected CompletableFuture<Boolean> evaluate() throws Exception {
    return evaluate(operands);
  }

  protected abstract CompletableFuture<Boolean> evaluate(final List<Node<Boolean>> operands);
}
