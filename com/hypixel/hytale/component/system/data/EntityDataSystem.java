package com.hypixel.hytale.component.system.data;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.task.ParallelRangeTask;
import com.hypixel.hytale.component.task.ParallelTask;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EntityDataSystem<ECS_TYPE, Q, R> extends ArchetypeDataSystem<ECS_TYPE, Q, R> {
   public boolean isParallel() {
      return false;
   }

   @Override
   public void fetch(
      @Nonnull ArchetypeChunk<ECS_TYPE> archetypeChunk,
      @Nonnull Store<ECS_TYPE> store,
      @Nonnull CommandBuffer<ECS_TYPE> commandBuffer,
      Q query,
      List<R> results
   ) {
      doFetch(this, archetypeChunk, store, commandBuffer, query, results);
   }

   public abstract void fetch(int var1, ArchetypeChunk<ECS_TYPE> var2, Store<ECS_TYPE> var3, CommandBuffer<ECS_TYPE> var4, Q var5, List<R> var6);

   public static <ECS_TYPE, Q, R> void doFetch(
      @Nonnull EntityDataSystem<ECS_TYPE, Q, R> system,
      @Nonnull ArchetypeChunk<ECS_TYPE> archetypeChunk,
      @Nonnull Store<ECS_TYPE> store,
      @Nonnull CommandBuffer<ECS_TYPE> commandBuffer,
      Q query,
      List<R> results
   ) {
      if (system.isParallel()) {
         int size = archetypeChunk.size();
         if (size == 0) {
            return;
         }

         ParallelTask<EntityDataSystem.SystemTaskData<ECS_TYPE, ?, ?>> task = store.getFetchTask();
         ParallelRangeTask<EntityDataSystem.SystemTaskData<ECS_TYPE, ?, ?>> systemTask = task.appendTask();
         systemTask.init(0, size);
         int i = 0;

         for (int systemTaskSize = systemTask.size(); i < systemTaskSize; i++) {
            ((EntityDataSystem.SystemTaskData<ECS_TYPE, Q, ?>)systemTask.get(i)).init(system, archetypeChunk, store, commandBuffer.fork(), query);
         }
      } else {
         int index = 0;

         for (int archetypeChunkSize = archetypeChunk.size(); index < archetypeChunkSize; index++) {
            system.fetch(index, archetypeChunk, store, commandBuffer, query, results);
         }
      }
   }

   public static class SystemTaskData<ECS_TYPE, Q, R> implements IntConsumer {
      private final List<R> results = new ObjectArrayList();
      @Nullable
      private EntityDataSystem<ECS_TYPE, Q, R> system;
      @Nullable
      private ArchetypeChunk<ECS_TYPE> archetypeChunk;
      @Nullable
      private Store<ECS_TYPE> store;
      @Nullable
      private CommandBuffer<ECS_TYPE> commandBuffer;
      @Nullable
      private Q query;

      public void init(
         EntityDataSystem<ECS_TYPE, Q, R> system,
         ArchetypeChunk<ECS_TYPE> archetypeChunk,
         Store<ECS_TYPE> store,
         CommandBuffer<ECS_TYPE> commandBuffer,
         Q query
      ) {
         this.system = system;
         this.archetypeChunk = archetypeChunk;
         this.store = store;
         this.commandBuffer = commandBuffer;
         this.query = query;
      }

      @Override
      public void accept(int index) {
         assert this.commandBuffer.setThread();

         this.system.fetch(index, this.archetypeChunk, this.store, this.commandBuffer, this.query, this.results);
      }

      public void clear() {
         this.system = null;
         this.archetypeChunk = null;
         this.store = null;
         this.commandBuffer = null;
         this.query = null;
         this.results.clear();
      }

      public static <ECS_TYPE, Q, R> void invokeParallelTask(
         @Nonnull ParallelTask<EntityDataSystem.SystemTaskData<ECS_TYPE, Q, R>> parallelTask,
         @Nonnull CommandBuffer<ECS_TYPE> commandBuffer,
         @Nonnull List<R> results
      ) {
         int parallelTaskSize = parallelTask.size();
         if (parallelTaskSize > 0) {
            parallelTask.doInvoke();

            for (int x = 0; x < parallelTaskSize; x++) {
               ParallelRangeTask<EntityDataSystem.SystemTaskData<ECS_TYPE, Q, R>> systemTask = parallelTask.get(x);
               int i = 0;

               for (int systemTaskSize = systemTask.size(); i < systemTaskSize; i++) {
                  EntityDataSystem.SystemTaskData<ECS_TYPE, Q, R> taskData = systemTask.get(i);
                  results.addAll(taskData.results);
                  taskData.commandBuffer.mergeParallel(commandBuffer);
                  taskData.clear();
               }
            }
         }
      }
   }
}
