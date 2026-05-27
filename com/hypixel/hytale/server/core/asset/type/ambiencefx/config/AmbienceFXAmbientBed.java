package com.hypixel.hytale.server.core.asset.type.ambiencefx.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.AudioUtil;
import com.hypixel.hytale.protocol.AmbienceTransitionSpeed;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.common.SoundFileValidators;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioStateResolver;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.StateBindingConfig;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFXAmbientBed implements NetworkSerializable<com.hypixel.hytale.protocol.AmbienceFXAmbientBed> {
   public static final BuilderCodec<AmbienceFXAmbientBed> CODEC = BuilderCodec.builder(AmbienceFXAmbientBed.class, AmbienceFXAmbientBed::new)
      .appendInherited(
         new KeyedCodec<>("Track", Codec.STRING),
         (ambienceFXAmbientBed, s) -> ambienceFXAmbientBed.track = s,
         ambienceFXAmbientBed -> ambienceFXAmbientBed.track,
         (ambienceFXAmbientBed, parent) -> ambienceFXAmbientBed.track = parent.track
      )
      .addValidator(Validators.nonNull())
      .addValidator(CommonAssetValidator.SOUNDS)
      .addValidator(SoundFileValidators.STEREO)
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("Volume", Codec.FLOAT),
         (ambienceFXAmbientBed, f) -> ambienceFXAmbientBed.decibels = f,
         ambienceFXAmbientBed -> ambienceFXAmbientBed.decibels,
         (ambienceFXAmbientBed, parent) -> ambienceFXAmbientBed.decibels = parent.decibels
      )
      .metadata(new UIEditor(new UIEditor.FormattedNumber(null, " dB", null)))
      .addValidator(Validators.range(-100.0F, 10.0F))
      .add()
      .<AmbienceTransitionSpeed>appendInherited(
         new KeyedCodec<>("TransitionSpeed", new EnumCodec<>(AmbienceTransitionSpeed.class)),
         (ambienceFXAmbientBed, e) -> ambienceFXAmbientBed.transitionSpeed = e,
         ambienceFXAmbientBed -> ambienceFXAmbientBed.transitionSpeed,
         (ambienceFXAmbientBed, parent) -> ambienceFXAmbientBed.transitionSpeed = parent.transitionSpeed
      )
      .documentation(
         "How quickly to transition to this ambient bed and fade out any ambient beds that are stopping. For fading out stopping ambient beds, faster transitions take priority. Fade-ins are already fast by default."
      )
      .add()
      .<StateBindingConfig[]>append(
         new KeyedCodec<>("StateBindings", StateBindingConfig.CODEC_ARRAY),
         (ambienceFXAmbientBed, v) -> ambienceFXAmbientBed.stateBindings = v,
         ambienceFXAmbientBed -> ambienceFXAmbientBed.stateBindings
      )
      .documentation("Subscribe this ambient bed to AudioState axes. Per-state volume deltas will apply as a result.")
      .add()
      .afterDecode(AmbienceFXAmbientBed::processConfig)
      .build();
   protected String track;
   protected float decibels;
   protected transient float volume = 1.0F;
   protected AmbienceTransitionSpeed transitionSpeed = AmbienceTransitionSpeed.Default;
   @Nullable
   protected StateBindingConfig[] stateBindings;

   public AmbienceFXAmbientBed(String track, float decibels, AmbienceTransitionSpeed transitionSpeed) {
      this.track = track;
      this.decibels = decibels;
      this.transitionSpeed = transitionSpeed;
   }

   protected AmbienceFXAmbientBed() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AmbienceFXAmbientBed toPacket() {
      com.hypixel.hytale.protocol.AmbienceFXAmbientBed packet = new com.hypixel.hytale.protocol.AmbienceFXAmbientBed();
      packet.track = this.track;
      packet.volume = this.volume;
      packet.transitionSpeed = this.transitionSpeed;
      packet.stateBindings = AudioStateResolver.toPacketArray(this.stateBindings);
      return packet;
   }

   @Nullable
   public StateBindingConfig[] getStateBindings() {
      return this.stateBindings;
   }

   public String getTrack() {
      return this.track;
   }

   public float getDecibels() {
      return this.decibels;
   }

   public float getVolume() {
      return this.volume;
   }

   public AmbienceTransitionSpeed getTransitionSpeed() {
      return this.transitionSpeed;
   }

   protected void processConfig() {
      this.volume = AudioUtil.decibelsToLinearGain(this.decibels);
   }

   @Nonnull
   @Override
   public String toString() {
      return "AmbienceFXAmbientBed{track='"
         + this.track
         + "', decibels="
         + this.decibels
         + ", volume="
         + this.volume
         + ", transitionSpeed="
         + this.transitionSpeed
         + "}";
   }
}
