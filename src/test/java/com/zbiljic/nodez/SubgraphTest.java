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
