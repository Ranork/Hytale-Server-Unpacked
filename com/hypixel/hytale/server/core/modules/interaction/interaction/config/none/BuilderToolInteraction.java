package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import javax.annotation.Nonnull;

public class BuilderToolInteraction extends SimpleInteraction {
   @Nonnull
   public static final BuilderCodec<BuilderToolInteraction> CODEC = BuilderCodec.builder(
         BuilderToolInteraction.class, BuilderToolInteraction::new, SimpleInteraction.CODEC
      )
      .documentation("Runs a builder tool")
      .build();

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.BuilderToolInteraction();
   }

   @Override
   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Client;
   }

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      context.getState().state = context.getClientState().state;
      super.tick0(firstRun, time, type, context, cooldownHandler);
   }

   @Nonnull
   @Override
   public String toString() {
      return "BuilderToolInteraction{} " + super.toString();
   }
}
