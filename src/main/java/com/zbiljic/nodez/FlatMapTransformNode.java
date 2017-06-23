package com.zbiljic.nodez;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Transforms a node using a function that returns a future of a new value.
 * <p>
 * See {@link Node#flatMap}.
 *
 * @param <SourceType> source node type
 * @param <R>          resulting node value type
 * @see Node
 * @see BaseTransformNode
 */
public class FlatMapTransformNode<SourceType, R> extends BaseTransformNode<SourceType, R> {

  /**
   * Create a new FlatMapTransformNode with decider key.
   * <p>
   * NOTE: Try not to use this directly, use Node.flatMap() instead.
   */
  static <SourceType, R> FlatMapTransformNode<SourceType, R> create(
    Node<SourceType> node,
    Function<SourceType, CompletableFuture<R>> transform,
    String name) {
    return new FlatMapTransformNode<>(node, transform, name);
  }

  private final Function<SourceType, CompletableFuture<R>> transform;

  protected FlatMapTransformNode(Node<SourceType> node,
                                 Function<SourceType, CompletableFuture<R>> transform,
                                 @Nullable String name) {
    super(node, name, false, true);
    this.transform = Preconditions.checkNotNull(transform);
  }

  @Override
  public String getResponseClassName() {
    return getLastTemplateType(this.transform.getClass());
  }

  @Override
  protected CompletableFuture<R> transform(SourceType source) {
    return transform.apply(source);
  }
}
