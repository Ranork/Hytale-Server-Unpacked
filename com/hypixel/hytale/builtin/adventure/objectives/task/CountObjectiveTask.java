package com.hypixel.hytale.builtin.adventure.objectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.CountObjectiveTaskAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public abstract class CountObjectiveTask extends ObjectiveTask {
   @Nonnull
   public static final BuilderCodec<CountObjectiveTask> CODEC = BuilderCodec.abstractBuilder(CountObjectiveTask.class, ObjectiveTask.BASE_CODEC)
      .append(new KeyedCodec<>("Count", Codec.INTEGER), (countTask, integer) -> countTask.count = integer, countTask -> countTask.count)
      .add()
      .build();
   protected int count;

   public CountObjectiveTask(@Nonnull CountObjectiveTaskAsset asset, int taskSetIndex, int taskIndex) {
      super(asset, taskSetIndex, taskIndex);
   }

   protected CountObjectiveTask() {
   }

   @Nonnull
   public CountObjectiveTaskAsset getAsset() {
      return (CountObjectiveTaskAsset)super.getAsset();
   }

   @Override
   public boolean checkCompletion() {
      return this.count >= this.getAsset().getCount();
   }

   @Override
   public void assetChanged(@Nonnull Objective objective) {
      if (this.complete) {
         this.count = this.getAsset().getCount();
      }

      super.assetChanged(objective);
   }

   public void increaseTaskCompletion(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, int qty, @Nonnull Objective objective) {
      if (this.areTaskConditionsFulfilled(store, ref, objective.getActivePlayerUUIDs())) {
         this.count = MathUtil.clamp(this.count + qty, this.count, this.getAsset().getCount());
         this.updateTaskCompletion(store, ref, objective);
      }
   }

   public void setTaskCompletion(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, int qty, @Nonnull Objective objective) {
      if (this.areTaskConditionsFulfilled(store, ref, objective.getActivePlayerUUIDs())) {
         this.count = MathUtil.clamp(qty, 0, this.getAsset().getCount());
         this.updateTaskCompletion(store, ref, objective);
      }
   }

   private void updateTaskCompletion(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Objective objective) {
      objective.markDirty();
      this.sendUpdateObjectiveTaskPacket(objective);
      if (this.checkCompletion()) {
         this.consumeTaskConditions(store, ref, objective.getActivePlayerUUIDs());
         this.complete(objective, store);
         objective.checkTaskSetCompletion(store);
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ObjectiveTask toPacket(@Nonnull Objective objective) {
      com.hypixel.hytale.protocol.ObjectiveTask packet = new com.hypixel.hytale.protocol.ObjectiveTask();
      packet.taskDescriptionKey = Message.translation(this.asset.getDescriptionKey(objective.getObjectiveId(), this.taskSetIndex, this.taskIndex))
         .getFormattedMessage();
      packet.currentCompletion = this.count;
      packet.completionNeeded = this.getAsset().getCount();
      return packet;
   }
}
