package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public abstract class CountObjectiveTaskAsset extends ObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<CountObjectiveTaskAsset> CODEC = BuilderCodec.abstractBuilder(CountObjectiveTaskAsset.class, BASE_CODEC)
      .append(new KeyedCodec<>("Count", Codec.INTEGER), (taskAsset, count) -> taskAsset.count = count, taskAsset -> taskAsset.count)
      .addValidator(Validators.greaterThan(0))
      .add()
      .build();
   protected int count = 1;

   public CountObjectiveTaskAsset(String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers, int count) {
      super(descriptionId, taskConditions, mapMarkers);
      this.count = count;
   }

   protected CountObjectiveTaskAsset() {
   }

   public int getCount() {
      return this.count;
   }

   @Override
   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      return !(task instanceof CountObjectiveTaskAsset) ? false : ((CountObjectiveTaskAsset)task).count == this.count;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CountObjectiveTaskAsset{count=" + this.count + "} " + super.toString();
   }
}
