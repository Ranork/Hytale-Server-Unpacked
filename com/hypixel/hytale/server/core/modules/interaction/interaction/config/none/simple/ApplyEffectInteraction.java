package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.simple;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ApplyEffectInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<ApplyEffectInteraction> CODEC = BuilderCodec.builder(
         ApplyEffectInteraction.class, ApplyEffectInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Applies the given entity effect to the entity.")
      .<String>appendInherited(
         new KeyedCodec<>("EffectId", new ContainedAssetCodec<>(EntityEffect.class, EntityEffect.CODEC)),
         (interaction, s) -> interaction.effectId = s,
         interaction -> interaction.effectId,
         (interaction, parent) -> interaction.effectId = parent.effectId
      )
      .addValidator(Validators.nonNull())
      .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
      .add()
      .<InteractionTarget>appendInherited(
         new KeyedCodec<>("Entity", InteractionTarget.CODEC), (o, i) -> o.entityTarget = i, o -> o.entityTarget, (o, p) -> o.entityTarget = p.entityTarget
      )
      .documentation("The entity to target for this interaction.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private String effectId;
   @Nonnull
   private InteractionTarget entityTarget = InteractionTarget.USER;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      if (this.effectId != null) {
         EntityEffect entityEffect = EntityEffect.getAssetMap().getAsset(this.effectId);
         if (entityEffect != null) {
            Ref<EntityStore> ref = context.getEntity();
            Ref<EntityStore> targetRef = this.entityTarget.getEntity(context, ref);
            if (targetRef != null && targetRef.isValid()) {
               CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

               assert commandBuffer != null;

               EffectControllerComponent effectControllerComponent = commandBuffer.getComponent(targetRef, EffectControllerComponent.getComponentType());
               if (effectControllerComponent != null) {
                  effectControllerComponent.addEffect(targetRef, entityEffect, commandBuffer);
               }
            }
         }
      }
   }

   @Override
   protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ApplyEffectInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ApplyEffectInteraction p = (com.hypixel.hytale.protocol.ApplyEffectInteraction)packet;
      p.effectId = EntityEffect.getAssetMap().getIndex(this.effectId);
      p.entityTarget = this.entityTarget.toProtocol();
   }

   @Nonnull
   @Override
   public String toString() {
      return "ApplyEffectInteraction{effectId='" + this.effectId + "', entityTarget=" + this.entityTarget + "} " + super.toString();
   }
}
