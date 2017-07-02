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

import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AndNodeTest extends NodeTestBase {

  @Test
  public void testLeftTrueNode() throws Exception {
    Node<Boolean> a = Node.TRUE;
    Node<Boolean> b = Node.FALSE;
    assertFalse(resultFromNode(AndNode.create(a, b)));
    assertFalse(resultFromNode(AndNode.createLazy(a, b)));
  }

  @Test
  public void testRightTrueNode() throws Exception {
    Node<Boolean> a = Node.FALSE;
    Node<Boolean> b = Node.TRUE;
    assertFalse(resultFromNode(AndNode.create(a, b)));
    assertFalse(resultFromNode(AndNode.createLazy(a, b)));
  }

  @Test
  public void testLeftAndRightTrueNode() throws Exception {
    Node<Boolean> a = Node.TRUE;
    Node<Boolean> b = Node.TRUE;
    assertTrue(resultFromNode(AndNode.create(a, b)));
    assertTrue(resultFromNode(AndNode.createLazy(a, b)));
  }

  @Test
  public void testLeftAndRightFalseNode() throws Exception {
    Node<Boolean> a = Node.FALSE;
    Node<Boolean> b = Node.FALSE;
    assertFalse(resultFromNode(AndNode.create(a, b)));
    assertFalse(resultFromNode(AndNode.createLazy(a, b)));
  }

  @Test
  public void testMultiNode() throws Exception {
    Node<Boolean> a = Node.TRUE;
    Node<Boolean> b = Node.TRUE;
    Node<Boolean> c = Node.FALSE;
    assertFalse(resultFromNode(AndNode.create(a, b, c)));
    assertFalse(resultFromNode(AndNode.createLazy(a, b, c)));
  }

  @Test
  public void testEagerEvaluate() throws Exception {
    final Boolean[] evaluatedA = new Boolean[1];
    final Boolean[] evaluatedB = new Boolean[1];
    evaluatedA[0] = new Boolean(false);
    evaluatedB[0] = new Boolean(false);

    Node<Boolean> a = new Node<Boolean>() {
      @Override
      protected CompletableFuture<Boolean> evaluate() throws Exception {
        evaluatedA[0] = true;
        return CompletableFuture.completedFuture(false);
      }
    };
    Node<Boolean> b = new Node<Boolean>() {
      @Override
      protected CompletableFuture<Boolean> evaluate() throws Exception {
        evaluatedB[0] = true;
        return CompletableFuture.completedFuture(false);
      }
    };
    Node<Boolean> node = AndNode.create(a, b);

    assertFalse(resultFromNode(node));
    assertTrue(evaluatedA[0]);
    assertTrue(evaluatedB[0]);
  }

  @Test
  public void testLazyEvaluate() throws Exception {
    final Boolean[] evaluatedA = new Boolean[1];
    final Boolean[] evaluatedB = new Boolean[1];
    evaluatedA[0] = new Boolean(false);
    evaluatedB[0] = new Boolean(false);

    Node<Boolean> a = new Node<Boolean>() {
      @Override
      protected CompletableFuture<Boolean> evaluate() throws Exception {
        evaluatedA[0] = true;
        return CompletableFuture.completedFuture(false);
      }
    };
    Node<Boolean> b = new Node<Boolean>() {
      @Override
      protected CompletableFuture<Boolean> evaluate() throws Exception {
        evaluatedB[0] = true;
        return CompletableFuture.completedFuture(true);
      }
    };
    Node<Boolean> node = AndNode.createLazy(a, b);

    assertFalse(resultFromNode(node));
    assertTrue(evaluatedA[0]);
    assertFalse(evaluatedB[0]);
  }
}
