package com.zbiljic.nodez;

import com.zbiljic.nodez.debug.DebugManager;
import com.zbiljic.nodez.utils.CompletableFutures;
import com.zbiljic.nodez.utils.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A {@code Node} represents the {@link CompletableFuture} computation of a value as the result of
 * evaluating its completed required and optional dependencies.
 *
 * @param <R> return type of the node
 */
public abstract class Node<R> implements Function0<CompletableFuture<R>> {

  private static final Logger log = LoggerFactory.getLogger(Node.class);

  //
  // Constants
  //

  // A static dependency map to remember all optional dependencies of all nodes.
  // This starts with an empty map and gradually collect optionality information for different
  // enum classes used in the nodes.
  private static Map<Class<? extends Enum>, EnumSet> OPTIONAL_DEP_MAP = new ConcurrentHashMap<>();

  protected static final CompletableFuture<Boolean> TRUE_FUTURE = CompletableFuture.completedFuture(true);
  protected static final CompletableFuture<Boolean> FALSE_FUTURE = CompletableFuture.completedFuture(false);
  protected static final CompletableFuture<Void> VOID_FUTURE = CompletableFuture.completedFuture(null);
  protected static final CompletableFuture FUTURE_EMPTY = CompletableFuture.completedFuture(Optional.empty());

  public enum DefaultDependencyEnum {
    DEP0,
    DEP1,
    DEP2,
    DEP3,
    DEP4,
    DEP5,
    DEP6,
    DEP7,
    DEP8,
    DEP9,
    DEP10,
    DEP11,
    DEP12,
    DEP13,
    DEP14,
    DEP15
  }

  public static final Node<Boolean> TRUE = Node.value("true", true);
  public static final Node<Boolean> FALSE = Node.value("false", false);
  public static final Node NULL_NODE = Node.value("null", null);

  //
  // Instance variables
  //

  private final AtomicBoolean futureCreated = new AtomicBoolean();
  private final CompletableFuture<R> promise = new CompletableFuture<>();

  // Name for this node instance, this is mostly auto-generated with type information
  protected String name;

  // A string key for the node, could be used to distinguish node instances of the same type.
  protected String key;

  // this is a flag to deal with the java difficulty of differentiating between
  // Node<T> and Node<Optional<T>> via type introspection.
  private final boolean optional;

  // if true, this node's evaluate() method can return CompletableFuture.completedFuture(null)
  // without causing exception and its emit() can return null, otherwise an exception will be thrown.
  private final boolean canEmitNull;

  // Dependent nodes by their name. The names were created in the Builder (or the map passed into
  // the constructor.
  private Map<Enum, Node> dependentNodesByName;

  protected Node[] sinkNodes;

  // This will be set if this node is the exposed "output" node of a Subgraph, it's done by
  // Subgraph.markExposedNodes().
  private Subgraph enclosingSubgraph;

  private long startTimeMs;
  private long evaluateStartTimeMs;
  private long evaluateStopTimeMs;
  private long stopTimeMs;

  //
  // Constructors
  //

  protected Node() {
    this(null, false, false, Collections.emptyMap(), new Node[0]);
  }

  protected Node(boolean optional, boolean canEmitNull) {
    this(null, optional, canEmitNull, Collections.emptyMap(), new Node[0]);
  }

  protected Node(String name, boolean optional, boolean canEmitNull) {
    this(name, optional, canEmitNull, Collections.emptyMap(), new Node[0]);
  }

  protected Node(Node... nodes) {
    this(null, false, false, Collections.emptyMap(), nodes);
  }

  protected Node(boolean optional, boolean canEmitNull, Node... nodes) {
    this(null, optional, canEmitNull, Collections.emptyMap(), nodes);
  }

  protected Node(String name, Node... nodes) {
    this(name, false, false, Collections.emptyMap(), nodes);
  }

  protected Node(String name, boolean optional, boolean canEmitNull, Node... nodes) {
    this(name, optional, canEmitNull, Collections.emptyMap(), nodes);
  }

  protected Node(Collection<Node> dependentNodes) {
    this(null, false, false, Collections.emptyMap(), dependentNodes);
  }

  protected Node(boolean optional, boolean canEmitNull, Collection<Node> dependentNodes) {
    this(null, optional, canEmitNull, Collections.emptyMap(), dependentNodes);
  }

  protected Node(String name, Collection<Node> dependentNodes) {
    this(name, false, false, Collections.emptyMap(), dependentNodes);
  }

  protected Node(String name, boolean optional, boolean canEmitNull, Collection<Node> dependentNodes) {
    this(name, optional, canEmitNull, Collections.emptyMap(), dependentNodes);
  }

