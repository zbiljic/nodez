package com.zbiljic.nodez;

import com.zbiljic.nodez.utils.CompletableFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Applies a predicate on a given source node, producing a boolean output.
 *
 * @param <SourceType> source node type
 * @see Node
 */
public class PredicateNode<SourceType> extends Node<Boolean> {

  private static final Logger log = LoggerFactory.getLogger(PredicateNode.class);

  public static <SourceType> PredicateNode<SourceType> create(Node<SourceType> node,
                                                              Predicate<SourceType> predicate) {
    return create(node, predicate, null);
  }

  public static <SourceType> PredicateNode<SourceType> create(Node<SourceType> node,
                                                              Predicate<SourceType> predicate,
                                                              @Nullable String name) {
    return new PredicateNode<>(node, predicate, name);
  }

  private final Node<SourceType> sourceNode;
  private final Predicate<SourceType> predicate;

  public PredicateNode(Node<SourceType> sourceNode,
                       Predicate<SourceType> predicate) {
    this(sourceNode, predicate, null);
  }

  public PredicateNode(Node<SourceType> sourceNode,
                       Predicate<SourceType> predicate,
                       @Nullable String name) {
    super(name != null ? name : String.format("Predicate[%s]", sourceNode.getName()), sourceNode);
    this.sourceNode = sourceNode;
    this.predicate = Preconditions.checkNotNull(predicate);
  }

  @Override
  protected CompletableFuture<Boolean> evaluate() throws Exception {
    SourceType sourceValue = sourceNode.emit();
    try {
      return CompletableFuture.completedFuture(predicate.test(sourceValue));
    } catch (Exception e) {
      String msg = String.format("%s threw: sourceNode.emit() => %s", getName(), sourceValue);
      log.error(msg, e);
      return CompletableFutures.exceptionallyCompletedFuture(new RuntimeException(msg, e));
    }
  }
}
