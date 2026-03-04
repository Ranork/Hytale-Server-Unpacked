package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.ScaledCurveCondition;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TargetDistanceCondition extends ScaledCurveCondition {
   public static final BuilderCodec<TargetDistanceCondition> CODEC = BuilderCodec.builder(
         TargetDistanceCondition.class, TargetDistanceCondition::new, ScaledCurveCondition.ABSTRACT_CODEC
      )
      .documentation("A scaled curve condition that returns a utility value based on the distance between the NPC and the target.")
      .build();
   @Nonnull
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();

   @Override
   protected double getInput(
      int selfIndex,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nullable Ref<EntityStore> target,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      EvaluationContext context
   ) {
      if (target != null && target.isValid()) {
         TransformComponent selfTransformComponent = archetypeChunk.getComponent(selfIndex, TRANSFORM_COMPONENT_TYPE);

         assert selfTransformComponent != null;

         Vector3d selfPos = selfTransformComponent.getPosition();
         TransformComponent targetTransformComponent = commandBuffer.getComponent(target, TRANSFORM_COMPONENT_TYPE);

         assert targetTransformComponent != null;

         Vector3d targetPos = targetTransformComponent.getPosition();
         return selfPos.distanceTo(targetPos);
      } else {
         return Double.MAX_VALUE;
      }
   }
}
