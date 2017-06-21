package com.zbiljic.nodez;

import com.zbiljic.nodez.utils.CompletableFutures;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
    assertFalse(CompletableFutures.awaitOptionalResult(future).isPresent());
    Throwable e = CompletableFutures.getException(future);
    assertTrue(e instanceof RuntimeException);
  }

  @Test
  public void testPredicateException() throws Exception {
    Node<Boolean> resultNode = PredicateNode.create(Node.value(3L), input -> {
      throw new IllegalStateException("always throws");
    });

    CompletableFuture<Boolean> future = resultNode.apply();
    assertFalse(CompletableFutures.awaitOptionalResult(future).isPresent());
    Throwable e = CompletableFutures.getException(future);
    assertTrue(e instanceof RuntimeException);
    assertTrue(e.getCause() instanceof IllegalStateException);
  }
}
