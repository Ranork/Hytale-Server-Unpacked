package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolStringArg;
import javax.annotation.Nonnull;

public class StringArg extends ToolArg<String> {
   public static final StringArg[] EMPTY_ARRAY = new StringArg[0];
   public static final BuilderCodec<StringArg> CODEC = BuilderCodec.builder(StringArg.class, StringArg::new, ToolArg.DEFAULT_CODEC)
      .addField(new KeyedCodec<>("Default", Codec.STRING), (stringArg, d) -> stringArg.value = d, stringArg -> stringArg.value)
      .build();

   public StringArg() {
   }

   public StringArg(String value) {
      this.value = value;
   }

   @Nonnull
   @Override
   public Codec<String> getCodec() {
      return Codec.STRING;
   }

   @Nonnull
   public String fromString(@Nonnull String str) {
      return str;
   }

   @Nonnull
   public BuilderToolStringArg toStringArgPacket() {
      return new BuilderToolStringArg(this.value);
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.String;
      packet.stringArg = this.toStringArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "StringArg{} " + super.toString();
   }
}
