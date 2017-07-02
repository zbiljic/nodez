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
  protected CompletableFuture<Boolean> evaluate() {
    return evaluate(operands);
  }

  protected abstract CompletableFuture<Boolean> evaluate(final List<Node<Boolean>> operands);
}
