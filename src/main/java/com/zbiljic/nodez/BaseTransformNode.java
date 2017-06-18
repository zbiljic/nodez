package com.zbiljic.nodez;

import com.zbiljic.nodez.utils.CompletableFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for nodes that transforms a node value, producing a new one.
 *
 * @param <SourceType> Source node type.
 * @param <R>          Resulting node value type.
 * @see Node
 */
public abstract class BaseTransformNode<SourceType, R> extends Node<R> {

  private static final Logger log = LoggerFactory.getLogger(BaseTransformNode.class);

  protected final Node<SourceType> node;

  protected BaseTransformNode(Node<SourceType> node,
                              @Nullable String name) {
    this(node, name, false, false);
  }

  protected BaseTransformNode(Node<SourceType> node,
                              @Nullable String name,
                              boolean optional,
                              boolean canEmitNull) {
    super(name != null ? name : String.format("Transform[%s]", node.getName()),
      optional,
      canEmitNull,
      node);
    this.node = node;
  }

  @Override
  public abstract String getResponseClassName();

  @Override
  protected CompletableFuture<R> evaluate() throws Exception {
    SourceType source = node.emit();

    CompletableFuture<R> response;

    try {
      response = transform(source);
    } catch (Exception e) {
      String msg = String.format(
        "TransformNode [%s] on [%s] threw an exception while transforming (%s): %s",
        getName(), node.getName(), e, String.valueOf(source));
      log.error(msg, e);
      return CompletableFutures.exceptionallyCompletedFuture(new RuntimeException(msg, e));
    }

    return response;
  }

  protected abstract CompletableFuture<R> transform(SourceType source);
}
