package com.hypixel.hytale.server.worldgen.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public final class ChunkThreadPoolExecutor extends ThreadPoolExecutor {
   private static final AtomicInteger GENERATION_COUNTER = new AtomicInteger(0);
   private final int generation = GENERATION_COUNTER.getAndIncrement();
   private final Runnable shutdownHook;

   public ChunkThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory,
      Runnable shutdownHook
   ) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
      this.shutdownHook = shutdownHook;
      LogUtil.getLogger().at(Level.INFO).log("Initialized ChunkGenerator-%d executor", this.generation);
   }

   @Override
   protected void terminated() {
      this.shutdownHook.run();
      LogUtil.getLogger().at(Level.INFO).log("ChunkGenerator-%d executor shutdown complete", this.generation);
   }
}
