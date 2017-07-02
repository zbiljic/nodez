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
package com.zbiljic.nodez.utils;

import com.zbiljic.nodez.Function0;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A collection of static utility methods that extend the {@link CompletableFuture Java completable
 * future} API.
 */
public final class CompletableFutures {

  private CompletableFutures() { /* No instance methods */ }

  /** Constant for a duration of {@code Long.MAX_VALUE} nanoseconds. */
  private static final Duration TOP = Duration.ofNanos(Long.MAX_VALUE);

  /**
   * Returns result of {@link CompletableFuture} if it completes within given duration.
   *
   * @param future  the {@link CompletableFuture} whose result will be returned for if it succeeds
   * @param timeout the maximum duration to wait
   * @param <T>     the type of the result of the {@link CompletableFuture}, that will be returned
   *                if the future succeeds
   * @return result of {@link CompletableFuture} if it completes within given duration
   * @throws CancellationException if this future was cancelled
   * @throws ExecutionException    if this future completed exceptionally
   * @throws InterruptedException  if the current thread was interrupted while waiting
   * @throws TimeoutException      if the result is not ready within timeout
   */
  public static <T> T awaitResult(CompletableFuture<T> future, Duration timeout) throws Exception {
    return future.get(timeout.toNanos(), NANOSECONDS);
  }

  /**
   * Returns result of {@link CompletableFuture} when it completes.
   *
   * @param future the {@link CompletableFuture} whose result will be returned for if it succeeds
   * @param <T>    the type of the result of the {@link CompletableFuture}, that will be returned if
   *               the future succeeds
   * @return result of {@link CompletableFuture} when it completes
   * @throws CancellationException if this future was cancelled
   * @throws ExecutionException    if this future completed exceptionally
   * @throws InterruptedException  if the current thread was interrupted while waiting
   */
  public static <T> T awaitResult(CompletableFuture<T> future) throws Exception {
    return awaitResult(future, TOP);
  }

