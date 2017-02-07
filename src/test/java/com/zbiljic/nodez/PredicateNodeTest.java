package com.zbiljic.nodez;

import com.zbiljic.nodez.utils.Throwables;

import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class PredicateNodeTest extends NodeTestBase {

  @Test
  public void testPredicate() throws Exception {
    Long value = 3L;
    Node<Boolean> resultNode = PredicateNode.create(Node.value(value), input -> input == 3L);
    assertTrue(resultFromNode(resultNode));
  }

  @Test
  public void testNullInput() throws Exception {
    Node<Boolean> resultNode = PredicateNode.create(Node.<Long>value(null), input -> input == 3L);

    CompletableFuture<Boolean> future = resultNode.apply();
    try {
      future.get();
      fail();
    } catch (Throwable e) {
      assertTrue(e.getCause() instanceof RuntimeException);
    }
  }

  @Test
  public void testPredicateException() throws Exception {
    Node<Boolean> resultNode = PredicateNode.create(Node.value(3L), input -> {
      throw new IllegalStateException("always throws");
    });

    CompletableFuture<Boolean> future = resultNode.apply();
    try {
      future.get();
      fail();
    } catch (Throwable e) {
      assertTrue(e.getCause() instanceof RuntimeException);
      assertTrue(Throwables.getRootCause(e) instanceof IllegalStateException);
    }
  }
}
