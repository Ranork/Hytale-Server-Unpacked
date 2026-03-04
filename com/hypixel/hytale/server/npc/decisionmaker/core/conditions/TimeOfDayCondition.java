package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.ScaledCurveCondition;
import javax.annotation.Nonnull;

public class TimeOfDayCondition extends ScaledCurveCondition {
   public static final BuilderCodec<TimeOfDayCondition> CODEC = BuilderCodec.builder(
         TimeOfDayCondition.class, TimeOfDayCondition::new, ScaledCurveCondition.ABSTRACT_CODEC
      )
      .documentation("A scaled curve condition that returns a utility value based on the current in-game time of day.")
      .build();

   protected TimeOfDayCondition() {
   }

   @Override
   protected double getInput(
      int selfIndex,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Ref<EntityStore> target,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      EvaluationContext context
   ) {
      WorldTimeResource worldTimeResource = commandBuffer.getResource(WorldTimeResource.getResourceType());
      return worldTimeResource.getDayProgress() * WorldTimeResource.HOURS_PER_DAY;
   }

   @Nonnull
   @Override
   public String toString() {
      return "TimeOfDayCondition{} " + super.toString();
   }
}
