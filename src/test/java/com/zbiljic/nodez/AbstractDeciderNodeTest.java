package com.zbiljic.nodez;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AbstractDeciderNodeTest extends NodeTestBase {

  private class TestDeciderNode extends AbstractDeciderNode {

    private Boolean value;

    public TestDeciderNode(Boolean value) {
      super("TestDecider", "feature");
      this.value = value;
    }

    @Override
    protected boolean isAvailable(String featureName) {
      return value;
    }
  }

  @Test
  public void testNodeTrue() throws Exception {
    TestDeciderNode node = new TestDeciderNode(true);
    assertTrue(resultFromNode(node));
  }

  @Test
  public void testNodeFalse() throws Exception {
    TestDeciderNode node = new TestDeciderNode(false);
    assertFalse(resultFromNode(node));
  }
}
