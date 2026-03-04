package com.hypixel.hytale.common.thread;

import com.hypixel.hytale.metrics.InitStackThread;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import javax.annotation.Nonnull;

public class HytaleForkJoinThreadFactory implements ForkJoinWorkerThreadFactory {
   @Nonnull
   @Override
   public ForkJoinWorkerThread newThread(@Nonnull ForkJoinPool pool) {
      return new HytaleForkJoinThreadFactory.WorkerThread(pool);
   }

   public static class WorkerThread extends ForkJoinWorkerThread implements InitStackThread {
      @Nonnull
      private final StackTraceElement[] initStack = Thread.currentThread().getStackTrace();

      protected WorkerThread(@Nonnull ForkJoinPool pool) {
         super(pool);
      }

      @Nonnull
      @Override
      public StackTraceElement[] getInitStack() {
         return this.initStack;
      }
   }
}