  protected Node(String name,
                 boolean optional,
                 boolean canEmitNull,
                 Map<Enum, Node> dependentNodesByName,
                 Collection<Node> sinkNodes) {
    this(name, optional, canEmitNull, dependentNodesByName, sinkNodes.toArray(new Node[sinkNodes.size()]));
  }

  protected Node(String name,
                 boolean optional,
                 boolean canEmitNull,
                 Collection<Node> dependentNodes,
                 Collection<Node> sinkNodes) {
    this(name, optional, canEmitNull, createNamedDependencies(dependentNodes), sinkNodes.toArray(new Node[sinkNodes.size()]));
  }

  /**
   * Constructor
   *
   * @param name                 The name of the node.
   * @param optional             Whether this node is optional. If true, this node should use {@link
   *                             Optional} as its return type.
   * @param canEmitNull          Whether this node can emit {@code null}.
   * @param dependentNodesByName A map from enum to dependent nodes, only after all these nodes are
   *                             ready will this node be running.
   * @param sinkNodes            A collection of sink nodes to run after this node finished {@link
   *                             #apply()}.
   */
  protected Node(@Nullable String name,
                 boolean optional,
                 boolean canEmitNull,
                 Map<Enum, Node> dependentNodesByName,
                 Node[] sinkNodes) {
    this.name = name != null && !name.isEmpty()
      ? name
      : this.getClass().getSimpleName();
    this.optional = optional;
    this.canEmitNull = canEmitNull;
    // dependent node map could be empty if the default constructor is called, this happens in
    // Builder.build(Class), where dependent node map is set later.
    this.dependentNodesByName = dependentNodesByName.isEmpty()
      ? Collections.emptyMap()
      : addOptionalDeps(dependentNodesByName);
    this.sinkNodes = sinkNodes.clone();
  }

  //
  // Accessors/Modifiers
  //

  public final String getName() {
    return key == null
      ? name
      : name + ":" + key;
  }

  @Nullable
  public final String getKey() {
    return key;
  }

  public final Node<R> withKey(String key) {
    this.key = key;
    return this;
  }

  public boolean isOptional() {
    return optional;
  }

  /**
   * Check if this node can emit null value.
   */
  protected final boolean canEmitNull() {
    return canEmitNull;
  }

  /**
   * Set dependencies after a Node is constructed using the default constructor, this should only be
   * called by builder. We add all unset optional dependencies and check if all required
   * dependencies are set, if not the Preconditions check will fail.
   */
  private void setAllDependencies(Map<Enum, Node> depsMap) {
    Preconditions.checkArgument(depsMap != null && !depsMap.isEmpty(),
      "You can set with empty dependency map");
    Map<Enum, Node> allDependencies = addOptionalDeps(depsMap);
    // check if all dependencies are provided
    EnumSet unsetEnums = EnumSet.complementOf(EnumSet.copyOf(allDependencies.keySet()));
    Preconditions.checkArgument(unsetEnums.isEmpty(),
      "Required dependencies not set for node [" + getName() + "]: " + unsetEnums);
    this.dependentNodesByName = allDependencies;
  }

  /**
   * Add optional nodes to the dependency map if they are not already there.
   */
  private Map<Enum, Node> addOptionalDeps(Map<Enum, Node> depMap) {
    Enum firstEnum = depMap.keySet().iterator().next();
    Set<Enum> optionalDeps = getOptionalDependenciesForClass(firstEnum.getClass());
    if (optionalDeps.isEmpty()) {
      return Collections.unmodifiableMap(depMap);
    } else {
      for (Enum e : optionalDeps) {
        if (!depMap.containsKey(e)) {
          Node absentNode = DebugManager.isEnabled()
            ? Node.optional(Node.noValue())
            : Node.empty();
          depMap.put(e, absentNode);
        }
      }
      return Collections.unmodifiableMap(depMap);
    }
  }

  /**
   * Get all of the node's dependencies.
   *
   * @return a collection of dependent nodes.
   */
  public final Collection<Node> getAllDependencies() {
    return dependentNodesByName.values();
  }

  /**
   * Get dependencies by name
   */
  final Map<Enum, Node> getDependenciesByName() {
    return dependentNodesByName;
  }

  /**
   * Get all inputs by name, for some nodes (like {@link PredicateSwitchNode}, {@link
   * BooleanOperationNode}), input is more than just dependencies.
   */
  Map<String, Node> getInputsByName() {
    final Map<String, Node> inputs = new HashMap<>();
    for (Map.Entry<Enum, Node> entry : getDependenciesByName().entrySet()) {
      inputs.put(entry.getKey().name(), entry.getValue());
    }
    return Collections.unmodifiableMap(inputs);
  }

  /**
   * Get dependency node itself.
   */
  protected <T> Node<T> getNodeDep(Enum name) {
    Preconditions.checkArgument(name != null && dependentNodesByName.containsKey(name),
      "Cannot find node dependency for %s", name);
    return dependentNodesByName.get(name);
  }

