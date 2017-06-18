package com.zbiljic.nodez.utils;

import com.zbiljic.nodez.Function0;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A collection of static utility methods that extend the {@link CompletableFuture Java completable
 * future} API.
 */
public final class CompletableFutures {

  private CompletableFutures() { /* No instance methods */ }

  private static final ScheduledExecutorService SCHEDULER =
    Executors.newScheduledThreadPool(
      1,
      new SchedulerThreadFactory("failAfter-%d"));

  /**
   * Returns a new {@code CompletableFuture} that is already exceptionally completed with the given
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

  public static <T> CompletableFuture<T> failAfter(Duration duration) {
    return failAfter(duration, SCHEDULER);
  }

  public static <T> CompletableFuture<T> failAfter(Duration duration,
                                                   ScheduledExecutorService scheduledExecutorService) {
    final CompletableFuture<T> promise = new CompletableFuture<>();
    scheduledExecutorService.schedule(() -> {
        final TimeoutException ex = new TimeoutException("Timeout after " + duration);
        return promise.completeExceptionally(ex);
      },
      duration.toMillis(), MILLISECONDS);
    return promise;
  }

  public static <T> CompletableFuture<T> within(CompletableFuture<T> future,
                                                Duration duration) {
    return within(future, duration, SCHEDULER);
  }

  public static <T> CompletableFuture<T> within(CompletableFuture<T> future,
                                                Duration duration,
                                                ScheduledExecutorService scheduledExecutorService) {
    final CompletableFuture<T> timeout = failAfter(duration, scheduledExecutorService);
    return future.applyToEither(timeout, Function.identity());
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

  /**
   * The scheduler thread factory.
   */
  static class SchedulerThreadFactory implements ThreadFactory {

    private final String nameFormat;
    private final Boolean daemon;
    private final ThreadFactory backingThreadFactory;
    private final AtomicLong count;

    SchedulerThreadFactory(String nameFormat) {
      String unused = format(nameFormat, 0); // fail fast if the format is bad or null
      this.nameFormat = nameFormat;
      daemon = true;
      backingThreadFactory = Executors.defaultThreadFactory();
      count = (nameFormat != null) ? new AtomicLong(0) : null;
    }

    public Thread newThread(Runnable runnable) {
      Thread thread = backingThreadFactory.newThread(runnable);
      if (nameFormat != null) {
        thread.setName(format(nameFormat, count.getAndIncrement()));
      }
      if (daemon != null) {
        thread.setDaemon(daemon);
      }
      return thread;
    }

    private static String format(String format, Object... args) {
      return String.format(Locale.ROOT, format, args);
    }
  }

}
