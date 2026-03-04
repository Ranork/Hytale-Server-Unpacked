package com.hypixel.hytale.common.util;

import com.hypixel.hytale.logger.HytaleLogger;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class CompletableFutureUtil {
   public static final Function<Throwable, ?> fn = throwable -> {
      if (!(throwable instanceof CompletableFutureUtil.TailedRuntimeException)) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(throwable)).log("Unhandled exception! " + Thread.currentThread());
      }

      throw new CompletableFutureUtil.TailedRuntimeException(throwable);
   };

   @Nonnull
   public static <T> CompletableFuture<T> whenComplete(@Nonnull CompletableFuture<T> future, @Nonnull CompletableFuture<T> callee) {
      return future.whenComplete((result, throwable) -> {
         if (throwable != null) {
            callee.completeExceptionally(throwable);
         } else {
            callee.complete((T)result);
         }
      });
   }

   public static boolean isCanceled(Throwable throwable) {
      return throwable instanceof CancellationException
         || throwable instanceof CompletionException && throwable.getCause() != null && throwable.getCause() != throwable && isCanceled(throwable.getCause());
   }

   @Nonnull
   public static <T> CompletableFuture<T> _catch(@Nonnull CompletableFuture<T> future) {
      return future.exceptionally((Function<Throwable, ? extends T>)fn);
   }

   @Nonnull
   public static <T> CompletableFuture<T> completionCanceled() {
      CompletableFuture<T> out = new CompletableFuture<>();
      out.cancel(false);
      return out;
   }

   public static void joinWithProgress(
      @Nonnull List<CompletableFuture<?>> list, @Nonnull CompletableFutureUtil.ProgressConsumer callback, int millisSleep, int millisProgress
   ) throws InterruptedException {
      CompletableFuture<?> all = CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
      long last = System.nanoTime();
      long nanosProgress = TimeUnit.MILLISECONDS.toNanos(millisProgress);
      int listSize = list.size();

      while (!all.isDone()) {
         Thread.sleep(millisSleep);
         long now;
         if (last + nanosProgress < (now = System.nanoTime())) {
            last = now;
            int done = 0;

            for (CompletableFuture c : list) {
               if (c.isDone()) {
                  done++;
               }
            }

            if (done < listSize) {
               callback.accept((double)done / listSize, done, listSize);
            }
         }
      }

      callback.accept(1.0, listSize, listSize);
      all.join();
   }

   @FunctionalInterface
   public interface ProgressConsumer {
      void accept(double var1, int var3, int var4);
   }

   static class TailedRuntimeException extends RuntimeException {
      public TailedRuntimeException(Throwable cause) {
         super(cause);
      }
   }
}
