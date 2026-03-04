package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.StringTag;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReplaceInteraction extends Interaction {
   @Nonnull
   public static final BuilderCodec<ReplaceInteraction> CODEC = BuilderCodec.builder(
         ReplaceInteraction.class, ReplaceInteraction::new, Interaction.ABSTRACT_CODEC
      )
      .documentation("Runs the interaction defined by the interaction variables if defined.")
      .<String>appendInherited(
         new KeyedCodec<>("DefaultValue", RootInteraction.CHILD_ASSET_CODEC),
         (i, s) -> i.defaultValue = s,
         i -> i.defaultValue,
         (i, parent) -> i.defaultValue = parent.defaultValue
      )
      .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
      .add()
      .<String>appendInherited(new KeyedCodec<>("Var", Codec.STRING), (i, s) -> i.variable = s, i -> i.variable, (i, parent) -> i.variable = parent.variable)
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(new KeyedCodec<>("DefaultOk", Codec.BOOLEAN), (i, s) -> i.defaultOk = s, i -> i.defaultOk, (i, parent) -> i.defaultOk = parent.defaultOk)
      .add()
      .build();
   private static final StringTag TAG_DEFAULT = StringTag.of("Default");
   private static final StringTag TAG_VARS = StringTag.of("Vars");
   @Nullable
   protected String defaultValue;
   protected String variable;
   protected boolean defaultOk;

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.None;
   }

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      if (!Interaction.failed(context.getState().state)) {
         if (firstRun) {
            this.doReplace(context, true);
         }
      }
   }

   @Override
   protected void simulateTick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      if (!Interaction.failed(context.getState().state)) {
         if (firstRun) {
            this.doReplace(context, false);
         }
      }
   }

   private void doReplace(@Nonnull InteractionContext context, boolean log) {
      Map<String, String> vars = context.getInteractionVars();
      String next = vars == null ? null : vars.get(this.variable);
      if (next == null && !this.defaultOk && log) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).atMostEvery(1, TimeUnit.MINUTES))
            .log("Missing replacement interactions for interaction: %s for var %s on item %s", this.id, this.variable, context.getHeldItem());
      }

      if (next == null) {
         next = this.defaultValue;
      }

      if (next == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         RootInteraction nextInteraction = RootInteraction.getRootInteractionOrUnknown(next);
         context.getState().state = InteractionState.Finished;
         context.execute(nextInteraction);
      }
   }

   @Override
   public boolean needsRemoteSync() {
      return true;
   }

   @Override
   public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
      if (this.defaultValue != null
         && InteractionManager.walkInteractions(
            collector, context, TAG_DEFAULT, RootInteraction.getRootInteractionOrUnknown(this.defaultValue).getInteractionIds()
         )) {
         return true;
      } else {
         Map<String, String> vars = context.getInteractionVars();
         if (vars == null) {
            return false;
         } else {
            String interactionIds = vars.get(this.variable);
            return interactionIds == null
               ? false
               : InteractionManager.walkInteractions(
                  collector, context, TAG_VARS, RootInteraction.getRootInteractionOrUnknown(interactionIds).getInteractionIds()
               );
         }
      }
   }

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ReplaceInteraction();
   }

   @Override
   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ReplaceInteraction p = (com.hypixel.hytale.protocol.ReplaceInteraction)packet;
      p.defaultValue = RootInteraction.getRootInteractionIdOrUnknown(this.defaultValue);
      p.variable = this.variable;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReplaceInteraction{defaultValue='"
         + this.defaultValue
         + "', variable='"
         + this.variable
         + "', defaultOk="
         + this.defaultOk
         + "} "
         + super.toString();
   }
}
