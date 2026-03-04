package com.hypixel.hytale.builtin.npccombatactionevaluator.conditions;

import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemory;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.ScaledCurveCondition;
import javax.annotation.Nonnull;

public class TargetMemoryCountCondition extends ScaledCurveCondition {
   @Nonnull
   public static final EnumCodec<TargetMemoryCountCondition.TargetType> TARGET_TYPE_CODEC = new EnumCodec<>(TargetMemoryCountCondition.TargetType.class)
      .documentKey(TargetMemoryCountCondition.TargetType.All, "All known targets.")
      .documentKey(TargetMemoryCountCondition.TargetType.Friendly, "Known friendly targets.")
      .documentKey(TargetMemoryCountCondition.TargetType.Hostile, "Known hostile targets.");
   @Nonnull
   public static final BuilderCodec<TargetMemoryCountCondition> CODEC = BuilderCodec.builder(
         TargetMemoryCountCondition.class, TargetMemoryCountCondition::new, ScaledCurveCondition.ABSTRACT_CODEC
      )
      .documentation("A scaled curve condition that returns a utility value based on the number of known targets in the memory.")
      .<TargetMemoryCountCondition.TargetType>appendInherited(
         new KeyedCodec<>("TargetType", TARGET_TYPE_CODEC),
         (condition, e) -> condition.targetType = e,
         condition -> condition.targetType,
         (condition, parent) -> condition.targetType = parent.targetType
      )
      .documentation("The type of targets to count.")
      .add()
      .build();
   @Nonnull
   protected static final ComponentType<EntityStore, TargetMemory> TARGET_MEMORY_COMPONENT_TYPE = TargetMemory.getComponentType();
   protected TargetMemoryCountCondition.TargetType targetType = TargetMemoryCountCondition.TargetType.Hostile;

   @Override
   protected double getInput(
      int selfIndex,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      Ref<EntityStore> target,
      CommandBuffer<EntityStore> commandBuffer,
      EvaluationContext context
   ) {
      TargetMemory targetMemoryComponent = archetypeChunk.getComponent(selfIndex, TARGET_MEMORY_COMPONENT_TYPE);

      assert targetMemoryComponent != null;

      return switch (this.targetType) {
         case Hostile -> targetMemoryComponent.getKnownHostiles().size();
         case Friendly -> targetMemoryComponent.getKnownFriendlies().size();
         case All -> targetMemoryComponent.getKnownFriendlies().size() + targetMemoryComponent.getKnownHostiles().size();
      };
   }

   private static enum TargetType {
      Hostile,
      Friendly,
      All;
   }
}
