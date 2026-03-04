package com.hypixel.hytale.server.core.asset.type.ambiencefx.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.AmbienceFXAltitude;
import com.hypixel.hytale.protocol.AmbienceFXSoundPlay3D;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class AmbienceFXSound implements NetworkSerializable<com.hypixel.hytale.protocol.AmbienceFXSound> {
   public static final BuilderCodec<AmbienceFXSound> CODEC = BuilderCodec.builder(AmbienceFXSound.class, AmbienceFXSound::new)
      .append(
         new KeyedCodec<>("SoundEventId", Codec.STRING),
         (ambienceFXSound, o) -> ambienceFXSound.soundEventId = o,
         ambienceFXSound -> ambienceFXSound.soundEventId
      )
      .addValidator(Validators.nonNull())
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .addField(
         new KeyedCodec<>("Play3D", new EnumCodec<>(AmbienceFXSoundPlay3D.class)),
         (ambienceFXSound, o) -> ambienceFXSound.play3D = o,
         ambienceFXSound -> ambienceFXSound.play3D
      )
      .<String>append(
         new KeyedCodec<>("BlockSoundSetId", Codec.STRING),
         (ambienceFXSound, o) -> ambienceFXSound.blockSoundSetId = o,
         ambienceFXSound -> ambienceFXSound.blockSoundSetId
      )
      .addValidator(BlockSoundSet.VALIDATOR_CACHE.getValidator())
      .add()
      .addField(
         new KeyedCodec<>("Altitude", new EnumCodec<>(AmbienceFXAltitude.class)),
         (ambienceFXSound, o) -> ambienceFXSound.altitude = o,
         ambienceFXSound -> ambienceFXSound.altitude
      )
      .addField(
         new KeyedCodec<>("Frequency", ProtocolCodecs.RANGEF),
         (ambienceFXSound, o) -> ambienceFXSound.frequency = o,
         ambienceFXSound -> ambienceFXSound.frequency
      )
      .addField(new KeyedCodec<>("Radius", ProtocolCodecs.RANGE), (ambienceFXSound, o) -> ambienceFXSound.radius = o, ambienceFXSound -> ambienceFXSound.radius)
      .afterDecode(AmbienceFXSound::processConfig)
      .build();
   public static final Rangef DEFAULT_FREQUENCY = new Rangef(1.0F, 10.0F);
   public static final Range DEFAULT_RADIUS = new Range(0, 24);
   protected String soundEventId;
   protected transient int soundEventIndex;
   protected AmbienceFXSoundPlay3D play3D = AmbienceFXSoundPlay3D.Random;
   protected String blockSoundSetId;
   protected transient int blockSoundSetIndex;
   protected AmbienceFXAltitude altitude = AmbienceFXAltitude.Normal;
   protected Rangef frequency = DEFAULT_FREQUENCY;
   protected Range radius = DEFAULT_RADIUS;

   public AmbienceFXSound(
      String soundEventId, AmbienceFXSoundPlay3D play3D, String blockSoundSetId, AmbienceFXAltitude altitude, Rangef frequency, Range radius
   ) {
      this.soundEventId = soundEventId;
      this.play3D = play3D;
      this.blockSoundSetId = blockSoundSetId;
      this.altitude = altitude;
      this.frequency = frequency;
      this.radius = radius;
   }

   protected AmbienceFXSound() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AmbienceFXSound toPacket() {
      com.hypixel.hytale.protocol.AmbienceFXSound packet = new com.hypixel.hytale.protocol.AmbienceFXSound();
      packet.soundEventIndex = this.soundEventIndex;
      packet.play3D = this.play3D;
      packet.blockSoundSetIndex = this.blockSoundSetIndex;
      packet.altitude = this.altitude;
      packet.frequency = this.frequency;
      packet.radius = this.radius;
      return packet;
   }

   public String getSoundEventId() {
      return this.soundEventId;
   }

   public int getSoundEventIndex() {
      return this.soundEventIndex;
   }

   public AmbienceFXSoundPlay3D getPlay3D() {
      return this.play3D;
   }

   public String getBlockSoundSetId() {
      return this.blockSoundSetId;
   }

   public AmbienceFXAltitude getAltitude() {
      return this.altitude;
   }

   public Rangef getFrequency() {
      return this.frequency;
   }

   public Range getRadius() {
      return this.radius;
   }

   protected void processConfig() {
      if (this.soundEventId != null) {
         this.soundEventIndex = SoundEvent.getAssetMap().getIndex(this.soundEventId);
      }

      if (this.blockSoundSetId != null) {
         this.blockSoundSetIndex = BlockSoundSet.getAssetMap().getIndex(this.blockSoundSetId);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "AmbienceFXSound{soundEventId='"
         + this.soundEventId
         + "', soundEventIndex="
         + this.soundEventIndex
         + ", play3D="
         + this.play3D
         + ", blockSoundSetId='"
         + this.blockSoundSetId
         + "', blockSoundSetIndex="
         + this.blockSoundSetIndex
         + ", altitude="
         + this.altitude
         + ", frequency="
         + this.frequency
         + ", radius="
         + this.radius
         + "}";
   }
}
