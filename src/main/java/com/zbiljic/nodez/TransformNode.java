package com.zbiljic.nodez;

import com.zbiljic.nodez.utils.CompletableFutures;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * {@code TransformNode} applies a function on a node value, producing a new value.
 * <p>
 * A typical transform is extracting a simple value from a more complex data structure.
 * <p>
 * This node can return null since the transform function can return null.
 *
 * @param <SourceType> Source node type.
 * @param <R>          Resulting node value type.
 * @see Node
 * @see BaseTransformNode
 */
public class TransformNode<SourceType, R> extends BaseTransformNode<SourceType, R> {

  /**
   * Create a new {@code TransformNode} with no name.
   * <p>
   * NOTE: try not to use this directly, use Node.map() instead.
   */
  public static <SourceType, R> TransformNode<SourceType, R> create(
    Node<SourceType> node,
    Function<SourceType, R> transform) {
    return new TransformNode<>(node, transform, null);
  }

  /**
   * Create a new {@code TransformNode} with name.
   * <p>
   * NOTE: try not to use this directly, use Node.map() instead.
   */
  public static <SourceType, R> TransformNode<SourceType, R> create(
    Node<SourceType> node,
    Function<SourceType, R> transform,
    String name) {
    return new TransformNode<>(node, transform, name);
  }

  private final Function<SourceType, R> transform;

  protected TransformNode(Node<SourceType> node,
                          Function<SourceType, R> transform,
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
    R response = transform.apply(source);

    if (response == null && !canEmitNull()) {
      TransformNodeNullException exception = new TransformNodeNullException(this, node, source);
      return CompletableFutures.exceptionallyCompletedFuture(exception);
    }

    return CompletableFuture.completedFuture(response);
  }
}
