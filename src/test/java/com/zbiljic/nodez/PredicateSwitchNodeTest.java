package com.zbiljic.nodez;

import com.zbiljic.nodez.utils.Throwables;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class PredicateSwitchNodeTest extends NodeTestBase {

  private static final Integer TRUE_NODE_VALUE = 2;
  private static final Integer FALSE_NODE_VALUE = 3;

  private Node<Integer> trueNode;
  private Node<Integer> falseNode;

  @BeforeClass
  public void setUp() throws Exception {
    trueNode = new Node<Integer>() {
      @Override
      protected CompletableFuture<Integer> evaluate() throws Exception {
        return CompletableFuture.completedFuture(TRUE_NODE_VALUE);
      }
    };
    falseNode = new Node<Integer>() {
      @Override
      protected CompletableFuture<Integer> evaluate() throws Exception {
        return CompletableFuture.completedFuture(FALSE_NODE_VALUE);
      }
    };
  }

  @Test
  public void testTrueSwitch() throws Exception {
    Node<Integer> node = Node.ifThenElse(Node.value(true), trueNode, falseNode);
    assertEquals(resultFromNode(node), TRUE_NODE_VALUE);
  }

  @Test
  public void testFalseSwitch() throws Exception {
    Node<Integer> node = Node.ifThenElse(Node.value(false), trueNode, falseNode);
    assertEquals(resultFromNode(node), FALSE_NODE_VALUE);
  }

  @Test
  public void testOneWaySwitch() throws Exception {
    Node<Integer> node = Node.ifThen(Node.value(false), trueNode);
    assertEquals(resultFromNode(node), null);
  }

  @Test
  public void testNullPredicate() throws Exception {
    Node<Integer> node = Node.ifThenElse(Node.value(null), trueNode, falseNode);

    CompletableFuture<Integer> future = node.apply();
    try {
      future.get();
      fail();
    } catch (Throwable e) {
      assertTrue(e.getCause() instanceof RuntimeException);
    }
  }

  @Test
  public void testPredicateException() throws Exception {
    Node<Boolean> predicateNode = new Node<Boolean>() {
      @Override
      protected CompletableFuture<Boolean> evaluate() throws Exception {
        throw new IllegalStateException("always throws");
      }
    };
    Node<Integer> node = Node.ifThenElse(predicateNode, trueNode, falseNode);

    CompletableFuture<Integer> future = node.apply();
    try {
      future.get();
      fail();
    } catch (Throwable e) {
      assertTrue(Throwables.getRootCause(e) instanceof IllegalStateException);
    }
  }
}
