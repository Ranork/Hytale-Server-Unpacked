package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolFloatArg;
import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;

public class FloatArg extends ToolArg<Float> {
   public static final BuilderCodec<FloatArg> CODEC = BuilderCodec.builder(FloatArg.class, FloatArg::new, ToolArg.DEFAULT_CODEC)
      .addField(new KeyedCodec<>("Default", Codec.DOUBLE), (floatArg, o) -> floatArg.value = o.floatValue(), floatArg -> (double)floatArg.value.floatValue())
      .addField(new KeyedCodec<>("Min", Codec.DOUBLE), (floatArg, o) -> floatArg.min = o.floatValue(), floatArg -> (double)floatArg.min)
      .addField(new KeyedCodec<>("Max", Codec.DOUBLE), (floatArg, o) -> floatArg.max = o.floatValue(), floatArg -> (double)floatArg.max)
      .build();
   protected float min;
   protected float max;

   public FloatArg() {
      this.value = 0.0F;
   }

   public FloatArg(float value, float min, float max) {
      this.value = value;
      this.min = min;
      this.max = max;
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   @Nonnull
   @Override
   public Codec<Float> getCodec() {
      return Codec.FLOAT;
   }

   @Nonnull
   public Float fromString(@Nonnull String str) throws ToolArgException {
      float value = Float.parseFloat(str);
      if (!(value < this.min) && !(value > this.max)) {
         return value;
      } else {
         throw new ToolArgException(
            Message.translation("server.builderTools.toolArgRangeError").param("value", value).param("min", this.min).param("max", this.max)
         );
      }
   }

   @Nonnull
   public BuilderToolFloatArg toFloatArgPacket() {
      return new BuilderToolFloatArg(this.value, this.min, this.max);
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.Float;
      packet.floatArg = this.toFloatArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "FloatArg{min=" + this.min + ", max=" + this.max + "} " + super.toString();
   }
}
