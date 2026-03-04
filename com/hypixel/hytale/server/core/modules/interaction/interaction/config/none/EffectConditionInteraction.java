package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Match;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EffectConditionInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<EffectConditionInteraction> CODEC = BuilderCodec.builder(
         EffectConditionInteraction.class, EffectConditionInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("An interaction that is successful if the given effects exist (or don't) on the target entity.")
      .<String[]>append(
         new KeyedCodec<>("EntityEffectIds", Codec.STRING_ARRAY),
         (effectConditionInteraction, strings) -> effectConditionInteraction.entityEffectIds = strings,
         effectConditionInteraction -> effectConditionInteraction.entityEffectIds
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyArray())
      .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getArrayValidator().late())
      .add()
      .<Match>append(
         new KeyedCodec<>("Match", new EnumCodec<>(Match.class)),
         (effectConditionInteraction, aBoolean) -> effectConditionInteraction.match = aBoolean,
         effectConditionInteraction -> effectConditionInteraction.match
      )
      .documentation(
         "Field to specify whether the entity must have the specified effects (All), or must not have the specified effects (None). Default value is: All."
      )
      .add()
      .<InteractionTarget>appendInherited(
         new KeyedCodec<>("Entity", InteractionTarget.CODEC), (o, i) -> o.entityTarget = i, o -> o.entityTarget, (o, p) -> o.entityTarget = p.entityTarget
      )
      .documentation("The entity to target for this interaction.")
      .addValidator(Validators.nonNull())
      .add()
      .afterDecode(
         effectConditionInteraction -> effectConditionInteraction.entityEffectIndexes = resolveEntityEffects(effectConditionInteraction.entityEffectIds)
      )
      .build();
   protected String[] entityEffectIds;
   protected int[] entityEffectIndexes;
   @Nonnull
   protected Match match = Match.All;
   @Nonnull
   private InteractionTarget entityTarget = InteractionTarget.USER;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      Ref<EntityStore> targetRef = this.entityTarget.getEntity(context, ref);
      if (targetRef != null && targetRef.isValid()) {
         EffectControllerComponent effectControllerComponent = commandBuffer.getComponent(targetRef, EffectControllerComponent.getComponentType());
         if (effectControllerComponent != null) {
            Int2ObjectMap<ActiveEntityEffect> activeEffects = effectControllerComponent.getActiveEffects();

            for (int i = 0; i < this.entityEffectIndexes.length; i++) {
               switch (this.match) {
                  case All:
                     if (!activeEffects.containsKey(this.entityEffectIndexes[i])) {
                        context.getState().state = InteractionState.Failed;
                        return;
                     }
                     break;
                  case None:
                     if (activeEffects.containsKey(this.entityEffectIndexes[i])) {
                        context.getState().state = InteractionState.Failed;
                        return;
                     }
               }
            }
         }
      }
   }

   private static int[] resolveEntityEffects(@Nullable String[] entityEffectIds) {
      if (entityEffectIds == null) {
         return ArrayUtil.EMPTY_INT_ARRAY;
      } else {
         IndexedLookupTableAssetMap<String, EntityEffect> entityEffectAssetMap = EntityEffect.getAssetMap();
         int[] entityEffectIndexes = new int[entityEffectIds.length];

         for (int i = 0; i < entityEffectIds.length; i++) {
            entityEffectIndexes[i] = entityEffectAssetMap.getIndex(entityEffectIds[i]);
         }

         return entityEffectIndexes;
      }
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.EffectConditionInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.EffectConditionInteraction p = (com.hypixel.hytale.protocol.EffectConditionInteraction)packet;
      p.entityEffects = this.entityEffectIndexes;
      p.match = this.match;
      p.entityTarget = this.entityTarget.toProtocol();
   }

   @Nonnull
   @Override
   public String toString() {
      return "EffectConditionInteraction{entityEffectIds="
         + Arrays.toString((Object[])this.entityEffectIds)
         + ", entityEffectIndexes="
         + Arrays.toString(this.entityEffectIndexes)
         + ", match="
         + this.match
         + ", entityTarget="
         + this.entityTarget
         + "} "
         + super.toString();
   }
}
