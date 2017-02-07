package com.zbiljic.nodez;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class EqualsNodeTest extends NodeTestBase {

  @Test
  public void testEvaluateEquals() throws Exception {
    Object someGuy = new Object();
    Node<Object> firstNode = Node.value(someGuy);
    Node<Object> secondNode = Node.value(someGuy);

    Node<Boolean> equalsNode = EqualsNode.create(firstNode, secondNode);

    assertTrue(resultFromNode(equalsNode));
  }

  @Test
  public void testEvaluateNotEquals() throws Exception {
    Object someGuy = new Object();
    Object someOtherGuy = new Object();
    Node<Object> firstNode = Node.value(someGuy);
    Node<Object> secondNode = Node.value(someOtherGuy);

    Node<Boolean> equalsNode = EqualsNode.create(firstNode, secondNode);

    assertFalse(resultFromNode(equalsNode));
  }
}
