package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.SimpleCondition;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LineOfSightCondition extends SimpleCondition {
   public static final BuilderCodec<LineOfSightCondition> CODEC = BuilderCodec.builder(LineOfSightCondition.class, LineOfSightCondition::new, ABSTRACT_CODEC)
      .documentation("A simple boolean condition that returns whether or not there is a line of sight to the target.")
      .build();
   @Nullable
   protected static final ComponentType<EntityStore, NPCEntity> NPC_COMPONENT_TYPE = NPCEntity.getComponentType();

   @Override
   public int getSimplicity() {
      return 40;
   }

   @Override
   protected boolean evaluate(
      int selfIndex,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nullable Ref<EntityStore> targetRef,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      EvaluationContext context
   ) {
      if (targetRef != null && targetRef.isValid()) {
         Ref<EntityStore> selfRef = archetypeChunk.getReferenceTo(selfIndex);
         NPCEntity selfNpcComponent = archetypeChunk.getComponent(selfIndex, NPC_COMPONENT_TYPE);

         assert selfNpcComponent != null;

         PositionCache positionCache = selfNpcComponent.getRole().getPositionCache();
         return positionCache.hasLineOfSight(selfRef, targetRef, commandBuffer);
      } else {
         return false;
      }
   }
}