  /**
   * Get a dependent node's emitted value by its name. You can only get named dependency's value. If
   * the node is optional, it will return {@link Optional} type.
   */
  protected <T> T getRawDep(Enum name) {
    Preconditions.checkArgument(name != null && dependentNodesByName.containsKey(name),
      "Cannot find raw node dependency value for %s", name);
    return (T) dependentNodesByName.get(name).emit();
  }

  /**
   * Get a dependent node's emitted value by its name.
   */
  @Nullable
  protected <T> T getDep(Enum name) {
    Preconditions.checkArgument(name != null && dependentNodesByName.containsKey(name),
      "Cannot find node dependency value for %s", name);
    return (T) this.<T>getDep(dependentNodesByName.get(name));
  }

  /**
   * Get a dependent node's emitted value by its name.
   *
   * @param name         Enum name of dependency
   * @param defaultValue default value to use if dependency is missing, i.e. emitted value is null
   * @param <T>          return type of dependency
   */
  protected <T> T getDep(Enum name, T defaultValue) {
    Preconditions.checkNotNull(defaultValue,
      "Cannot have default value for a dependency as null");
    T value = getDep(name);
    return value == null
      ? defaultValue
      : value;
  }

  /**
   * Get dependency value by their node, if the node is optional, it will strip the {@link Optional}
   * class wrapping and returns null if the value is absent.
   */
  protected <T> T getDep(Node<T> depNode) {
    return depNode.isOptional()
      ? ((Optional<T>) depNode.emit()).orElse(null)
      : depNode.emit();
  }

  /**
   * Return a set of enum fields that define which named dependencies are optional.
   * <p>
   * This is only used to generate DOT graph.
   */
  public final Set<? extends Enum> getOptionalDependencies() {
    if (dependentNodesByName.isEmpty()) {
      return Collections.emptySet();
    }
    Enum firstEnum = dependentNodesByName.keySet().iterator().next();
    return getOptionalDependenciesForClass(firstEnum.getClass());
  }

  private Node<R> setSinkNodes(Node... sinkNodes) {
    Preconditions.checkArgument(!futureCreated.get(), "Node [%s] has been applied.", getName());
    Preconditions.checkNotNull(sinkNodes);
    this.sinkNodes = sinkNodes;
    return this;
  }

  public final Node<R> setSinkNodes(List<Node> sinkNodes) {
    setSinkNodes(sinkNodes.toArray(new Node[sinkNodes.size()]));
    return this;
  }

  public final Node<R> addSinkNodes(Node... sinkNodes) {
    return setSinkNodes(concat(this.sinkNodes, sinkNodes));
  }

  public final Node<R> addSinkNodes(List<Node> sinkNodes) {
    return addSinkNodes(sinkNodes.toArray(new Node[sinkNodes.size()]));
  }

  public Subgraph getEnclosingSubgraph() {
    return enclosingSubgraph;
  }

  public void setEnclosingSubgraph(Subgraph enclosingSubgraph) {
    this.enclosingSubgraph = enclosingSubgraph;
  }

  //
  // Methods
  //

  /**
   * Get class name for the response type using reflection.
   */
  public String getResponseClassName() {
    return getLastTemplateType(this.getClass());
  }

  /**
   * Create dot graph for a node, this provides a way to visualize node dependencies and helps
   * debugging.
   *
   * @return A string in DOT syntax, which can be rendered using Graphviz or other software.
   */
  public String toDotGraph() {
    return NodeDotGraphGenerator.createDot(this);
  }

  //@formatter:off
  /**
   * Convert a node to a future "safely". This will mask all possible failures in the wrapped node,
   * including:
   * - non-NullableNode returning a null value
   * - exception thrown inside node
   * - any of node's further dependency's failure
   * all of them will turn into {@code CompletableFuture.completedFuture(null)}.
   *
   * If you do not want to mask failures below, you should directly use {@link #apply()}. If current
   * node is nullable (inherit from {@link NullableNode}, or {@link #canEmitNull()} returns true),
   * it can return {@code CompletableFuture.completedFuture(null)} properly; if current node is not
   * nullable, or there is an exception thrown this will become a CompletableFuture exception.
   */
  //@formatter:on
  public final CompletableFuture<R> toCompletableFutureSafe() {
    return Node.optional(this)
      .apply()
      .thenApply(new Function<Optional<R>, R>() {
        @Override
        public R apply(Optional<R> response) {
          return response.orElse(null);
        }
      });
  }

