package com.zbiljic.nodez;

public class TransformNodeNullException extends Exception {

  public final Node transformNode;
  public final Node sourceNode;
  public final Object source;

  public TransformNodeNullException(Node transformNode,
                                    Node sourceNode,
                                    Object source) {
    this.transformNode = transformNode;
    this.sourceNode = sourceNode;
    this.source = source;
  }

  @Override
  public String toString() {
    return String.format("TransformNode [%s] response is null while transforming %s",
      transformNode.getName(),
      String.valueOf(source));
  }
}
