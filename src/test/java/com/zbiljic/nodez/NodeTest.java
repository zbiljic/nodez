package com.zbiljic.nodez;

import com.zbiljic.nodez.utils.CompletableFutures;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class NodeTest extends NodeTestBase {

  /**
   * A simple node that sums the value of three other nodes in a specific way
   */
  static class SumNode extends Node<Integer> {

    public enum D {
      FIRST,
      SECOND,
      THIRD,
      @OptionalDep FOURTH_OP
    }

    @Override
    protected CompletableFuture<Integer> evaluate() throws Exception {
      int sum = ((Integer) getRawDep(D.FIRST))
        + 10 * ((Integer) getRawDep(D.SECOND))
        + 100 * ((Integer) getRawDep(D.THIRD));
      Optional<Integer> fourth = getRawDep(D.FOURTH_OP);
      if (fourth.isPresent()) {
        sum += 1000 * fourth.get();
      }
      return CompletableFuture.completedFuture(sum);
    }
  }

  @Test
  public void testSumNodeSimple() throws Exception {
    Node<Integer> dep1 = Node.value(1);
    Node<Integer> dep2 = Node.value(2);
    Node<Integer> dep3 = Node.value(3);
    Node<Integer> dep4 = Node.value(4);
    Node<Integer> sumNode = Node.builder(SumNode.class)  // works without D.class given!
      .withDependencies(
        SumNode.D.FIRST, dep1,
        SumNode.D.SECOND, dep2,
        SumNode.D.THIRD, dep3,
        SumNode.D.FOURTH_OP, dep4)
      .build();
    CompletableFutures.awaitResult(sumNode.apply());

    assertEquals(sumNode.getAllDependencies().size(), 4);
    assertEquals((int) sumNode.getDep(SumNode.D.FIRST), 1);
    assertEquals((int) sumNode.getDep(SumNode.D.SECOND), 2);
    assertEquals((int) sumNode.getDep(SumNode.D.THIRD), 3);
    assertEquals((int) sumNode.getDep(SumNode.D.FOURTH_OP), 4);
    assertEquals((int) sumNode.emit(), 4321);
  }

  @Test
  public void testSumNodeSimpleFailure() throws Exception {
    Node<Integer> dep1 = Node.value(1);
    Node<Integer> dep2 = Node.value(2);
    Node<Integer> dep3 = Node.fail(new IOException());
    Node<Integer> dep4 = Node.value(4);
    Node<Integer> sumNode = Node.builder(SumNode.class)  // works without D.class given!
      .withDependencies(
        SumNode.D.FIRST, dep1,
        SumNode.D.SECOND, dep2,
        SumNode.D.THIRD, dep3,
        SumNode.D.FOURTH_OP, dep4)
      .build();
    assertNodeThrow(sumNode);
  }

  @Test
  public void testSumNodeSimpleWithPrewrapping() throws Exception {
    Node<Integer> dep1 = Node.value(1);
    Node<Integer> dep2 = Node.value(2);
    Node<Integer> dep3 = Node.value(3);
    Node<Optional<Integer>> dep4 = Node.optional(Node.value(4));
    Node<Integer> sumNode = Node.builder(SumNode.class)
      .withDependencies(
        SumNode.D.FIRST, dep1,
        SumNode.D.SECOND, dep2,
        SumNode.D.THIRD, dep3,
        SumNode.D.FOURTH_OP, dep4)
      .build();
    CompletableFutures.awaitResult(sumNode.apply());
    assertEquals((int) sumNode.emit(), 4321);
  }

  @Test
  public void testMissingDependency() throws Exception {
    // Missing required one is not OK
    try {
      Node.builder(SumNode.class)
        .withDependencies(
          SumNode.D.FIRST, Node.value(1),
          SumNode.D.FOURTH_OP, Node.value(4))
        .build();
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
      assertTrue(e.getMessage().contains("SECOND, THIRD"));
    }

    // Missing optional one is ok
    Node<Integer> resultNode = Node.builder(SumNode.class)
      .withDependencies(
        SumNode.D.FIRST, Node.value(1),
        SumNode.D.SECOND, Node.value(2),
        SumNode.D.THIRD, Node.value(3))
      .build();
    assertEquals((int) resultFromNode(resultNode), 321);
  }

  @Test
  public void testSumNodeWithOptional() throws Exception {
    Node<Integer> dep1 = Node.value(1);
    Node<Integer> dep2 = Node.value(2);
    Node<Integer> dep3 = Node.value(3);
    Node<Integer> dep4 = Node.noValue();
    Node<Integer> sumNode = Node.builder(SumNode.class)
      .dependsOn(SumNode.D.FIRST, dep1)
      .dependsOn(SumNode.D.SECOND, dep2)
      .dependsOn(SumNode.D.THIRD, dep3)
      .dependsOn(SumNode.D.FOURTH_OP, dep4)
      .build();
    CompletableFutures.awaitResult(sumNode.apply());

    assertEquals(sumNode.getAllDependencies().size(), 4);
    assertEquals((int) sumNode.getDep(SumNode.D.FIRST), 1);
    assertEquals((int) sumNode.getDep(SumNode.D.SECOND), 2);
    assertEquals((int) sumNode.getDep(SumNode.D.THIRD), 3);
    assertNull(sumNode.getDep(SumNode.D.FOURTH_OP));
    assertEquals((int) sumNode.emit(), 321);
  }

  @Test
  public void testSumNodeWithOptionalMissing() throws Exception {
    Node<Integer> dep1 = Node.value(1);
    Node<Integer> dep2 = Node.value(2);
    Node<Integer> dep3 = Node.value(3);
    Node<Integer> sumNode = Node.builder(SumNode.class)
      .dependsOn(SumNode.D.FIRST, dep1)
      .dependsOn(SumNode.D.SECOND, dep2)
      .dependsOn(SumNode.D.THIRD, dep3)
      .build();
    CompletableFutures.awaitResult(sumNode.apply());

    assertEquals(sumNode.getAllDependencies().size(), 4);
    assertEquals((int) sumNode.getDep(SumNode.D.FIRST), 1);
    assertEquals((int) sumNode.getDep(SumNode.D.SECOND), 2);
    assertEquals((int) sumNode.getDep(SumNode.D.THIRD), 3);
    assertNull(sumNode.getDep(SumNode.D.FOURTH_OP));
    assertEquals((int) sumNode.emit(), 321);
  }

  @Test
  public void testOptional() throws Exception {
    Node<Integer> node = new PlusOneNode(Node.optional(Node.noValue()));
    Integer result = CompletableFutures.awaitResult(node.apply());
    System.out.println("-- result = " + result);
  }

  static class PlusOneNode extends Node<Integer> {

    Node<Optional<Integer>> anotherNode;

    PlusOneNode(Node<Optional<Integer>> anotherNode) {
      super(anotherNode);
      this.anotherNode = anotherNode;
    }

    @Override
    protected CompletableFuture<Integer> evaluate() throws Exception {
      Optional<Integer> another = anotherNode.emit();
      if (another.isPresent()) {
        return CompletableFuture.completedFuture(another.get() + 1);
      } else {
        return CompletableFuture.completedFuture(0);
      }
    }
  }

  @Test
  public void testMap() throws Exception {
    final AtomicInteger functionRuns = new AtomicInteger(0);
    NamedFunction<Integer, String> func = new NamedFunction<Integer, String>("name") {
      @Override
      public String apply(Integer i) {
        functionRuns.incrementAndGet();
        Preconditions.checkNotNull(i, "Input to map function cannot be null");
        return "number " + i;
      }
    };
    Node<String> mappedNode = Node.value(100).map(func);
    assertEquals(resultFromNode(mappedNode), "number 100");
    assertEquals(functionRuns.get(), 1);

  }

  @Test
  public void testFlatMap() throws Exception {
    Node<Integer> first = Node.value(100);
    Node<String> second = first.flatMap(new NamedFunction<Integer, CompletableFuture<String>>("func") {
      @Override
      public CompletableFuture<String> apply(Integer i) {
        return CompletableFuture.completedFuture("future " + i);
      }
    });
    assertEquals(resultFromNode(second), "future 100");
  }

  @Test
  public void testWhen() throws Exception {
    Node<String> node = Node.value("condition was true");
    assertEquals(resultFromNode(node.when(Node.TRUE)), "condition was true");
    assertNull(resultFromNode(node.when(Node.FALSE)));
  }

  @Test
  public void testUnless() throws Exception {
    Node<String> node = Node.value("condition was false");
    assertEquals(resultFromNode(node.unless(Node.FALSE)), "condition was false");
    assertNull(resultFromNode(node.unless(Node.TRUE)));
  }

  @Test
  public void testWhenUnless() throws Exception {
    Node<String> node = Node.value("when was true and unless was false");
    assertEquals(resultFromNode(node.when(Node.TRUE).unless(Node.FALSE)), "when was true and unless was false");
    assertNull(resultFromNode(node.when(Node.TRUE).unless(Node.TRUE)));
    assertNull(resultFromNode(node.when(Node.FALSE).unless(Node.FALSE)));
    assertNull(resultFromNode(node.when(Node.FALSE).unless(Node.TRUE)));
  }

  @Test
  public void testOrElse() throws Exception {
    Node<String> node = Node.value("foo");
    Node<String> elseNode = Node.value("node was null");

    assertEquals(resultFromNode(node.orElse(elseNode)), "foo");

    assertEquals(resultFromNode(Node.<String>noValue().orElse(elseNode)), "node was null");
    assertEquals(
      resultFromNode(Node.<String>fail(new Exception()).orElse(elseNode)), "node was null");
    assertEquals(resultFromNode(node.when(Node.FALSE).orElse(elseNode)), "node was null");
  }

  @Test
  public void testValueNode() throws Exception {
    Node<String> strNode = Node.value("test");
    assertEquals("test", strNode.emit());

    Node<String> supplierNode = Node.valueFromSupplier("stringSupplierNode", new Supplier<String>() {
      private boolean called = false;

      @Override
      public String get() {
        if (called) {
          fail();
        }
        called = true;
        return "test";
      }
    });
    assertEquals(supplierNode.emit(), "test");
    assertEquals(supplierNode.emit(), "test");
    assertEquals(supplierNode.emit(), "test");
  }

  @Test
  public void testNodeMapCollect() throws Exception {
    Map<Integer, Node<Double>> intToNodeDoubleMap = new HashMap<>();
    intToNodeDoubleMap.put(1, Node.value(100.0));
    intToNodeDoubleMap.put(2, Node.value(200.0));
    intToNodeDoubleMap.put(3, Node.value(300.0));

    Node<Map<Integer, Double>> intToDoubleMapNode = Node.collect(intToNodeDoubleMap);
    Map<Integer, Double> integerDoubleMap = resultFromNode(intToDoubleMapNode);

    assertEquals(integerDoubleMap.get(1), 100.0, 0);
    assertEquals(integerDoubleMap.get(2), 200.0, 0);
    assertEquals(integerDoubleMap.get(3), 300.0, 0);
  }

  @Test
  public void testNodeListCollect() throws Exception {
    List<Node<Integer>> intNodeList = new ArrayList<>();
    intNodeList.add(Node.value(1));
    intNodeList.add(Node.value(2));
    intNodeList.add(Node.value(3));

    Node<List<Integer>> intListNode = Node.collect(intNodeList);
    List<Integer> intList = resultFromNode(intListNode);

    assertEquals(intList.get(0), 1, 0);
    assertEquals(intList.get(1), 2, 0);
    assertEquals(intList.get(2), 3, 0);

    // Test splitAndCollect()
    Node<List<String>> stringListNode = Node.splitAndCollect(
      intListNode,
      new NamedFunction<Integer, Node<String>>("toString") {
        @Override
        public Node<String> apply(Integer i) {
          return Node.value("str" + i);
        }
      });

    List<String> stringList = resultFromNode(stringListNode);

    assertEquals(stringList.get(0), "str1");
    assertEquals(stringList.get(1), "str2");
    assertEquals(stringList.get(2), "str3");
  }

  @Test
  public void testWaitOn() throws Exception {
    final List<Integer> store = new ArrayList<>();

    final NamedFunction<Integer, Integer> STORE_INTEGER =
      new NamedFunction<Integer, Integer>("store-integer") {
        @Override
        public Integer apply(Integer value) {
          store.add(value);
          return value;
        }
      };

    Node<Integer> mark1 = Node.value(1).map(STORE_INTEGER);
    Node<Integer> mark2 = Node.value(2).map(STORE_INTEGER);
    Node<Integer> mark3 = Node.value(3).map(STORE_INTEGER);
    Node<Integer> last = Node.value(999).map(STORE_INTEGER);

    Node<Integer> afterWait = last.waitOn(mark1, mark2, mark3);
    Integer value = resultFromNode(afterWait);

    assertEquals(value.intValue(), 999);
    assertEquals(store.size(), 4);
    assertEquals(store.get(3).intValue(), 999);  // the last node should insert its value last
  }

}
