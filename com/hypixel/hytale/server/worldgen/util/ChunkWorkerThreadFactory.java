package com.hypixel.hytale.server.worldgen.util;

import com.hypixel.hytale.server.worldgen.ChunkGeneratorResource;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class ChunkWorkerThreadFactory implements ThreadFactory {
   private static final AtomicInteger FACTORY_COUNTER = new AtomicInteger();
   private final ChunkGenerator chunkGenerator;
   private final String threadNameFormat;
   @Nonnull
   private final Integer factoryId;
   @Nonnull
   private final AtomicInteger threadCounter;

   public ChunkWorkerThreadFactory(ChunkGenerator chunkGenerator, String threadNameFormat) {
      this.chunkGenerator = chunkGenerator;
      this.threadNameFormat = threadNameFormat;
      this.factoryId = FACTORY_COUNTER.incrementAndGet();
      this.threadCounter = new AtomicInteger();
   }

   @Nonnull
   @Override
   public Thread newThread(Runnable r) {
      Integer threadId = this.threadCounter.incrementAndGet();
      String threadName = String.format(this.threadNameFormat, this.factoryId, threadId);
      ChunkWorkerThreadFactory.ChunkWorker workerThread = new ChunkWorkerThreadFactory.ChunkWorker(r, threadName, this.chunkGenerator);
      workerThread.setDaemon(true);
      return workerThread;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChunkWorkerThreadFactory{chunkGenerator="
         + this.chunkGenerator
         + ", threadNameFormat='"
         + this.threadNameFormat
         + "', factoryId="
         + this.factoryId
         + ", threadCounter="
         + this.threadCounter
         + "}";
   }

   protected static class ChunkWorker extends Thread {
      protected final ChunkGenerator chunkGenerator;

      protected ChunkWorker(Runnable r, @Nonnull String name, ChunkGenerator chunkGenerator) {
         super(r, name);
         this.chunkGenerator = chunkGenerator;
      }

      @Override
      public void run() {
         ChunkGeneratorResource resource = ChunkGenerator.getResource();
         resource.init(this.chunkGenerator);

         try {
            super.run();
         } finally {
            resource.release();
         }
      }
   }
}