  /**
   * Creates the future used to determine when the node's dependencies are able to be {@link
   * #evaluate()}'ed.
   * <p>
   * The default implementation is to join all dependencies so that {@link #evaluate()} is only
   * called when all dependencies are complete and successful.
   */
  CompletableFuture<Void> futureFromDependencies() {
    if (dependentNodesByName.size() == 0) {
      return VOID_FUTURE;
    }
    final CompletableFuture[] dependencies = new CompletableFuture[dependentNodesByName.size()];
    int i = 0;
    for (Node node : dependentNodesByName.values()) {
      dependencies[i] = node.apply();
      i++;
    }
    return CompletableFuture.allOf(dependencies);
  }

  /**
   * Calls {@link #apply()} on all sink nodes.
   */
  private void applySinkNodes() {
    for (int i = 0; i < sinkNodes.length; i++) {
      if (sinkNodes[i] != null) {
        sinkNodes[i].apply();
      }
    }
  }

  /**
   * Wait on a bunch of nodes before returning current node's result. This is convenient in creating
   * some temporary dependencies.
   */
  public Node<R> waitOn(Node... nodesToWait) {
    Preconditions.checkArgument(nodesToWait.length <= DefaultDependencyEnum.values().length,
      "Too many nodes to wait on");
    final Node<R> outerNode = this;
    return new NullableNode<R>(this.getName() + "_waited", nodesToWait) {
      @Override
      protected CompletableFuture<R> evaluate() throws Exception {
        return outerNode.apply();
      }
    };
  }

  @Override
  public CompletableFuture<R> apply() {
    if (!futureCreated.compareAndSet(false, true)) {
      return promise;
    }

    // Capture when the node started waiting on dependencies
    startTimeMs = System.currentTimeMillis();

    futureFromDependencies()
      .thenComposeAsync(unused -> {

        CompletableFuture<R> result;

        if (!isOptional()) {
          logStart();
        }
        try {
          evaluateStartTimeMs = System.currentTimeMillis();

          result = evaluate();
          if (result == null) {
            result = CompletableFutures.exceptionallyCompletedFuture(
              new RuntimeException("evaluate() returned null CompletableFuture object!"));
          }
        } catch (Exception e) {
          String message = "evaluate threw an exception";
          debugDetailed("%s\n%s", message, Throwables.getStackTraceAsString(e));
          log.error(message, e);
          result = CompletableFutures.exceptionallyCompletedFuture(e);
        }

        evaluateStopTimeMs = System.currentTimeMillis();
        return result;
      })
      .whenComplete((value, throwable) -> {
        stopTimeMs = System.currentTimeMillis();
        if (!isOptional()) {
          if (throwable == null) {
            logResponse(value);
            logEnd();
          } else {
            logError(throwable);
          }
        }
        if (throwable == null) {
          // completed successfully
          if (value == null && !canEmitNull) {
            promise.completeExceptionally(
              new RuntimeException("evaluate() returned CompletableFuture.value(null) but the step is not marked as Nullable."));
          } else {
            promise.complete(value);
          }
        } else {
          // completed exceptionally
          if (isOptional()) {
            promise.complete((R) Optional.empty());
          } else {
            promise.completeExceptionally(throwable);
          }
        }
      });

    applySinkNodes();

    return promise;
  }

  /**
   * Callback that fires when all of the required dependencies succeeded and have non-null values.
   *
   * @return a future of the computed node's value; may be a future of null if it failed.
   */
  protected abstract CompletableFuture<R> evaluate() throws Exception;

  /**
   * Gets the {@code Node} value.
   * <p>
   * The node will only emit a non-null value if it completed successfully.
   * <p>
   * Since a node's required dependencies must succeed for {@link #evaluate()} to be called, a
   * node's implementation should only be able to call {@code #emit()} when it's guaranteed to
   * return successfully.
   */
  public R emit() {
    if (!promise.isCompletedExceptionally()) {
      try {
        return promise.get();
      } catch (Exception e) {
        log.error("Exception during emit()", e);
        throw new RuntimeException("Could not read promise", e);
      }
    }

    // It's logically impossible to get here if:
    //    apply() was called on the terminating graph node
    //    AND the promise's node was added as a dependency.
    // So it's possible to get here if you just create a node and then call emit() on it w/o
    // calling apply() on the terminating graph node.
    // Assuming the graph was used properly, then it's possible to get here if the node wasn't added
    //
    // Report back to the user which state the node was in, but also remind them to add the node
    // as a dependency.

    if (promise.isCompletedExceptionally()) {
      throw new IllegalStateException(
        String.format("NODE[%s]: Attempting to call emit() on failed required node.  "
          + "Did you forget to add this node as a required dependency?", getName()));
    }

    throw new IllegalStateException(
      String.format("NODE[%s]: Attempting to call emit() on an incomplete required node.  "
        + "Did you forget to add this node as a required dependency?", getName()));
  }

  //
  // Debug
  //

  private volatile String debugPrefix = null;

  private String getDebugPrefix() {
    if (debugPrefix == null) {
      debugPrefix = "NODE [" + getName() + "]: ";
    }
    return DebugManager.isDetailedEnabled()
      ? String.format("[%04d] %s", System.currentTimeMillis() % 10000, debugPrefix)
      : debugPrefix;
  }

