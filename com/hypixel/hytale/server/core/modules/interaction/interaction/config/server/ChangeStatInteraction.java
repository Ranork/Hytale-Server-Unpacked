package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ChangeStatInteraction extends ChangeStatBaseInteraction {
   public static final BuilderCodec<ChangeStatInteraction> CODEC = BuilderCodec.builder(
         ChangeStatInteraction.class, ChangeStatInteraction::new, ChangeStatBaseInteraction.CODEC
      )
      .documentation("Changes the given stats.")
      .build();

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      Ref<EntityStore> ref = context.getEntity();
      EntityStatMap entityStatMapComponent = commandBuffer.getComponent(ref, EntityStatMap.getComponentType());
      if (entityStatMapComponent != null) {
         entityStatMapComponent.processStatChanges(EntityStatMap.Predictable.SELF, this.entityStats, this.valueType, this.changeStatBehaviour);
      }
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ChangeStatInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ChangeStatInteraction p = (com.hypixel.hytale.protocol.ChangeStatInteraction)packet;
      p.statModifiers = this.entityStats;
      p.valueType = this.valueType;
      p.changeStatBehaviour = this.changeStatBehaviour;
      p.entityTarget = this.entityTarget.toProtocol();
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChangeStatInteraction{}" + super.toString();
   }
}
