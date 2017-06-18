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
 * 1. add public member variables of Node type for all the nodes you want to expose.
 * 2. implement your own constructor, with one or more Node inputs and other input, create all
 * node wiring in the constructor.
 * 3. at the end of constructor, call {@link #markExposedNodes()}.
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
   * This is only done when current debug level is > 0, so we don't need to run this for every
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
