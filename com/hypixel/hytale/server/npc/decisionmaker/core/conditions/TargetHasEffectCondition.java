package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.SimpleCondition;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TargetHasEffectCondition extends SimpleCondition {
   public static final BuilderCodec<TargetHasEffectCondition> CODEC = BuilderCodec.builder(
         TargetHasEffectCondition.class, TargetHasEffectCondition::new, ABSTRACT_CODEC
      )
      .documentation("A simple boolean condition that returns whether the target entity has a specific active entity effect.")
      .<String>appendInherited(
         new KeyedCodec<>("EffectId", Codec.STRING),
         (condition, s) -> condition.entityEffectId = s,
         condition -> condition.entityEffectId,
         (condition, parent) -> condition.entityEffectId = parent.entityEffectId
      )
      .addValidator(EntityEffect.VALIDATOR_CACHE.getValidator())
      .documentation("The entity effect to check for.")
      .add()
      .afterDecode(condition -> {
         if (condition.entityEffectId != null) {
            condition.entityEffectIndex = EntityEffect.getAssetMap().getIndex(condition.entityEffectId);
         }
      })
      .build();
   @Nullable
   private String entityEffectId;
   private int entityEffectIndex;

   protected TargetHasEffectCondition() {
   }

   @Override
   protected boolean evaluate(
      int selfIndex,
      ArchetypeChunk<EntityStore> archetypeChunk,
      @Nullable Ref<EntityStore> target,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      EvaluationContext context
   ) {
      if (target != null && target.isValid()) {
         EffectControllerComponent effectController = commandBuffer.getComponent(target, EffectControllerComponent.getComponentType());
         return effectController == null ? false : effectController.hasEffect(this.entityEffectIndex);
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "TargetHasEffectCondition{entityEffectId='" + this.entityEffectId + "'} " + super.toString();
   }
}
