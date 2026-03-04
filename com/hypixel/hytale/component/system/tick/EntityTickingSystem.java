package com.hypixel.hytale.component.system.tick;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.task.ParallelRangeTask;
import com.hypixel.hytale.component.task.ParallelTask;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EntityTickingSystem<ECS_TYPE> extends ArchetypeTickingSystem<ECS_TYPE> {
   protected static boolean maybeUseParallel(int archetypeChunkSize, int taskCount) {
      return false;
   }

   protected static boolean useParallel(int archetypeChunkSize, int taskCount) {
      return taskCount > 0 || archetypeChunkSize > ParallelRangeTask.PARALLELISM;
   }

   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return false;
   }

   @Override
   public void tick(float dt, @Nonnull ArchetypeChunk<ECS_TYPE> archetypeChunk, @Nonnull Store<ECS_TYPE> store, @Nonnull CommandBuffer<ECS_TYPE> commandBuffer) {
      doTick(this, dt, archetypeChunk, store, commandBuffer);
   }

   public abstract void tick(float var1, int var2, @Nonnull ArchetypeChunk<ECS_TYPE> var3, @Nonnull Store<ECS_TYPE> var4, @Nonnull CommandBuffer<ECS_TYPE> var5);

   public static <ECS_TYPE> void doTick(
      @Nonnull EntityTickingSystem<ECS_TYPE> system,
      float dt,
      @Nonnull ArchetypeChunk<ECS_TYPE> archetypeChunk,
      @Nonnull Store<ECS_TYPE> store,
      @Nonnull CommandBuffer<ECS_TYPE> commandBuffer
   ) {
      int archetypeChunkSize = archetypeChunk.size();
      if (archetypeChunkSize != 0) {
         ParallelTask<EntityTickingSystem.SystemTaskData<ECS_TYPE>> task = store.getParallelTask();
         if (system.isParallel(archetypeChunkSize, task.size())) {
            ParallelRangeTask<EntityTickingSystem.SystemTaskData<ECS_TYPE>> systemTask = task.appendTask();
            systemTask.init(0, archetypeChunkSize);
            int i = 0;

            for (int systemTaskSize = systemTask.size(); i < systemTaskSize; i++) {
               systemTask.get(i).init(system, dt, archetypeChunk, store, commandBuffer.fork());
            }
         } else {
            for (int index = 0; index < archetypeChunkSize; index++) {
               system.tick(dt, index, archetypeChunk, store, commandBuffer);
            }
         }
      }
   }

   public static class SystemTaskData<ECS_TYPE> implements IntConsumer {
      @Nullable
      private EntityTickingSystem<ECS_TYPE> system;
      private float dt;
      @Nullable
      private ArchetypeChunk<ECS_TYPE> archetypeChunk;
      @Nullable
      private Store<ECS_TYPE> store;
      @Nullable
      private CommandBuffer<ECS_TYPE> commandBuffer;

      public void init(
         EntityTickingSystem<ECS_TYPE> system, float dt, ArchetypeChunk<ECS_TYPE> archetypeChunk, Store<ECS_TYPE> store, CommandBuffer<ECS_TYPE> commandBuffer
      ) {
         this.system = system;
         this.dt = dt;
         this.archetypeChunk = archetypeChunk;
         this.store = store;
         this.commandBuffer = commandBuffer;
      }

      @Override
      public void accept(int index) {
         assert this.commandBuffer.setThread();

         this.system.tick(this.dt, index, this.archetypeChunk, this.store, this.commandBuffer);
      }

      public void clear() {
         this.system = null;
         this.archetypeChunk = null;
         this.store = null;
         this.commandBuffer = null;
      }

      public static <ECS_TYPE> void invokeParallelTask(
         @Nonnull ParallelTask<EntityTickingSystem.SystemTaskData<ECS_TYPE>> parallelTask, @Nonnull CommandBuffer<ECS_TYPE> commandBuffer
      ) {
         int parallelTaskSize = parallelTask.size();
         if (parallelTaskSize > 0) {
            parallelTask.doInvoke();

            for (int x = 0; x < parallelTaskSize; x++) {
               ParallelRangeTask<EntityTickingSystem.SystemTaskData<ECS_TYPE>> systemTask = parallelTask.get(x);
               int i = 0;

               for (int systemTaskSize = systemTask.size(); i < systemTaskSize; i++) {
                  EntityTickingSystem.SystemTaskData<ECS_TYPE> taskData = systemTask.get(i);
                  taskData.commandBuffer.mergeParallel(commandBuffer);
                  taskData.clear();
               }
            }
         }
      }
   }
}
