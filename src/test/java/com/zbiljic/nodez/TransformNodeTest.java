package com.zbiljic.nodez;

import com.zbiljic.nodez.utils.Throwables;

import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class TransformNodeTest extends NodeTestBase {

  private static final Long TWO_VALUE = 2L;

  public static final Function<Long, Long> PLUS_FN =
    new Function<Long, Long>() {
      @Override
      public Long apply(Long term) {
        return term + TWO_VALUE;
      }
    };

  public static final Function<Long, Long> NULL_FN =
    new Function<Long, Long>() {
      @Override
      public Long apply(Long term) {
        return null;
      }
    };

  public static final Function<Long, Long> THROW_FN =
    new Function<Long, Long>() {
      @Override
      public Long apply(Long term) {
        throw new NullPointerException("here ya go!");
      }
    };

  @Test
  public void testTransformNode() throws Exception {
    Long term = 3L;
    Node<Long> termNode = Node.value(term);
    Node<Long> resultNode = TransformNode.create(termNode, PLUS_FN);
    Long result = resultFromNode(resultNode);
    assertEquals(result, Long.valueOf(term + TWO_VALUE));
  }

  @Test
  public void testTransformNodeNull() throws Exception {
    String name = "this is a transform node";
    Long term = 3L;
    Node<Long> termNode = Node.value(term);
    Node<Long> resultNode = TransformNode.create(termNode, NULL_FN, name);

    // null result no longer throws in transformer
    assertNull(resultFromNode(resultNode));
  }

  @Test
  public void testTransformNodeException() throws Exception {
    String name = "this is a transform node";
    Long term = 3L;
    Node<Long> termNode = Node.value(term);
    Node<Long> resultNode = TransformNode.create(termNode, THROW_FN, name);
    CompletableFuture<Long> graph = resultNode.apply();

    try {
      graph.get();
      fail();
    } catch (Throwable e) {
      assertTrue(e.getCause() instanceof RuntimeException);
      assertTrue(Throwables.getRootCause(e) instanceof NullPointerException);
    }
  }
}
