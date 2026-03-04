package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import javax.annotation.Nonnull;

public class ChainFlagInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<ChainFlagInteraction> CODEC = BuilderCodec.builder(
         ChainFlagInteraction.class, ChainFlagInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Sets a flag on the given chain id that a Chaining interaction can optionally use to adjust what it'll execute.")
      .<String>appendInherited(new KeyedCodec<>("ChainId", Codec.STRING), (o, i) -> o.chainId = i, o -> o.chainId, (o, p) -> o.chainId = p.chainId)
      .addValidator(Validators.nonNull())
      .add()
      .<String>appendInherited(new KeyedCodec<>("Flag", Codec.STRING), (o, i) -> o.flag = i, o -> o.flag, (o, p) -> o.flag = p.flag)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected String chainId;
   protected String flag;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
   }

   @Nonnull
   @Override
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ChainFlagInteraction();
   }

   @Override
   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ChainFlagInteraction p = (com.hypixel.hytale.protocol.ChainFlagInteraction)packet;
      p.chainId = this.chainId;
      p.flag = this.flag;
   }
}
