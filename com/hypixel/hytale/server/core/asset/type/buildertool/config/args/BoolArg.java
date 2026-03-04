package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolBoolArg;
import javax.annotation.Nonnull;

public class BoolArg extends ToolArg<Boolean> {
   public static final BuilderCodec<BoolArg> CODEC = BuilderCodec.builder(BoolArg.class, BoolArg::new, ToolArg.DEFAULT_CODEC)
      .addField(new KeyedCodec<>("Default", Codec.BOOLEAN), (boolArg, d) -> boolArg.value = d, boolArg -> boolArg.value)
      .build();

   public BoolArg() {
   }

   public BoolArg(boolean value) {
      this.value = value;
   }

   @Nonnull
   @Override
   public Codec<Boolean> getCodec() {
      return Codec.BOOLEAN;
   }

   @Nonnull
   public Boolean fromString(@Nonnull String str) {
      return Boolean.valueOf(str);
   }

   @Nonnull
   public BuilderToolBoolArg toBoolArgPacket() {
      return new BuilderToolBoolArg(this.value);
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.Bool;
      packet.boolArg = this.toBoolArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "BoolArg{} " + super.toString();
   }
}
