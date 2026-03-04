package com.hypixel.hytale.server.flock.decisionmaker.conditions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.ScaledCurveCondition;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;

public class FlockSizeCondition extends ScaledCurveCondition {
   public static final BuilderCodec<FlockSizeCondition> CODEC = BuilderCodec.builder(
         FlockSizeCondition.class, FlockSizeCondition::new, ScaledCurveCondition.ABSTRACT_CODEC
      )
      .build();

   protected FlockSizeCondition() {
   }

   @Override
   protected double getInput(
      int selfIndex,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      Ref<EntityStore> target,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      EvaluationContext context
   ) {
      NPCEntity self = archetypeChunk.getComponent(selfIndex, NPCEntity.getComponentType());
      FlockMembership membership = archetypeChunk.getComponent(selfIndex, FlockMembership.getComponentType());
      if (membership == null) {
         return 1.0;
      } else {
         Ref<EntityStore> flockReference = membership.getFlockRef();
         return flockReference != null && flockReference.isValid() ? commandBuffer.getComponent(flockReference, EntityGroup.getComponentType()).size() : 1.0;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "FlockSizeCondition{} " + super.toString();
   }
}
