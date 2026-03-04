package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.ScaledCurveCondition;
import javax.annotation.Nonnull;

public class TimeSinceLastUsedCondition extends ScaledCurveCondition {
   public static final BuilderCodec<TimeSinceLastUsedCondition> CODEC = BuilderCodec.builder(
         TimeSinceLastUsedCondition.class, TimeSinceLastUsedCondition::new, ScaledCurveCondition.ABSTRACT_CODEC
      )
      .documentation("A scaled curve condition that returns a utility value based on how long it has been since the Option was last used.")
      .build();

   protected TimeSinceLastUsedCondition() {
   }

   @Override
   protected double getInput(
      int selfIndex,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Ref<EntityStore> target,
      CommandBuffer<EntityStore> commandBuffer,
      @Nonnull EvaluationContext context
   ) {
      long interval = System.nanoTime() - context.getLastUsedNanos();
      return interval / 1.0E9;
   }
}
