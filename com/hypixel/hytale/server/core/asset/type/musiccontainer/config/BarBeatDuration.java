package com.hypixel.hytale.server.core.asset.type.musiccontainer.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import javax.annotation.Nonnull;

public class BarBeatDuration {
   @Nonnull
   public static final BuilderCodec<BarBeatDuration> CODEC = BuilderCodec.builder(BarBeatDuration.class, BarBeatDuration::new)
      .metadata(UIDisplayMode.COMPACT)
      .append(new KeyedCodec<>("Bars", Codec.INTEGER), (d, v) -> d.bars = v, d -> d.bars)
      .add()
      .append(new KeyedCodec<>("Beats", Codec.INTEGER), (d, v) -> d.beats = v, d -> d.beats)
      .add()
      .<Float>append(new KeyedCodec<>("Ms", Codec.FLOAT), (d, v) -> d.ms = v, d -> d.ms)
      .documentation(
         "Additional millisecond offset summed with bar/beat components. Allows pure ms authoring (e.g. { Ms: 1234 }) or fine-tuning bar/beat positions without a divide."
      )
      .add()
      .build();
   public int bars;
   public int beats;
   public float ms;

   public float toMs(float bpm, int beatsPerBar) {
      if (bpm <= 0.0F) {
         return this.ms;
      } else {
         float beatDuration = 60000.0F / bpm;
         return this.bars * beatsPerBar * beatDuration + this.beats * beatDuration + this.ms;
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BarBeatDuration toProtocol() {
      com.hypixel.hytale.protocol.BarBeatDuration p = new com.hypixel.hytale.protocol.BarBeatDuration();
      p.bars = this.bars;
      p.beats = this.beats;
      p.ms = this.ms;
      return p;
   }
}
