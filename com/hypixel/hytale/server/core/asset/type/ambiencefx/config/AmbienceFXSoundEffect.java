package com.hypixel.hytale.server.core.asset.type.ambiencefx.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.equalizereffect.config.EqualizerEffect;
import com.hypixel.hytale.server.core.asset.type.reverbeffect.config.ReverbEffect;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFXSoundEffect implements NetworkSerializable<com.hypixel.hytale.protocol.AmbienceFXSoundEffect> {
   public static final BuilderCodec<AmbienceFXSoundEffect> CODEC = BuilderCodec.builder(AmbienceFXSoundEffect.class, AmbienceFXSoundEffect::new)
      .appendInherited(
         new KeyedCodec<>("ReverbEffectId", Codec.STRING),
         (ambienceFXSoundEffect, s) -> ambienceFXSoundEffect.reverbEffectId = s,
         ambienceFXSoundEffect -> ambienceFXSoundEffect.reverbEffectId,
         (ambienceFXSoundEffect, parent) -> ambienceFXSoundEffect.reverbEffectId = parent.reverbEffectId
      )
      .addValidator(ReverbEffect.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("EqualizerEffectId", Codec.STRING),
         (ambienceFXSoundEffect, s) -> ambienceFXSoundEffect.equalizerEffectId = s,
         ambienceFXSoundEffect -> ambienceFXSoundEffect.equalizerEffectId,
         (ambienceFXSoundEffect, parent) -> ambienceFXSoundEffect.equalizerEffectId = parent.equalizerEffectId
      )
      .addValidator(EqualizerEffect.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("IsInstant", Codec.BOOLEAN),
         (ambienceFXSoundEffect, b) -> ambienceFXSoundEffect.isInstant = b,
         ambienceFXSoundEffect -> ambienceFXSoundEffect.isInstant,
         (ambienceFXSoundEffect, parent) -> ambienceFXSoundEffect.isInstant = parent.isInstant
      )
      .add()
      .afterDecode(AmbienceFXSoundEffect::processConfig)
      .build();
   @Nullable
   protected String reverbEffectId;
   protected transient int reverbEffectIndex = 0;
   @Nullable
   protected String equalizerEffectId;
   protected transient int equalizerEffectIndex = 0;
   protected boolean isInstant = false;

   public AmbienceFXSoundEffect(@Nullable String reverbEffectId, @Nullable String equalizerEffectId, boolean isInstant) {
      this.reverbEffectId = reverbEffectId;
      this.equalizerEffectId = equalizerEffectId;
      this.isInstant = isInstant;
   }

   protected AmbienceFXSoundEffect() {
   }

   protected void processConfig() {
      if (this.reverbEffectId != null) {
         this.reverbEffectIndex = ReverbEffect.getAssetMap().getIndex(this.reverbEffectId);
      }

      if (this.equalizerEffectId != null) {
         this.equalizerEffectIndex = EqualizerEffect.getAssetMap().getIndex(this.equalizerEffectId);
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AmbienceFXSoundEffect toPacket() {
      com.hypixel.hytale.protocol.AmbienceFXSoundEffect packet = new com.hypixel.hytale.protocol.AmbienceFXSoundEffect();
      packet.reverbEffectIndex = this.reverbEffectIndex;
      packet.equalizerEffectIndex = this.equalizerEffectIndex;
      packet.isInstant = this.isInstant;
      return packet;
   }

   @Nullable
   public String getReverbEffectId() {
      return this.reverbEffectId;
   }

   public int getReverbEffectIndex() {
      return this.reverbEffectIndex;
   }

   @Nullable
   public String getEqualizerEffectId() {
      return this.equalizerEffectId;
   }

   public int getEqualizerEffectIndex() {
      return this.equalizerEffectIndex;
   }

   public boolean isInstant() {
      return this.isInstant;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AmbienceFXSoundEffect{reverbEffectId='"
         + this.reverbEffectId
         + "', equalizerEffectId='"
         + this.equalizerEffectId
         + "', isInstant="
         + this.isInstant
         + "}";
   }
}
