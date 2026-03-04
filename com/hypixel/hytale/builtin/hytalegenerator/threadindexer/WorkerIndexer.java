package com.hypixel.hytale.builtin.hytalegenerator.threadindexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class WorkerIndexer {
   private final int workerCount;
   @Nonnull
   private final List<WorkerIndexer.Id> ids;

   public WorkerIndexer(int workerCount) {
      if (workerCount <= 0) {
         throw new IllegalArgumentException("workerCount must be > 0");
      } else {
         this.workerCount = workerCount;
         List<WorkerIndexer.Id> tempIds = new ArrayList<>(workerCount);

         for (int i = 0; i < workerCount; i++) {
            tempIds.add(new WorkerIndexer.Id(i));
         }

         this.ids = Collections.unmodifiableList(tempIds);
      }
   }

   public int getWorkerCount() {
      return this.workerCount;
   }

   @Nonnull
   public List<WorkerIndexer.Id> getWorkedIds() {
      return this.ids;
   }

   @Nonnull
   public WorkerIndexer.Session createSession() {
      return new WorkerIndexer.Session();
   }

   public static class Data<T> {
      private T[] data;
      private Supplier<T> initialize;

      public Data(int size, @Nonnull Supplier<T> initializer) {
         this.data = (T[])(new Object[size]);
         this.initialize = initializer;
      }

      public Data(@Nonnull WorkerIndexer.Data<?> other, @Nonnull Supplier<T> initializer) {
         this(other.data.length, initializer);
      }

      public Data(@Nonnull WorkerIndexer workerIndexer, @Nonnull Supplier<T> initializer) {
         this(workerIndexer.getWorkerCount(), initializer);
      }

      public boolean isValid(@Nonnull WorkerIndexer.Id id) {
         return id != null && id.id < this.data.length && id.id >= 0;
      }

      @Nonnull
      public T get(@Nonnull WorkerIndexer.Id id) {
         if (!this.isValid(id)) {
            throw new IllegalArgumentException("Invalid thread id " + id);
         } else {
            if (this.data[id.id] == null) {
               this.data[id.id] = this.initialize.get();

               assert this.data[id.id] != null;
            }

            return this.data[id.id];
         }
      }

      public void set(@Nonnull WorkerIndexer.Id id, T value) {
         if (!this.isValid(id)) {
            throw new IllegalArgumentException("Invalid thread id " + id);
         } else {
            this.data[id.id] = value;
         }
      }

      public void forEach(@Nonnull BiConsumer<WorkerIndexer.Id, T> consumer) {
         for (int i = 0; i < this.data.length; i++) {
            WorkerIndexer.Id id = new WorkerIndexer.Id(i);
            consumer.accept(id, this.data[i]);
         }
      }
   }

   public static class Id {
      public static final int UNKNOWN_THREAD_ID = -1;
      public static final int MAIN_THREAD_ID = 0;
      @Nonnull
      public static final WorkerIndexer.Id UNKNOWN = new WorkerIndexer.Id(-1);
      @Nonnull
      public static final WorkerIndexer.Id MAIN = new WorkerIndexer.Id(0);
      public final int id;

      private Id(int id) {
         this.id = id;
      }

      @Nonnull
      @Override
      public String toString() {
         return String.valueOf(this.id);
      }
   }

   public class Session {
      private int index = 0;

      public WorkerIndexer.Id next() {
         if (this.index >= WorkerIndexer.this.workerCount) {
            throw new IllegalStateException("worker count exceeded");
         } else {
            return WorkerIndexer.this.ids.get(this.index++);
         }
      }

      public boolean hasNext() {
         return this.index < WorkerIndexer.this.workerCount;
      }
   }
}
