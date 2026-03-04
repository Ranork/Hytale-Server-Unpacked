package com.hypixel.hytale.component.task;

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ParallelRangeTask<D extends IntConsumer> extends CountedCompleter<Void> {
   public static final int PARALLELISM = Math.max(ForkJoinPool.getCommonPoolParallelism(), 1);
   public static final int TASK_COUNT = Math.max(ForkJoinPool.getCommonPoolParallelism() << 2, 1);
   @Nonnull
   private final ParallelRangeTask.SubTask<D>[] subTasks = new ParallelRangeTask.SubTask[TASK_COUNT];
   private int size;
   public volatile boolean running;

   public ParallelRangeTask(@Nonnull Supplier<D> supplier) {
      this(null, supplier);
   }

   public ParallelRangeTask(CountedCompleter<?> completer, @Nonnull Supplier<D> supplier) {
      super(completer);

      for (int i = 0; i < this.subTasks.length; i++) {
         this.subTasks[i] = new ParallelRangeTask.SubTask<>(this, supplier.get());
      }
   }

   @Override
   public void reinitialize() {
      if (this.running) {
         throw new IllegalStateException("ParallelRangeTask has already been started");
      } else {
         super.reinitialize();
      }
   }

   @Nonnull
   public ParallelRangeTask<D> init(int from, int to) {
      this.reinitialize();
      int perTask = Math.max((to - from + (this.subTasks.length - 1)) / this.subTasks.length, 1);

      for (this.size = 0; this.size < this.subTasks.length && from < to; this.size++) {
         int next = Math.min(from + perTask, to);
         this.subTasks[this.size].init(from, next);
         from = next;
      }

      if (from < to) {
         throw new IllegalStateException("Failed to distribute the whole range to tasks!");
      } else {
         return this;
      }
   }

   public int size() {
      return this.size;
   }

   public D get(int i) {
      return this.subTasks[i].getData();
   }

   public void set(int i, D data) {
      if (this.running) {
         throw new IllegalStateException("ParallelRangeTask has already been started");
      } else {
         this.subTasks[i].setData(data);
      }
   }

   @Override
   public void compute() {
      this.setPendingCount(this.size - 1);

      for (int i = 0; i < this.size - 1; i++) {
         this.subTasks[i].fork();
      }

      this.subTasks[this.size - 1].compute();
   }

   static class SubTask<D extends IntConsumer> extends CountedCompleter<Void> {
      private int from;
      private int to;
      private D data;

      SubTask(ParallelRangeTask parent, D data) {
         super(parent);
         this.data = data;
      }

      void init(int from, int to) {
         this.reinitialize();
         this.from = from;
         this.to = to;
      }

      D getData() {
         return this.data;
      }

      void setData(D data) {
         this.data = data;
      }

      @Override
      public void compute() {
         for (int i = this.from; i < this.to; i++) {
            this.data.accept(i);
         }

         this.propagateCompletion();
      }
   }
}
