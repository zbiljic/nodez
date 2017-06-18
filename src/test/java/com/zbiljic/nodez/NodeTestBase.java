package com.zbiljic.nodez;

import com.zbiljic.nodez.debug.DebugManager;
import com.zbiljic.nodez.utils.CompletableFutures;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.assertTrue;

public class NodeTestBase {

  private static final Duration DEFAULT_WAIT_DURATION = Duration.ofSeconds(30);

  @BeforeMethod
  public void setUp() throws Exception {
    DebugManager.resetForTest();
  }

  /**
   * Get result from future
   */
  public static <T> T resultFromFuture(CompletableFuture<T> future) throws Exception {
    T result;
    try {
      result = CompletableFutures.within(future, DEFAULT_WAIT_DURATION).get();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    return result;
  }

  /**
   * Get result from a node, if the node fails or returns null, an exception will be thrown
   */
  public static <T> T resultFromNode(Node<T> node) throws Exception {
    return resultFromFuture(node.apply());
  }

  public static <T> void assertNodeThrow(Node<T> node) throws Exception {
    CompletableFuture<T> r = node.apply();
    CompletableFutures.within(r, DEFAULT_WAIT_DURATION);
    boolean isThrow = r.isCompletedExceptionally();
    assertTrue(isThrow, "expecting a throw but get: " + r);
  }
}
