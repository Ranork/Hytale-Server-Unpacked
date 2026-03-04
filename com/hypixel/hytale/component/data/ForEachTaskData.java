package com.hypixel.hytale.component.data;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.task.ParallelRangeTask;
import com.hypixel.hytale.component.task.ParallelTask;
import com.hypixel.hytale.function.consumer.IntBiObjectConsumer;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ForEachTaskData<ECS_TYPE> implements IntConsumer {
   @Nullable
   private IntBiObjectConsumer<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> consumer;
   @Nullable
   private ArchetypeChunk<ECS_TYPE> archetypeChunk;
   @Nullable
   private CommandBuffer<ECS_TYPE> commandBuffer;

   public void init(
      IntBiObjectConsumer<ArchetypeChunk<ECS_TYPE>, CommandBuffer<ECS_TYPE>> consumer,
      ArchetypeChunk<ECS_TYPE> archetypeChunk,
      CommandBuffer<ECS_TYPE> commandBuffer
   ) {
      this.consumer = consumer;
      this.archetypeChunk = archetypeChunk;
      this.commandBuffer = commandBuffer;
   }

   @Override
   public void accept(int index) {
      assert this.commandBuffer.setThread();

      this.consumer.accept(index, this.archetypeChunk, this.commandBuffer);
   }

   public void clear() {
      this.consumer = null;
      this.archetypeChunk = null;
      this.commandBuffer = null;
   }

   public static <ECS_TYPE> void invokeParallelTask(
      @Nonnull ParallelTask<ForEachTaskData<ECS_TYPE>> parallelTask, @Nonnull CommandBuffer<ECS_TYPE> commandBuffer
   ) {
      int parallelTaskSize = parallelTask.size();
      if (parallelTaskSize > 0) {
         parallelTask.doInvoke();

         for (int x = 0; x < parallelTaskSize; x++) {
            ParallelRangeTask<ForEachTaskData<ECS_TYPE>> systemTask = parallelTask.get(x);
            int i = 0;

            for (int systemTaskSize = systemTask.size(); i < systemTaskSize; i++) {
               ForEachTaskData<ECS_TYPE> data = systemTask.get(i);
               data.commandBuffer.mergeParallel(commandBuffer);
               data.clear();
            }
         }
      }
   }
}