  public void debugBasic(final String message, Object... args) {
    if (DebugManager.isBasicEnabled()) {
      DebugManager.basic(getDebugPrefix() + message, args);
    }
  }

  public void debugDetailed(final String message, Object... args) {
    if (DebugManager.isDetailedEnabled()) {
      DebugManager.detailed(getDebugPrefix() + message, args);
    }
  }

  public void debugVerbose(final String message, Object... args) {
    if (DebugManager.isVerboseEnabled()) {
      DebugManager.verbose(getDebugPrefix() + message, args);
    }
  }

  protected void logStart() {
    debugDetailed("Start");
  }

  protected void logEnd() {
    debugDetailed("End (%d/%d ms)",
      stopTimeMs - startTimeMs, evaluateStopTimeMs - evaluateStartTimeMs);
  }

  protected void logError(Throwable t) {
    debugDetailed("Failed (%d/%d ms): %s",
      stopTimeMs - startTimeMs, evaluateStopTimeMs - evaluateStartTimeMs, t.getMessage());
    debugVerbose("Detailed failure: %s", Throwables.getStackTraceAsString(t));
  }

  /**
   * Log response string, by default it doesn't print much information.
   */
  protected void logResponse(@Nullable R response) {
    String str = response == null ? null : printResponse(response);
    if (str != null && !isOptional()) {
      debugDetailed("response: %s", str);
    }
  }

  /**
   * Print response into a string for logging/debugging purpose.
   */
  @Nullable
  protected String printResponse(R response) {
    if (DebugManager.isVerboseEnabled()) {
      return String.valueOf(response);
    }
    return null;
  }

  //
  // Static
  //

