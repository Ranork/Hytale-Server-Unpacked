package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.CollectorTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SerialInteraction extends Interaction {
   @Nonnull
   public static final BuilderCodec<SerialInteraction> CODEC = BuilderCodec.builder(
         SerialInteraction.class, SerialInteraction::new, BuilderCodec.abstractBuilder(Interaction.class).build()
      )
      .documentation("Runs the given interactions in order.")
      .<String[]>appendInherited(
         new KeyedCodec<>("Interactions", CHILD_ASSET_CODEC_ARRAY),
         (o, i) -> o.interactions = i,
         o -> o.interactions,
         (o, p) -> o.interactions = p.interactions
      )
      .documentation("A list of interactions to run. They will be executed in the order specified sequentially.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonNullArrayElements())
      .add()
      .build();
   protected String[] interactions;

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      throw new IllegalStateException("Should not be reached");
   }

   @Override
   protected void simulateTick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      throw new IllegalStateException("Should not be reached");
   }

   @Override
   public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
      for (int i = 0; i < this.interactions.length; i++) {
         if (InteractionManager.walkInteraction(collector, context, SerialInteraction.SerialTag.of(i), this.interactions[i])) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void compile(@Nonnull OperationsBuilder builder) {
      for (String interaction : this.interactions) {
         Interaction.getInteractionOrUnknown(interaction).compile(builder);
      }
   }

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.SerialInteraction();
   }

   @Override
   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.SerialInteraction p = (com.hypixel.hytale.protocol.SerialInteraction)packet;
      int[] serialInteractions = p.serialInteractions = new int[this.interactions.length];

      for (int i = 0; i < this.interactions.length; i++) {
         serialInteractions[i] = Interaction.getInteractionIdOrUnknown(this.interactions[i]);
      }
   }

   @Override
   public boolean needsRemoteSync() {
      return false;
   }

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.None;
   }

   private static class SerialTag implements CollectorTag {
      private final int index;

      private SerialTag(int index) {
         this.index = index;
      }

      public int getIndex() {
         return this.index;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            SerialInteraction.SerialTag that = (SerialInteraction.SerialTag)o;
            return this.index == that.index;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.index;
      }

      @Nonnull
      @Override
      public String toString() {
         return "SerialTag{index=" + this.index + "}";
      }

      @Nonnull
      public static SerialInteraction.SerialTag of(int index) {
         return new SerialInteraction.SerialTag(index);
      }
   }
}
