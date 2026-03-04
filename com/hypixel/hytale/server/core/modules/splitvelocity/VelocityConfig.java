package com.hypixel.hytale.server.core.modules.splitvelocity;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.VelocityThresholdStyle;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class VelocityConfig implements NetworkSerializable<com.hypixel.hytale.protocol.VelocityConfig> {
   @Nonnull
   public static BuilderCodec<VelocityConfig> CODEC = BuilderCodec.builder(VelocityConfig.class, VelocityConfig::new)
      .appendInherited(
         new KeyedCodec<>("GroundResistance", Codec.FLOAT),
         (o, i) -> o.groundResistance = i,
         o -> o.groundResistance,
         (o, p) -> o.groundResistance = p.groundResistance
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("GroundResistanceMax", Codec.FLOAT),
         (o, i) -> o.groundResistanceMax = i,
         o -> o.groundResistanceMax,
         (o, p) -> o.groundResistanceMax = p.groundResistanceMax
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("AirResistance", Codec.FLOAT), (o, i) -> o.airResistance = i, o -> o.airResistance, (o, p) -> o.airResistance = p.airResistance
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("AirResistanceMax", Codec.FLOAT),
         (o, i) -> o.airResistanceMax = i,
         o -> o.airResistanceMax,
         (o, p) -> o.airResistance = p.airResistanceMax
      )
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<Float>appendInherited(new KeyedCodec<>("Threshold", Codec.FLOAT), (o, i) -> o.threshold = i, o -> o.threshold, (o, p) -> o.threshold = p.threshold)
      .documentation("The threshold of the velocity's length before resistance starts to transition to the Max values (if set)")
      .add()
      .<VelocityThresholdStyle>appendInherited(
         new KeyedCodec<>("Style", new EnumCodec<>(VelocityThresholdStyle.class)), (o, i) -> o.style = i, o -> o.style, (o, p) -> o.style = p.style
      )
      .documentation("Whether the transition from min to max resistance values should be linear or not")
      .add()
      .build();
   private float groundResistance = 0.82F;
   private float groundResistanceMax = 0.0F;
   private float airResistance = 0.96F;
   private float airResistanceMax = 0.0F;
   private float threshold = 1.0F;
   private VelocityThresholdStyle style = VelocityThresholdStyle.Linear;

   public float getGroundResistance() {
      return this.groundResistance;
   }

   public float getAirResistance() {
      return this.airResistance;
   }

   public float getGroundResistanceMax() {
      return this.groundResistanceMax;
   }

   public float getAirResistanceMax() {
      return this.airResistanceMax;
   }

   public float getThreshold() {
      return this.threshold;
   }

   public VelocityThresholdStyle getStyle() {
      return this.style;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.VelocityConfig toPacket() {
      return new com.hypixel.hytale.protocol.VelocityConfig(
         this.groundResistance, this.groundResistanceMax, this.airResistance, this.airResistanceMax, this.threshold, this.style
      );
   }
}
