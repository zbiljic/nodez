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

import com.zbiljic.nodez.debug.DebugLevel;
import com.zbiljic.nodez.debug.DebugManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SubgraphTest extends NodeTestBase {

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    DebugManager.resetForTest(DebugLevel.BASIC);
  }

  // Normal subgraph
  static class SimpleGraph extends Subgraph {

    public final Node<Long> longNode;

    SimpleGraph() {
      this.longNode = Node.value(10L);
      markExposedNodes();
    }
  }

  // Subgraph without public field, this will throw exception
  static class BadGraph extends Subgraph {

    private Node<Long> privateLongNode;  // this will fail

    BadGraph() {
      this.privateLongNode = Node.value(10L);
      markExposedNodes();
    }
  }

  @Test
  public void testSimple() throws Exception {
    SimpleGraph subgraph = new SimpleGraph();
    assertEquals(subgraph, subgraph.longNode.getEnclosingSubgraph());
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testNoPublicField() throws Exception {
    new BadGraph();
  }
}