  /**
   * Adds all the elements of the given arrays into a new array.
   * <p>
   * The new array contains all of the element of {@code array1} followed by all of the elements
   * {@code array2}. When an array is returned, it is always a new array.
   *
   * @param array1 the first array whose elements are added to the new array, may not be {@code
   *               null}
   * @param array2 the second array whose elements are added to the new array, may not be {@code
   *               null}
   * @param <T>    the component type of the array
   * @return The new array.
   */
  private static <T> T[] concat(final T[] array1, final T[] array2) {
    final T[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  protected static String getLastTemplateType(Class clazz) {
    Type t = clazz.getGenericSuperclass();
    if (t instanceof ParameterizedType) {
      Type[] argTypes = ((ParameterizedType) t).getActualTypeArguments();
      if (argTypes.length > 0) {
        return argTypes[argTypes.length - 1].toString();
      }
    }
    return "";
  }

  private static Map<Enum, Node> createNamedDependencies(Collection<Node> nodes) {
    int maxSize = DefaultDependencyEnum.values().length;
    Preconditions.checkArgument(nodes.size() <= maxSize,
      "You can't have more than %s dependencies for a node.", maxSize);
    Map<Enum, Node> map = new EnumMap(DefaultDependencyEnum.class);
    DefaultDependencyEnum[] values = DefaultDependencyEnum.values();
    int index = 0;
    for (Node node : nodes) {
      map.put(values[index++], node);
    }
    return map;
  }

  /**
   * A helper function to get optional dependencies for an enum class and cache the result in a map.
   * If the cached results is available, use it.
   */
  private static Set getOptionalDependenciesForClass(Class<? extends Enum> enumClass) {
    EnumSet optionalEnumSet = OPTIONAL_DEP_MAP.get(enumClass);
    if (optionalEnumSet == null) {
      optionalEnumSet = EnumSet.noneOf(enumClass);
      try {
        for (Object item : enumClass.getEnumConstants()) {
          Annotation[] annotations = enumClass.getField(item.toString()).getAnnotations();
          if (annotations != null) {
            for (Annotation annotation : annotations) {
              if (annotation instanceof OptionalDep) {
                optionalEnumSet.add(item);
              }
            }
          }
        }
        OPTIONAL_DEP_MAP.put(enumClass, EnumSet.copyOf(optionalEnumSet));
      } catch (Exception e) {
        log.error("Cannot get fields for enum class {}", enumClass, e);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
      }
    }
    return EnumSet.copyOf(optionalEnumSet);
  }

  //
  // Builder
  //

  /**
   * A convenient method to create a builder for a given target node class.
   * Enum class for the dependencies will be decided when the first dependency is added.
   */
  public static <T> Builder<T> builder(Class<? extends Node<T>> nodeClass) {
    return new Builder<>(nodeClass, null);
  }

  /**
   * A simpler helper merging {@link Node#builder(Node)} and {@link Builder#withDependencies(Object...)#build(Class,
   * Object...)} into one call.
   */
  public static <T> Node<T> build(Class<? extends Node<T>> nodeClass, Object... dependencies) {
    return new Builder<>(nodeClass, null).withDependencies(dependencies).build();
  }

  /**
   * Provides a way of creating a builder from an existing instance of a {@link Node}.
   * <p>
   * This method is useful for nodes that require arguments in the constructor or for using mocks.
   */
  public static <T> Builder<T> builder(Node<T> nodeInstance) {
    return new Builder<>(nodeInstance, null);
  }

  /**
   * A general builder to build a {@link Node} using named dependencies.
   * <p>
   * If the {@code node} is created from a class, it will first call the default constructor of the
   * given class (so make sure it has one, since {@link Node} already has one, not implementing any
   * constructor gives you one by default).
   * <p>
   * The builder assigns dependencies to the {@code node} instance using enum names. For any
   * dependencies marked as {@link OptionalDep} in the enum class, if they don't already exist in
   * the collected dependency map, they will be added as {@link Node#empty()}.
   *
   * @param <T> The return type of the nodes this Builder builds.
   */
  public static class Builder<T> {

    private static final Node[] EMPTY_NODE_ARRAY = new Node[0];

    protected final Node<T> nodeInstance;
    protected String nodeKey;
    protected Map<Enum, Node> dependentNodesByName;
    protected Node[] sinkNodes = EMPTY_NODE_ARRAY;

    public Builder(Class<? extends Node<T>> nodeClass) {
      this(nodeClass, null);
    }

    public Builder(Class<? extends Node<T>> nodeClass, @Nullable Class<? extends Enum> enumClass) {
      this(createInstance(nodeClass), enumClass);
    }

    public Builder(Node<T> nodeInstance, @Nullable Class<? extends Enum> enumClass) {
      this.nodeInstance = nodeInstance;
      if (enumClass != null) {
        initDependencyMap(enumClass);
      }
    }

    private static <T> Node<T> createInstance(Class<? extends Node<T>> nodeClass) {
      try {
        return nodeClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(
          String.format(
            "Cannot create instance for Node [%s], make sure it has a default constructor",
            nodeClass.getSimpleName()), e);
      }
    }

    /** Create dependency map by enum, this allows the lazy creation of this class. */
    private void initDependencyMap(Class<? extends Enum> enumClass) {
      if (dependentNodesByName == null) {
        dependentNodesByName = new EnumMap(enumClass);
      }
    }

    private void initDependencyMap(Enum name) {
      if (dependentNodesByName == null) {
        initDependencyMap(name.getClass());
      }
    }

    /**
     * Check if a dependency is optional or not by name.
     */
    private boolean isDependencyOptional(Enum name) {
      return getOptionalDependenciesForClass(name.getClass()).contains(name);
    }

    public Builder<T> withNodeKey(String nodeKey) {
      this.nodeKey = nodeKey;
      return this;
    }

    public Builder<T> withSinkNodes(Node... sinkNodes) {
      this.sinkNodes = sinkNodes.clone();
      return this;
    }

    public Builder<T> withSinkNodes(List<Node> sinkNodes) {
      return withSinkNodes(sinkNodes.toArray(new Node[sinkNodes.size()]));
    }

    /**
     * Add a named dependency.
     * <p>
     * If the dependency is marked as optional, the node will be wrapped so when it fails to emit a
     * valid result, current node's execution won't be affected.
     */
    public Builder<T> dependsOn(Enum name, Node node) {
      initDependencyMap(name);
      return dependsOnInternal(name, node);
    }

    protected Builder<T> dependsOnInternal(Enum name, Node node) {
      // Wrap this node if its dependency state is optional and the node is not already wrapped.
      // The node.isOptional() is mostly for backward compatibility as we may sometimes pass in
      // an optional-wrapped node using the new-style builder.
      //TODO: remove the isOptional() check after we clean up all such cases.
      Node dependency = isDependencyOptional(name) && !node.isOptional()
        ? Node.optional(node)
        : node;
      Preconditions.checkArgument(dependentNodesByName.put(name, dependency) == null,
        "You have already added a dependent node named: %s", name);
      return this;
    }

    /**
     * A convenient wrapper to add all dependencies.
     *
     * @param deps All dependencies with enum key and node object alternating. There must be even
     *             number of items in this list.
     * @return A node builder with given dependencies.
     */
    public Builder<T> withDependencies(Object... deps) {
      Preconditions.checkArgument(deps.length > 0 && deps.length % 2 == 0,
        "There must be even number of arguments in Node.Builder.buildWithDependencies()");
      try {
        initDependencyMap((Enum) deps[0]);
        for (int i = 0; i < deps.length; i += 2) {
          Enum key = (Enum) deps[i];
          Node node = (Node) deps[i + 1];
          dependsOnInternal(key, node);
        }
      } catch (ClassCastException e) {
        log.error("Casting exception while creating node", e);
        throw new RuntimeException(e);
      }
      return this;
    }

    /**
     * Get all dependent nodes in a map. If user uses Builder or subclasses it themselves, the enum
     * class would be provided by the user; if they use AnonymousBuilder, or simply pass in a
     * collection of unnamed dependencies, the key will be {@link DefaultDependencyEnum}.
     */
    protected Map<Enum, Node> getDependencyMap() {
      return dependentNodesByName;
    }

    public Node<T> build() {
      nodeInstance.setAllDependencies(getDependencyMap());
      nodeInstance.withKey(nodeKey);
      nodeInstance.setSinkNodes(sinkNodes);
      return nodeInstance;
    }
  }

  //
  // Transformations
  //

  /**
   * Map the output of current node to a new type T.
   */
  public <T> Node<T> map(NamedFunction<R, T> function) {
    return TransformNode.create(this, function, function.getName());
  }

  public <T> Node<T> map(String name, Function<R, T> function) {
    return map(NamedFunction.create(name, function));
  }

  /**
   * Maps the value of current node to a new type T by applying the provided function, but only when
   * the value is present. For {@code null} values, the function is not even run and the transformed
   * value is {@code null}. Exceptions will be convert to {@code null}.
   * <p>
   * This means the function doesn't have to handle nullable inputs when run on nullable nodes.
   */
  public <T> Node<T> mapOnSuccess(NamedFunction<R, T> function) {
    return ifSuccessThen(this, map(function));
  }

  public <T> Node<T> mapOnSuccess(String name, Function<R, T> function) {
    return ifSuccessThen(this, map(name, function));
  }

  public <T> Node<T> flatMap(NamedFunction<R, CompletableFuture<T>> function) {
    return FlatMapTransformNode.create(this, function, function.getName());
  }

  public <T> Node<T> flatMap(String name, Function<R, CompletableFuture<T>> function) {
    return flatMap(NamedFunction.create(name, function));
  }

  /**
   * Collect results from a map of nodes into a node of the map.
   */
  public static <A, B> Node<Map<A, B>> collect(final Map<A, Node<B>> nodeMap) {
    Preconditions.checkNotNull(nodeMap);
    final Map<A, CompletableFuture<B>> futures = new HashMap<>();
    for (Map.Entry<A, Node<B>> mapEntry : nodeMap.entrySet()) {
      futures.put(mapEntry.getKey(), mapEntry.getValue().apply());
    }
    return Node.wrapCompletableFuture(CompletableFutures.collect(futures));
  }

  /**
   * Collect results from a list of nodes into a node of list.
   */
  public static <T> Node<List<T>> collect(List<Node<T>> nodeList) {
    Preconditions.checkNotNull(nodeList);
    List<CompletableFuture<T>> futures = new ArrayList<>();
    for (Node<T> node : nodeList) {
      futures.add(node.apply());
    }
    return Node.wrapCompletableFuture(CompletableFutures.collect(futures));
  }

  /**
   * Splits and transforms a Node of a list of elements A and then collects as a Node of lists of
   * element B.
   */
  public static <A, B> Node<List<B>> splitAndCollect(Node<List<A>> list,
                                                     NamedFunction<A, Node<B>> transformFunction) {
    return list.flatMap(
      NamedFunction.create("splitAndCollectList", items -> {
        List<Node<B>> newList = new ArrayList<>();
        for (A item : items) {
          newList.add(transformFunction.apply(item));
        }
        return Node.collect(newList).apply();
      }))
      .whenSuccess(list);
  }

  /**
   * Split and collect with Java {@link Function}.
   */
  public static <A, B> Node<List<B>> splitAndCollect(Node<List<A>> list,
                                                     String name,
                                                     Function<A, Node<B>> function) {
    return splitAndCollect(list, NamedFunction.create(name, function));
  }

  /**
   * Returns the value of the current node if the condition node is evaluated as {@code true}.
   * Otherwise, returns a node with a {@code null} value.
   */
  public Node<R> when(Node<Boolean> conditionNode) {
    return ifThen(conditionNode, this);
  }

  /**
   * Returns the value of the current node if the condition node is evaluated as {@code false}.
   * Otherwise, returns a node with a {@code null} value.
   */
  public Node<R> unless(Node<Boolean> conditionNode) {
    return ifThen(NotNode.of(conditionNode), this);
  }

  /**
   * Returns the value of the current node if the condition node is successful. Otherwise,
   * returns a node with a {@code null} value.
   */
  public Node<R> whenSuccess(Node conditionNode) {
    return ifSuccessThen(conditionNode, this);
  }

  /**
   * Returns the value of the other node if the current one has failed or has {@code null} value,
   * otherwise it returns the current node.
   */
  public Node<R> orElse(Node<R> otherNode) {
    return ifSuccessThenElse(this, this, otherNode);
  }

  /**
   * Create a predicate out of this node
   */
  public Node<Boolean> predicate(String name, Predicate<R> predicate) {
    return PredicateNode.create(name, this, predicate);
  }

  public Node<Boolean> predicate(NamedPredicate<R> predicate) {
    return PredicateNode.create(predicate.getName(), this, predicate);
  }

  public Node<Boolean> isNull() {
    return this.predicate(getName() + "_isNull", Objects::isNull);
  }

  public Node<Boolean> isNotNull() {
    return this.predicate(getName() + "_isNotNull", Objects::nonNull);
  }

  //
  // Convenient Helpers
  //

  /**
   * This statically shared node represents an optional node with an empty value. Any node that
   * depends on this node will always get {@link Optional#empty()} from emit.
   */
  private static final Node STEP_OPTIONAL_EMPTY = new Node("EMPTY", true, false) {
    @Override
    protected CompletableFuture evaluate() {
      return FUTURE_EMPTY;
    }
  };

  /**
   * Gets an {@code Node} with no contained reference.
   */
  public static <T> Node<Optional<T>> empty() {
    return (Node<Optional<T>>) STEP_OPTIONAL_EMPTY;
  }

  /**
   * Wrap a node which has an optional generic type and return an {@link Optional} wrapped value.
   * <p>
   * This node will always succeed, and will return {@link Optional#empty()} if the underlying node
   * fails.
   */
  public static <T> Node<Optional<T>> optional(final Node<T> node) {
    if (node == null) {
      return empty();
    }
    return new OptionalNodeWrapper<>(node);
  }

  /**
   * Create a fixed value {@code Node}.
   *
   * @param value value of the node
   * @param <T>   type of the node
   */
  public static <T> Node<T> value(T value) {
    return ValueNode.create(value);
  }

  /**
   * Create a fixed value Node.
   *
   * @param <T>   type of the node
   * @param name  name of the node used in graph serialization
   * @param value value of the node
   */
  public static <T> Node<T> value(String name, T value) {
    return ValueNode.create(name, value);
  }

  /**
   * Create a node from a value supplier. The supplier will be called at most once.
   * <p>
   * This is only called when the value node is actually used (have its {@link #emit()} or {@link
   * #apply()} called, not during its creation)
   */
  public static <T> Node<T> valueFromSupplier(String name, Supplier<T> valueSupplier) {
    return SupplierValueNode.create(name, valueSupplier);
  }

  /**
   * Gets a Node with a null value.
   * <p>
   * Any node that depends on this noValue node will not succeed.
   */
  public static <K> Node<K> noValue() {
    if (DebugManager.isEnabled()) {
      return Node.value("null", null);
    } else {
      // non-debug time optimization
      return (Node<K>) NULL_NODE;
    }
  }

  /**
   * Create a failure node with the given exception.
   */
  public static <T> Node<T> fail(final Exception e) {
    return new Node<T>() {
      @Override
      protected CompletableFuture<T> evaluate() throws Exception {
        return CompletableFutures.exceptionallyCompletedFuture(e);
      }
    };
  }

  public static <T> PredicateSwitchNode<T> ifThenElse(Node<Boolean> predicateNode,
                                                      Node<T> trueNode,
                                                      Node<T> falseNode) {
    return new PredicateSwitchNode<>(predicateNode, trueNode, falseNode);
  }

  public static <T> PredicateSwitchNode<T> ifThen(Node<Boolean> predicateNode,
                                                  Node<T> trueNode) {
    return new PredicateSwitchNode<>(predicateNode, trueNode, Node.noValue());
  }

  public static <T> PredicateSwitchNode<T> ifSuccessThenElse(Node testNode,
                                                             Node<T> trueNode,
                                                             Node<T> falseNode) {
    return ifThenElse(IfSuccessfulNode.create(testNode), trueNode, falseNode);
  }

  public static <T> PredicateSwitchNode<T> ifSuccessThen(Node testNode,
                                                         Node<T> trueNode) {
    return ifThen(IfSuccessfulNode.create(testNode), trueNode);
  }

  /**
   * Wrap a {@link CompletableFuture} object into a node.
   */
  public static <T> Node<T> wrapCompletableFuture(final CompletableFuture<T> future) {
    return wrapCompletableFuture("wrapCompletableFuture[" + getLastTemplateType(future.getClass()) + "]", future);
  }

  /**
   * Wrap a {@link CompletableFuture} object into a node with a name.
   */
  public static <T> Node<T> wrapCompletableFuture(final String name, final CompletableFuture<T> future) {
    // Create a dummy wrapping node, not optional, no dependencies or sinks
    return new NullableNode<T>(name) {
      @Override
      protected CompletableFuture<T> evaluate() {
        return future;
      }
    };
  }

}
