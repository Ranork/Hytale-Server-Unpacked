package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolIntArg;
import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;

public class IntArg extends ToolArg<Integer> {
   public static final BuilderCodec<IntArg> CODEC = BuilderCodec.builder(IntArg.class, IntArg::new, ToolArg.DEFAULT_CODEC)
      .addField(new KeyedCodec<>("Default", Codec.INTEGER), (intArg, d) -> intArg.value = d, intArg -> intArg.value)
      .addField(new KeyedCodec<>("Min", Codec.INTEGER), (intArg, d) -> intArg.min = d, intArg -> intArg.min)
      .addField(new KeyedCodec<>("Max", Codec.INTEGER), (intArg, d) -> intArg.max = d, intArg -> intArg.max)
      .build();
   protected int min;
   protected int max;

   public IntArg() {
   }

   public IntArg(int value, int min, int max) {
      this.value = value;
      this.min = min;
      this.max = max;
   }

   @Nonnull
   @Override
   public Codec<Integer> getCodec() {
      return Codec.INTEGER;
   }

   public int getMin() {
      return this.min;
   }

   public int getMax() {
      return this.max;
   }

   @Nonnull
   public Integer fromString(@Nonnull String str) throws ToolArgException {
      int value = Integer.parseInt(str);
      if (value >= this.min && value <= this.max) {
         return value;
      } else {
         throw new ToolArgException(
            Message.translation("server.builderTools.toolArgRangeError").param("value", value).param("min", this.min).param("max", this.max)
         );
      }
   }

   @Nonnull
   public BuilderToolIntArg toIntArgPacket() {
      return new BuilderToolIntArg(this.value, this.min, this.max);
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.Int;
      packet.intArg = this.toIntArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "IntArg{min=" + this.min + ", max=" + this.max + "} " + super.toString();
   }
}
