package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import javax.annotation.Nonnull;

public class ToggleGliderInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<ToggleGliderInteraction> CODEC = BuilderCodec.builder(
         ToggleGliderInteraction.class, ToggleGliderInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Toggles Glider movement for the player.")
      .build();

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ToggleGliderInteraction();
   }

   @Nonnull
   @Override
   public String toString() {
      return "ToggleGliderInteraction{} " + super.toString();
   }
}
