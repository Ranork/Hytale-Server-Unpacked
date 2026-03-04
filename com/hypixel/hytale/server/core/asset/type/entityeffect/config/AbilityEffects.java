package com.hypixel.hytale.server.core.asset.type.entityeffect.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;

public class AbilityEffects implements NetworkSerializable<com.hypixel.hytale.protocol.AbilityEffects> {
   @Nonnull
   public static final BuilderCodec<AbilityEffects> CODEC = BuilderCodec.builder(AbilityEffects.class, AbilityEffects::new)
      .appendInherited(
         new KeyedCodec<>("Disabled", InteractionModule.INTERACTION_TYPE_SET_CODEC),
         (entityEffect, s) -> entityEffect.disabled = s,
         entityEffect -> entityEffect.disabled,
         (entityEffect, parent) -> entityEffect.disabled = parent.disabled
      )
      .documentation("A collection of interaction types to become disabled while the entity effect affiliated with this ability effect is active")
      .add()
      .build();
   protected Set<InteractionType> disabled;

   public AbilityEffects(@Nonnull Set<InteractionType> disabled) {
      this.disabled = EnumSet.copyOf(disabled);
   }

   protected AbilityEffects() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AbilityEffects toPacket() {
      com.hypixel.hytale.protocol.AbilityEffects packet = new com.hypixel.hytale.protocol.AbilityEffects();
      packet.disabled = this.disabled == null ? null : this.disabled.toArray(InteractionType[]::new);
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AbilityEffects{, disabled=" + this.disabled + "}";
   }
}
