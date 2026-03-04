package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChainingInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class CancelChainInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<CancelChainInteraction> CODEC = BuilderCodec.builder(
         CancelChainInteraction.class, CancelChainInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Cancels an active chaining state for the given chain id.")
      .<String>appendInherited(new KeyedCodec<>("ChainId", Codec.STRING), (o, i) -> o.chainId = i, o -> o.chainId, (o, p) -> o.chainId = p.chainId)
      .documentation("The ID of the chain to cancel.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected String chainId;

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.CancelChainInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.CancelChainInteraction p = (com.hypixel.hytale.protocol.CancelChainInteraction)packet;
      p.chainId = this.chainId;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
   }

   @Override
   protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      ChainingInteraction.Data dataComponent = commandBuffer.ensureAndGetComponent(ref, ChainingInteraction.Data.getComponentType());
      dataComponent.getNamedMap().removeInt(this.chainId);
   }
}
