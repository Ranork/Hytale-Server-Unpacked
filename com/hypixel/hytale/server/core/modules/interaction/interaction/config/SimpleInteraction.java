package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.StringTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Label;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SimpleInteraction extends Interaction {
   public static final BuilderCodec<SimpleInteraction> CODEC = BuilderCodec.builder(SimpleInteraction.class, SimpleInteraction::new, Interaction.ABSTRACT_CODEC)
      .documentation(
         "A interaction that does nothing other than base interaction features. Can be used for simple delays or triggering animations in between other interactions."
      )
      .<String>appendInherited(
         new KeyedCodec<>("Next", Interaction.CHILD_ASSET_CODEC),
         (interaction, s) -> interaction.next = s,
         interaction -> interaction.next,
         (interaction, parent) -> interaction.next = parent.next
      )
      .documentation("The interactions to run when this interaction succeeds.")
      .addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Failed", Interaction.CHILD_ASSET_CODEC),
         (interaction, s) -> interaction.failed = s,
         interaction -> interaction.failed,
         (interaction, parent) -> interaction.failed = parent.failed
      )
      .documentation("The interactions to run when this interaction fails.")
      .addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   private static final StringTag TAG_NEXT = StringTag.of("Next");
   private static final StringTag TAG_FAILED = StringTag.of("Failed");
   private static final int FAILED_LABEL_INDEX = 0;
   @Nullable
   protected String next;
   @Nullable
   protected String failed;

   protected SimpleInteraction() {
   }

   public SimpleInteraction(String id) {
      super(id);
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.None;
   }

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      if (context.getState().state == InteractionState.Failed && context.hasLabels()) {
         context.jump(context.getLabel(0));
      }
   }

   @Override
   protected void simulateTick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      if (this.getWaitForDataFrom() == WaitForDataFrom.Server && context.getServerState() != null && context.getServerState().state == InteractionState.Failed) {
         context.getState().state = InteractionState.Failed;
      }

      this.tick0(firstRun, time, type, context, cooldownHandler);
   }

   @Override
   public void compile(@Nonnull OperationsBuilder builder) {
      if (this.next == null && this.failed == null) {
         builder.addOperation(this);
      } else {
         Label failedLabel = builder.createUnresolvedLabel();
         Label endLabel = builder.createUnresolvedLabel();
         builder.addOperation(this, failedLabel);
         if (this.next != null) {
            Interaction nextInteraction = Interaction.getInteractionOrUnknown(this.next);
            nextInteraction.compile(builder);
         }

         if (this.failed != null) {
            builder.jump(endLabel);
         }

         builder.resolveLabel(failedLabel);
         if (this.failed != null) {
            Interaction failedInteraction = Interaction.getInteractionOrUnknown(this.failed);
            failedInteraction.compile(builder);
         }

         builder.resolveLabel(endLabel);
      }
   }

   @Override
   public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
      return this.next != null && InteractionManager.walkInteraction(collector, context, TAG_NEXT, this.next)
         ? true
         : this.failed != null && InteractionManager.walkInteraction(collector, context, TAG_FAILED, this.failed);
   }

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.SimpleInteraction();
   }

   @Override
   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.SimpleInteraction p = (com.hypixel.hytale.protocol.SimpleInteraction)packet;
      p.next = Interaction.getInteractionIdOrUnknown(this.next);
      p.failed = Interaction.getInteractionIdOrUnknown(this.failed);
   }

   @Override
   public boolean needsRemoteSync() {
      return needsRemoteSync(this.next) || needsRemoteSync(this.failed);
   }

   @Override
   public String toString() {
      return "SimpleInteraction{next='" + this.next + "'failed='" + this.failed + "'} " + super.toString();
   }
}