  /**
   * Returns {@link Optional} that will contain result of {@link CompletableFuture} if it completes
   * within given duration, or {@link Optional#empty()} if some exception occurs.
   *
   * @param future  the {@link CompletableFuture} whose result will be returned for if it succeeds
   * @param timeout the maximum duration to wait
   * @param <T>     the type of the result of the {@link CompletableFuture}, that will be returned
   *                when if the future succeeds
   * @return {@link Optional} that will contain result of {@link CompletableFuture} if it completes
   * within given duration, or {@link Optional#empty()} if some exception occurs
   */
  public static <T> Optional<T> awaitOptionalResult(CompletableFuture<T> future, Duration timeout) {
    try {
      return Optional.of(future.get(timeout.toNanos(), NANOSECONDS));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Returns {@link Optional} that will contain result of {@link CompletableFuture} if it completes,
   * or {@link Optional#empty()} if some exception occurs.
   *
   * @param future the {@link CompletableFuture} whose result will be returned for if it succeeds
   * @param <T>    the type of the result of the {@link CompletableFuture}, that will be returned
   *               when if the future succeeds.
   * @return {@link Optional} that will contain result of {@link CompletableFuture} if it completes
   * within given duration, or {@link Optional#empty()} if some exception occurs
   */
  public static <T> Optional<T> awaitOptionalResult(CompletableFuture<T> future) {
    return awaitOptionalResult(future, TOP);
  }

  /**
   * Check if a future has completed with success.
   */
  public static boolean completedWithSuccess(CompletableFuture<?> future) {
    try {
      return future.isDone() && !future.isCompletedExceptionally();
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Check if a future has completed with failure.
   */
  public static boolean completedWithFailure(CompletableFuture<?> future) {
    try {
      return future.isDone() && future.isCompletedExceptionally();
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }

  public static Throwable getException(CompletableFuture future) {
    if (!completedWithFailure(future)) {
      throw new IllegalStateException();
    }
    try {
      //noinspection unchecked
      return (Throwable) future.exceptionally(new Function<Throwable, Throwable>() {
        @Override
        public Throwable apply(Throwable ex) {
          if (ex instanceof CompletionException) {
            return ex.getCause();
          }
          return ex;
        }
      }).get();
    } catch (InterruptedException | ExecutionException e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a new {@link CompletableFuture} that is already exceptionally completed with the given
   * exception.
   *
   * @param throwable the exception
   * @param <T>       an arbitrary type for the returned future; can be anything since the future
   *                  will be exceptionally completed and thus there will never be a value of type
   *                  {@code T}
   * @return a future that exceptionally completed with the supplied exception
   * @throws NullPointerException if the supplied throwable is {@code null}
   */
  public static <T> CompletableFuture<T> exceptionallyCompletedFuture(Throwable throwable) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    future.completeExceptionally(throwable);
    return future;
  }

  public static <T> CompletableFuture<T> withFallback(
    CompletableFuture<T> future,
    Function<Throwable, ? extends CompletableFuture<T>> fallback) {
    return future
      .handle((response, error) -> error)
      .thenCompose(error -> error != null ? fallback.apply(error) : future);
  }

  public static <T> CompletableFuture<T> withFallback(
    CompletableFuture<T> future,
    BiFunction<CompletableFuture<T>, Throwable, ? extends CompletableFuture<T>> fallback) {
    return future
      .handle((response, error) -> error)
      .thenCompose(error -> error != null ? fallback.apply(future, error) : future);
  }

  public static CompletableFuture allOf(List<CompletableFuture<?>> futures) {
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
  }

  /**
   * Returns a new {@link CompletableFuture} which completes to a list of all values of its input
   * stages, if all succeed.  The list of results is in the same order as the input stages.
   * <p>
   * If any of the given stages complete exceptionally, then the returned future also does so, with
   * a {@link CompletionException} holding this exception as its cause.
   * <p>
   * If no stages are provided, returns a future holding an empty list.
   *
   * @param stages a {@link List} of {@link CompletionStage}s to combine
   * @param <T>    the common type of all of the input stages, that determines the type of the
   *               output future
   * @return a future that completes to a list of the results of the supplied stages
   */
  public static <T> CompletableFuture<List<T>> collect(List<? extends CompletionStage<? extends T>> stages) {
    // We use traditional for-loops instead of streams here for performance reasons

    @SuppressWarnings("unchecked") // generic array creation
    final CompletableFuture<? extends T>[] all = new CompletableFuture[stages.size()];
    for (int i = 0; i < stages.size(); i++) {
      all[i] = stages.get(i).toCompletableFuture();
    }
    return CompletableFuture.allOf(all)
      .thenApply(ignored -> {
        final List<T> result = new ArrayList<>(all.length);
        for (int i = 0; i < all.length; i++) {
          result.add(all[i].join());
        }
        return result;
      });
  }

  /**
   * Wait for a map of {@link CompletableFuture}s and return a future of map, with the keys mapping
   * to the value returned from corresponding futures.
   */
  public static <K, V> CompletableFuture<Map<K, V>> collect(final Map<K, CompletableFuture<V>> futures) {
    try {
      List<CompletableFuture<Pair<K, V>>> entries = new ArrayList<>();
      for (final Map.Entry<K, CompletableFuture<V>> entry : futures.entrySet()) {
        final CompletableFuture<Pair<K, V>> cf = map(entry.getValue(), (Function0<Pair<K, V>>) () -> {
          try {
            return Pair.of(entry.getKey(), entry.getValue().get());
          } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
          }
        });
        entries.add(cf);
      }

      final CompletableFuture<List<Pair<K, V>>> listFuture = collect(entries);

      return map(listFuture, (Function0<Map<K, V>>) () -> {
        Map<K, V> result = new HashMap<>();
        try {
          for (Pair<K, V> pair : listFuture.get()) {
            result.put(pair.left(), pair.right());
          }
          return result;
        } catch (Exception e) {
          Throwables.throwIfUnchecked(e);
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }

  private static <A, R> CompletableFuture<R> map(final CompletableFuture<A> future, final Function0<R> f) {
    return future.thenCompose(unused -> CompletableFuture.completedFuture(f.apply()));
  }

}
