package com.hypixel.hytale.server.core.io.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Comparator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class LatencySimulationHandler extends ChannelDuplexHandler {
   public static final String PIPELINE_KEY = "latencySimulator";
   private static final AtomicInteger counter = new AtomicInteger();
   private final DelayQueue<LatencySimulationHandler.DelayedHandler> delayedQueue = new DelayQueue<>();
   @Nonnull
   private final Thread taskThread;
   private final long delayNanos;

   public LatencySimulationHandler(long delay, @Nonnull TimeUnit unit) {
      this.delayNanos = unit.toNanos(delay);
      this.taskThread = new Thread(() -> {
         try {
            while (!Thread.interrupted()) {
               LatencySimulationHandler.DelayedHandler handler = this.delayedQueue.take();
               handler.ctx.executor().execute(handler);
            }
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
         }
      }, "latency-simulator-" + counter.getAndIncrement());
      this.taskThread.setDaemon(true);
      this.taskThread.start();
   }

   public void read(ChannelHandlerContext ctx) throws Exception {
      this.delayedQueue.offer(new LatencySimulationHandler.DelayedRead(ctx, System.nanoTime() + this.delayNanos));
   }

   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      this.delayedQueue.offer(new LatencySimulationHandler.DelayedWrite(ctx, System.nanoTime() + this.delayNanos, msg, promise));
   }

   public void flush(ChannelHandlerContext ctx) {
      this.delayedQueue.offer(new LatencySimulationHandler.DelayedFlush(ctx, System.nanoTime() + this.delayNanos));
   }

   public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      super.handlerRemoved(ctx);
      this.taskThread.interrupt();
      ObjectArrayList<LatencySimulationHandler.DelayedHandler> list = new ObjectArrayList(this.delayedQueue);
      list.sort(Comparator.comparingLong(value -> value.executeAtNanos));
      ObjectListIterator var3 = list.iterator();

      while (var3.hasNext()) {
         LatencySimulationHandler.DelayedHandler handler = (LatencySimulationHandler.DelayedHandler)var3.next();
         handler.run();
      }
   }

   public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
      super.close(ctx, promise);
      this.taskThread.interrupt();
   }

   public static void setLatency(@Nonnull Channel channel, long delay, @Nonnull TimeUnit unit) {
      ChannelPipeline pipeline = channel.pipeline();
      if (pipeline.get("latencySimulator") == null) {
         if (delay > 0L) {
            pipeline.addAfter("packetArrayEncoder", "latencySimulator", new LatencySimulationHandler(delay, unit));
         }
      } else if (delay <= 0L) {
         pipeline.remove("latencySimulator");
      } else {
         pipeline.replace("latencySimulator", "latencySimulator", new LatencySimulationHandler(delay, unit));
      }
   }

   private static class DelayedFlush extends LatencySimulationHandler.DelayedHandler {
      public DelayedFlush(ChannelHandlerContext ctx, long executeAtNanos) {
         super(ctx, executeAtNanos);
      }

      @Override
      public void run() {
         this.ctx.flush();
      }
   }

   private abstract static class DelayedHandler implements Delayed, Runnable {
      protected final ChannelHandlerContext ctx;
      protected final long executeAtNanos;

      protected DelayedHandler(ChannelHandlerContext ctx, long executeAtNanos) {
         this.ctx = ctx;
         this.executeAtNanos = executeAtNanos;
      }

      @Override
      public long getDelay(@Nonnull TimeUnit unit) {
         return unit.convert(this.executeAtNanos - System.nanoTime(), TimeUnit.NANOSECONDS);
      }

      public int compareTo(@Nonnull Delayed o) {
         return Long.compare(this.executeAtNanos, ((LatencySimulationHandler.DelayedHandler)o).executeAtNanos);
      }
   }

   private static class DelayedRead extends LatencySimulationHandler.DelayedHandler {
      private DelayedRead(ChannelHandlerContext ctx, long executeAtNanos) {
         super(ctx, executeAtNanos);
      }

      @Override
      public void run() {
         this.ctx.read();
      }
   }

   private static class DelayedWrite extends LatencySimulationHandler.DelayedHandler {
      private final Object msg;
      private final ChannelPromise promise;

      public DelayedWrite(ChannelHandlerContext ctx, long executeAtNanos, Object msg, ChannelPromise promise) {
         super(ctx, executeAtNanos);
         this.msg = msg;
         this.promise = promise;
      }

      @Override
      public void run() {
         this.ctx.write(this.msg, this.promise);
      }
   }
}
