package com.hypixel.hytale.server.core.asset.type.audiostate.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.protocol.AmbienceStateWrite;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceStateWriteConfig {
   @Nonnull
   public static final BuilderCodec<AmbienceStateWriteConfig> CODEC = BuilderCodec.builder(AmbienceStateWriteConfig.class, AmbienceStateWriteConfig::new)
      .append(new KeyedCodec<>("AudioState", Codec.STRING), (w, s) -> w.audioStateId = s, w -> w.audioStateId)
      .documentation("AudioState id to write (must be Authority: Client).")
      .add()
      .<String>append(new KeyedCodec<>("Value", Codec.STRING), (w, s) -> w.valueName = s, w -> w.valueName)
      .documentation("State value name to set on activation.")
      .add()
      .<StateTransitionConfig>append(
         new KeyedCodec<>("TransitionOverride", StateTransitionConfig.CODEC), (w, t) -> w.transitionOverride = t, w -> w.transitionOverride
      )
      .documentation("Optional transition override (duration, curve, sync). Uses axis default if omitted.")
      .add()
      .build();
   @Nonnull
   public static final ArrayCodec<AmbienceStateWriteConfig> CODEC_ARRAY = new ArrayCodec<>(CODEC, AmbienceStateWriteConfig[]::new);
   @Nullable
   protected String audioStateId;
   @Nullable
   protected String valueName;
   @Nullable
   protected StateTransitionConfig transitionOverride;
   transient int audioStateIndex = -1;
   transient int valueIndex = -1;

   @Nonnull
   public AmbienceStateWrite toPacket() {
      AmbienceStateWrite packet = new AmbienceStateWrite();
      packet.audioStateIndex = this.audioStateIndex;
      packet.valueIndex = this.valueIndex;
      packet.transitionOverride = this.transitionOverride != null ? this.transitionOverride.toPacket() : null;
      return packet;
   }
}
