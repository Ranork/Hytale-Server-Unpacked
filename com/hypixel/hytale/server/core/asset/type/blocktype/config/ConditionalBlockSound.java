package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class ConditionalBlockSound implements NetworkSerializable<com.hypixel.hytale.protocol.ConditionalBlockSound> {
   public static final BuilderCodec<ConditionalBlockSound> CODEC = BuilderCodec.builder(ConditionalBlockSound.class, ConditionalBlockSound::new)
      .append(new KeyedCodec<>("SoundEventId", Codec.STRING), (cbs, s) -> cbs.soundEventId = s, cbs -> cbs.soundEventId)
      .addValidator(Validators.nonNull())
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .addValidator(SoundEventValidators.LOOPING)
      .add()
      .<String>append(new KeyedCodec<>("AmbienceFXId", Codec.STRING), (cbs, s) -> cbs.ambienceFXId = s, cbs -> cbs.ambienceFXId)
      .addValidator(Validators.nonNull())
      .addValidator(AmbienceFX.VALIDATOR_CACHE.getValidator())
      .add()
      .afterDecode(ConditionalBlockSound::processConfig)
      .build();
   protected String soundEventId;
   protected transient int soundEventIndex;
   protected String ambienceFXId;
   protected transient int ambienceFXIndex;

   public ConditionalBlockSound(String soundEventId, String ambienceFXId) {
      this.soundEventId = soundEventId;
      this.ambienceFXId = ambienceFXId;
   }

   protected ConditionalBlockSound() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ConditionalBlockSound toPacket() {
      com.hypixel.hytale.protocol.ConditionalBlockSound packet = new com.hypixel.hytale.protocol.ConditionalBlockSound();
      packet.soundEventIndex = this.soundEventIndex;
      packet.ambienceFXIndex = this.ambienceFXIndex;
      return packet;
   }

   public String getSoundEventId() {
      return this.soundEventId;
   }

   public String getAmbienceFXId() {
      return this.ambienceFXId;
   }

   protected void processConfig() {
      if (this.soundEventId != null) {
         this.soundEventIndex = SoundEvent.getAssetMap().getIndex(this.soundEventId);
      }

      if (this.ambienceFXId != null) {
         this.ambienceFXIndex = AmbienceFX.getAssetMap().getIndex(this.ambienceFXId);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ConditionalBlockSound{soundEventId='"
         + this.soundEventId
         + "', soundEventIndex="
         + this.soundEventIndex
         + ", ambienceFXId='"
         + this.ambienceFXId
         + "', ambienceFXIndex="
         + this.ambienceFXIndex
         + "}";
   }
}
