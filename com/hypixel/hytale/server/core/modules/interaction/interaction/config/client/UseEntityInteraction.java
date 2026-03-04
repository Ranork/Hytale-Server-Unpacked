package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class UseEntityInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<UseEntityInteraction> CODEC = BuilderCodec.builder(
         UseEntityInteraction.class, UseEntityInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Attempts to use the target entity, executing interactions on it if any.")
      .build();

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Client;
   }

   @Override
   protected final void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      InteractionSyncData chainData = context.getClientState();

      assert chainData != null;

      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> targetRef = commandBuffer.getStore().getExternalData().getRefFromNetworkId(chainData.entityId);
      if (targetRef != null && targetRef.isValid()) {
         Interactions interactionsComponent = commandBuffer.getComponent(targetRef, Interactions.getComponentType());
         if (interactionsComponent != null) {
            String interaction = interactionsComponent.getInteractionId(type);
            if (interaction == null) {
               context.getState().state = InteractionState.Failed;
            } else {
               context.execute(RootInteraction.getRootInteractionOrUnknown(interaction));
            }
         } else {
            context.getState().state = InteractionState.Failed;
         }
      } else {
         context.getState().state = InteractionState.Failed;
      }
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.UseEntityInteraction();
   }

   @Override
   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   @Override
   public String toString() {
      return "UseEntityInteraction{} " + super.toString();
   }
}
