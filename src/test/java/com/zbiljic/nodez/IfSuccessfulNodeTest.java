package com.zbiljic.nodez;

import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class IfSuccessfulNodeTest extends NodeTestBase {

  @Test
  public void testSuccess() throws Exception {
    Node<Boolean> node = IfSuccessfulNode.create(Node.value(100));
    assertTrue(resultFromNode(node));
  }

  @Test
  public void testNull() throws Exception {
    Node<Boolean> node = IfSuccessfulNode.create(Node.noValue());
    assertFalse(resultFromNode(node));
  }

  @Test
  public void testFailure() throws Exception {
    Node<Boolean> node = IfSuccessfulNode.create(new Node<String>() {
      @Override
      protected CompletableFuture<String> evaluate() throws Exception {
        throw new Exception("bad!");
      }
    });
    assertFalse(resultFromNode(node));
  }
}
