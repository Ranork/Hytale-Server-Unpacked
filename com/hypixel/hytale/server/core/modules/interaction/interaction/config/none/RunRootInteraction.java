package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import javax.annotation.Nonnull;

public class RunRootInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<RunRootInteraction> CODEC = BuilderCodec.builder(
         RunRootInteraction.class, RunRootInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Runs the given interaction root.")
      .<String>appendInherited(
         new KeyedCodec<>("RootInteraction", Codec.STRING),
         (o, i) -> o.rootInteraction = i,
         o -> o.rootInteraction,
         (o, p) -> o.rootInteraction = p.rootInteraction
      )
      .documentation("A reference to a root interaction to run")
      .addValidator(Validators.nonNull())
      .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   protected String rootInteraction;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      context.getState().state = InteractionState.Finished;
      context.execute(RootInteraction.getRootInteractionOrUnknown(this.rootInteraction));
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.RunRootInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.RunRootInteraction p = (com.hypixel.hytale.protocol.RunRootInteraction)packet;
      p.rootInteraction = RootInteraction.getRootInteractionIdOrUnknown(this.rootInteraction);
   }
}
