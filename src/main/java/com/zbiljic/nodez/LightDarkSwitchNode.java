package com.zbiljic.nodez;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A node that runs one or two nodes, and selectively returns the result based on the condition.
 * Different from {@link PredicateSwitchNode}, this ALWAYS calls apply to both true and false nodes
 * (false nodes could be {@code null} though), and the condition only controls which result to
 * return.
 *
 * @see Node
 */
public class LightDarkSwitchNode<T> extends Node<T> {

  /**
   * Use a condition to dark read a response node, which is always applied.
   * <p>
   * If the condition is {@code true}, {@code null} will be returned, if {@code false}, the result
   * of {@code responseNode} will be returned.
   */
  public static <T> Node<T> create(Node<Boolean> shouldDarkReadNode, Node<T> responseNode) {
    return new LightDarkSwitchNode<>(shouldDarkReadNode, null, responseNode);
  }

  /**
   * Use a condition to choose between two nodes (always both applied).
   * <p>
   * If the condition is {@code true}, return the result of first node, otherwise the second.
   */
  public static <T> Node<T> create(Node<Boolean> shouldDarkReadNode, Node<T> darkNode, Node<T> lightNode) {
    return new LightDarkSwitchNode<>(shouldDarkReadNode, darkNode, lightNode);
  }

  private final Node<Boolean> shouldDarkReadNode;
  private final Node<T> darkNode;
  private final Node<T> lightNode;

  private LightDarkSwitchNode(Node<Boolean> shouldDarkReadNode,
                              @Nullable Node<T> darkNode,
                              @Nullable Node<T> lightNode) {
    super(false, allowNull(darkNode, lightNode), shouldDarkReadNode);  // similar to predicate switch node, only depends on the condition
    this.shouldDarkReadNode = shouldDarkReadNode;
    this.darkNode = darkNode;
    this.lightNode = lightNode;
  }

  private static boolean allowNull(final Node darkNode, final Node lightNode) {
    return (darkNode != null && darkNode.canEmitNull()) || (lightNode != null && lightNode.canEmitNull());
  }

  @Override
  public String getResponseClassName() {
    return darkNode != null
      ? darkNode.getResponseClassName()
      : (lightNode != null ? lightNode.getResponseClassName() : "");
  }

  @Override
  protected CompletableFuture<T> evaluate() throws Exception {
    boolean shouldDarkRead = shouldDarkReadNode.emit();
    CompletableFuture<T> darkResultFuture = darkNode != null
      ? darkNode.apply()
      : CompletableFuture.completedFuture(null);
    CompletableFuture<T> lightResultFuture = lightNode != null
      ? lightNode.apply()
      : CompletableFuture.completedFuture(null);
    return shouldDarkRead
      ? darkResultFuture
      : lightResultFuture;
  }

  @Override
  Map<String, Node> getInputsByName() {
    final Map<String, Node> inputs = new HashMap<>();
    inputs.put("condition", shouldDarkReadNode);
    if (darkNode != null) {
      inputs.put("TRUE", darkNode);
    }
    if (lightNode != null) {
      inputs.put("FALSE", lightNode);
    }
    return Collections.unmodifiableMap(inputs);
  }
}
