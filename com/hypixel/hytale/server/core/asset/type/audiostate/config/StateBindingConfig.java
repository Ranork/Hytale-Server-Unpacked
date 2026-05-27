package com.hypixel.hytale.server.core.asset.type.audiostate.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.protocol.StateBinding;
import com.hypixel.hytale.protocol.StateDelta;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateBindingConfig {
   @Nonnull
   public static final BuilderCodec<StateBindingConfig> CODEC = BuilderCodec.builder(StateBindingConfig.class, StateBindingConfig::new)
      .append(new KeyedCodec<>("AudioState", Codec.STRING), (b, s) -> b.audioStateId = s, b -> b.audioStateId)
      .documentation("Id of the AudioState this binding subscribes to.")
      .add()
      .<StateDeltaConfig[]>append(new KeyedCodec<>("Deltas", StateDeltaConfig.CODEC_ARRAY), (b, d) -> b.deltas = d, b -> b.deltas)
      .documentation("Per-state-value deltas. Unlisted values default to zero (no effect).")
      .add()
      .build();
   @Nonnull
   public static final ArrayCodec<StateBindingConfig> CODEC_ARRAY = new ArrayCodec<>(CODEC, StateBindingConfig[]::new);
   @Nullable
   protected String audioStateId;
   @Nullable
   protected StateDeltaConfig[] deltas;
   transient int audioStateIndex = -1;

   @Nonnull
   public StateBinding toPacket() {
      StateBinding packet = new StateBinding();
      packet.audioStateIndex = this.audioStateIndex;
      if (this.deltas != null) {
         StateDelta[] protoDeltas = new StateDelta[this.deltas.length];

         for (int i = 0; i < this.deltas.length; i++) {
            protoDeltas[i] = this.deltas[i].toPacket();
         }

         packet.deltas = protoDeltas;
      }

      return packet;
   }
}
