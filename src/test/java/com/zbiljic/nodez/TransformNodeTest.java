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
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

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

    assertFalse(CompletableFutures.awaitOptionalResult(graph).isPresent());
    Throwable e = CompletableFutures.getException(graph);
    assertTrue(e instanceof RuntimeException);
    assertTrue(e.getCause() instanceof NullPointerException);
  }
}
