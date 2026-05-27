package com.hypixel.hytale.server.core.asset.type.audiostate.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.StateDelta;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateDeltaConfig {
   @Nonnull
   public static final BuilderCodec<StateDeltaConfig> CODEC = BuilderCodec.builder(StateDeltaConfig.class, StateDeltaConfig::new)
      .metadata(UIDisplayMode.COMPACT)
      .<String>append(new KeyedCodec<>("Value", Codec.STRING), (d, s) -> d.valueName = s, d -> d.valueName)
      .documentation("State value name this delta applies to.")
      .add()
      .<Float>append(new KeyedCodec<>("VolumeDb", Codec.FLOAT), (d, f) -> d.volumeDb = f, d -> d.volumeDb)
      .addValidator(Validators.range(-144.0F, 10.0F))
      .documentation("Signed dB offset applied to voice gain when this state value is active. Ignored when Mute is true.")
      .add()
      .<Boolean>append(new KeyedCodec<>("Mute", Codec.BOOLEAN), (d, b) -> d.mute = b, d -> d.mute)
      .documentation("When true, forces the subscriber silent for this state value. Fading is still applied.")
      .add()
      .<Float>append(new KeyedCodec<>("PitchSemitones", Codec.FLOAT), (d, f) -> d.pitchSemitones = f, d -> d.pitchSemitones)
      .addValidator(Validators.range(-60.0F, 12.0F))
      .documentation(
         "Signed semitone offset applied to the subscriber's playback pitch when this state value is active. 0 = no change. 12 = one octave up, -12 = one octave down, -6 = half octave down (tritone), -60 = perceptually frozen / time-stop effect."
      )
      .add()
      .build();
   @Nonnull
   public static final ArrayCodec<StateDeltaConfig> CODEC_ARRAY = new ArrayCodec<>(CODEC, StateDeltaConfig[]::new);
   @Nullable
   protected String valueName;
   protected float volumeDb;
   protected boolean mute;
   protected float pitchSemitones;
   transient int valueIndex = -1;

   public StateDelta toPacket() {
      StateDelta packet = new StateDelta();
      packet.valueIndex = this.valueIndex;
      packet.volumeDb = this.volumeDb;
      packet.mute = this.mute;
      packet.pitchSemitones = this.pitchSemitones;
      return packet;
   }
}
