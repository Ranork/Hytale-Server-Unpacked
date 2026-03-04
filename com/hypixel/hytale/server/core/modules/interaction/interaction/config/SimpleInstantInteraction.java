package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import javax.annotation.Nonnull;

public abstract class SimpleInstantInteraction extends SimpleInteraction {
   public static final BuilderCodec<SimpleInstantInteraction> CODEC = BuilderCodec.abstractBuilder(SimpleInstantInteraction.class, SimpleInteraction.CODEC)
      .build();

   public SimpleInstantInteraction(String id) {
      super(id);
   }

   protected SimpleInstantInteraction() {
   }

   @Override
   protected final void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      if (firstRun) {
         this.firstRun(type, context, cooldownHandler);
         super.tick0(firstRun, time, type, context, cooldownHandler);
      }
   }

   @Override
   protected void simulateTick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      if (firstRun) {
         this.simulateFirstRun(type, context, cooldownHandler);
         if (this.getWaitForDataFrom() == WaitForDataFrom.Server
            && context.getServerState() != null
            && context.getServerState().state == InteractionState.Failed) {
            context.getState().state = InteractionState.Failed;
         }

         super.tick0(firstRun, time, type, context, cooldownHandler);
      }
   }

   protected abstract void firstRun(@Nonnull InteractionType var1, @Nonnull InteractionContext var2, @Nonnull CooldownHandler var3);

   protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      this.firstRun(type, context, cooldownHandler);
   }

   @Override
   public String toString() {
      return "SimpleInstantInteraction{} " + super.toString();
   }
}
