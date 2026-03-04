package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.Condition;
import java.util.concurrent.ThreadLocalRandom;

public class RandomiserCondition extends Condition {
   public static final BuilderCodec<RandomiserCondition> CODEC = BuilderCodec.builder(RandomiserCondition.class, RandomiserCondition::new, BASE_CODEC)
      .documentation("A condition that jitters between two defined values to add a small amount of randomness to the final utility value.")
      .<Double>appendInherited(
         new KeyedCodec<>("MinValue", Codec.DOUBLE),
         (condition, d) -> condition.minValue = d,
         condition -> condition.minValue,
         (condition, parent) -> condition.minValue = parent.minValue
      )
      .documentation("The minimum bound of the jitter.")
      .addValidator(Validators.range(0.0, 1.0))
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("MaxValue", Codec.DOUBLE),
         (condition, d) -> condition.maxValue = d,
         condition -> condition.maxValue,
         (condition, parent) -> condition.maxValue = parent.maxValue
      )
      .documentation("The maximum bound of the jitter.")
      .addValidator(Validators.range(0.0, 1.0))
      .add()
      .build();
   protected double minValue;
   protected double maxValue;

   @Override
   public double calculateUtility(
      int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, Ref<EntityStore> target, CommandBuffer<EntityStore> commandBuffer, EvaluationContext context
   ) {
      return ThreadLocalRandom.current().nextDouble(this.minValue, this.maxValue);
   }

   @Override
   public int getSimplicity() {
      return 10;
   }
}
