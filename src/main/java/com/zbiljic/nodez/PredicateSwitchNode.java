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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Predicate switch nodes make a choice based on a predicate and subsequently represents the output
 * of the chosen node.
 * <p>
 * If the predicate is true, then the node set with {@code ifTrue} is used, otherwise the node set
 * with {@code ifFalse} is used.
 * <p>
 * The chosen node is lazily executed, so work is not wasted on the unused node.
 *
 * @param <R> return type of the node
 * @see Node
 */
public class PredicateSwitchNode<R> extends Node<R> {

  private final Node<Boolean> predicateNode;
  private final Node<R> trueNode;
  private final Node<R> falseNode;

  public PredicateSwitchNode(Node<Boolean> predicateNode,
                             Node<R> trueNode,
                             Node<R> falseNode) {
    super(String.format("IF::%s(%s, %s)", predicateNode.getName(), trueNode.getName(), falseNode.getName()),
      false,
      allowNull(trueNode, falseNode),
      predicateNode);
    this.predicateNode = predicateNode;
    this.trueNode = Preconditions.checkNotNull(trueNode);
    this.falseNode = Preconditions.checkNotNull(falseNode);
  }

  private static boolean allowNull(final Node trueNode, final Node falseNode) {
    return trueNode.canEmitNull() || falseNode.canEmitNull();
  }

  @Override
  protected void logEnd() {
    super.logEnd();
    debugDetailed("predicate value from [%s] = %s", predicateNode.getName(), predicateNode.emit());
  }

  @Override
  public String getResponseClassName() {
    return this.trueNode.getResponseClassName();
  }

  @Override
  protected final CompletableFuture<R> evaluate() throws Exception {
    return predicateNode.emit()
      ? trueNode.apply()
      : falseNode.apply();
  }

  @Override
  Map<String, Node> getInputsByName() {
    final Map<String, Node> inputs = new HashMap<>();
    inputs.put("condition", predicateNode);
    inputs.put("TRUE", trueNode);
    inputs.put("FALSE", falseNode);
    return Collections.unmodifiableMap(inputs);
  }
}
