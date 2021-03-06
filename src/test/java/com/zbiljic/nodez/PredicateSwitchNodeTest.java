/*
 * Copyright 2017 Nemanja Zbiljić
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zbiljic.nodez;

import com.zbiljic.nodez.utils.CompletableFutures;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
    assertFalse(CompletableFutures.awaitOptionalResult(future).isPresent());
    Throwable e = CompletableFutures.getException(future);
    assertTrue(e instanceof RuntimeException);
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
    assertFalse(CompletableFutures.awaitOptionalResult(future).isPresent());
    Throwable e = CompletableFutures.getException(future);
    assertTrue(e instanceof IllegalStateException);
  }
}
