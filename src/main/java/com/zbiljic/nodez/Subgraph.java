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

import com.zbiljic.nodez.debug.DebugManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@code Subgraph} is a subset of a node graph, it takes a bunch of nodes as input and create a
 * node graph. It exposes one or more internal nodes of this graph via public member variables.
 * <p>
 * To implement a subclass, you should:
 * <ol>
 * <li>add public member variables of Node type for all the nodes you want to expose.</li>
 * <li>implement your own constructor, with one or more Node inputs and other input, create all node
 * wiring in the constructor.</li>
 * <li>at the end of constructor, call {@link #markExposedNodes()}.</li>
 * </ol>
 * <p>
 */
public abstract class Subgraph {

  private static final Logger log = LoggerFactory.getLogger(Subgraph.class);

  /**
   * ALWAYS REMEMBER TO CALL THIS AT THE END OF SUBCLASS CONSTRUCTOR.
   * <p>
   * Mark all exposed public Node member variables with current {@code Subgraph} instance using
   * reflection. This is for debugging and DOT graph generation purpose. If this method is not
   * called, there's no impact on the executions of nodes, it just affect the generated DOT graph.
   * <p>
   * This is only done when current debug level is &gt; 0, so we don't need to run this for every
   * production query.
   */
  protected void markExposedNodes() {
    if (DebugManager.isEnabled()) {
      List<Node> exposedNodes = getExposedNodes();
      if (exposedNodes.isEmpty()) {
        throw new RuntimeException("You don't have any public Node field in subgraph class "
          + this.getClass().getSimpleName());
      }
      for (Node node : exposedNodes) {
        node.setEnclosingSubgraph(this);
      }
    }
  }

  /**
   * Find all exposed public node member variables by reflection.
   */
  List<Node> getExposedNodes() {
    List<Node> nodes = new ArrayList<>();
    Field[] fields = this.getClass().getDeclaredFields();
    for (Field f : fields) {
      if (Modifier.isPublic(f.getModifiers()) && Node.class.isAssignableFrom(f.getType())) {
        try {
          nodes.add((Node) f.get(this));
        } catch (IllegalAccessException e) {
          log.warn("Cannot access field [{}] in subgraph {}", f.getName(), this.getClass().getSimpleName());
        }
      }
    }
    return nodes;
  }

  /**
   * Mark all nodes in the list with current {@code Subgraph}, a simpler non-reflection based
   * version.
   */
  protected void markExposedNodes(Node... nodes) {
    for (Node node : nodes) {
      node.setEnclosingSubgraph(this);
    }
  }

  public String toDotGraph() {
    return NodeDotGraphGenerator.createDot(this);
  }
}
