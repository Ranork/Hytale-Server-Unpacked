package com.hypixel.hytale.component.task;

import com.hypixel.hytale.common.util.ArrayUtil;
import java.util.Arrays;
import java.util.concurrent.CountedCompleter;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ParallelTask<D extends IntConsumer> extends CountedCompleter<Void> {
   private final Supplier<D> supplier;
   @Nonnull
   private ParallelRangeTask<D>[] subTasks = new ParallelRangeTask[0];
   private int size;
   private volatile boolean running;

   public ParallelTask(Supplier<D> supplier) {
      this(null, supplier);
   }

   public ParallelTask(CountedCompleter<?> completer, Supplier<D> supplier) {
      super(completer);
      this.supplier = supplier;
   }

   @Override
   public void reinitialize() {
      if (this.running) {
         throw new IllegalStateException("Parallel task has already been started");
      } else {
         super.reinitialize();
      }
   }

   public void init() {
      this.reinitialize();
      this.size = 0;
   }

   public ParallelRangeTask<D> appendTask() {
      if (this.running) {
         throw new IllegalStateException("Parallel task has already been started");
      } else {
         if (this.subTasks.length <= this.size) {
            this.subTasks = Arrays.copyOf(this.subTasks, ArrayUtil.grow(this.size));

            for (int i = this.size; i < this.subTasks.length; i++) {
               this.subTasks[i] = new ParallelRangeTask<>(this, this.supplier);
            }
         }

         return this.subTasks[this.size++];
      }
   }

   public int size() {
      return this.size;
   }

   public ParallelRangeTask<D> get(int i) {
      return this.subTasks[i];
   }

   @Override
   public void compute() {
      this.setPendingCount(this.size - 1);

      for (int i = 0; i < this.size - 1; i++) {
         this.subTasks[i].fork();
      }

      this.subTasks[this.size - 1].compute();
   }

   public void doInvoke() {
      this.running = true;

      for (int i = 0; i < this.size; i++) {
         this.subTasks[i].running = true;
      }

      this.invoke();

      for (int i = 0; i < this.size; i++) {
         this.subTasks[i].running = false;
      }

      this.running = false;
   }
}
