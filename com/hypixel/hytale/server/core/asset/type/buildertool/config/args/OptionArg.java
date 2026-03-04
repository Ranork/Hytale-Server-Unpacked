package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOptionArg;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class OptionArg extends ToolArg<String> {
   public static final BuilderCodec<OptionArg> CODEC = BuilderCodec.builder(OptionArg.class, OptionArg::new, ToolArg.DEFAULT_CODEC)
      .addField(new KeyedCodec<>("Default", Codec.STRING), (optionArg, o) -> optionArg.value = o, optionArg -> optionArg.value)
      .addField(new KeyedCodec<>("Options", Codec.STRING_ARRAY), (optionArg, o) -> optionArg.options = o, optionArg -> optionArg.options)
      .build();
   protected String[] options;

   public OptionArg() {
   }

   public OptionArg(String value, String[] options) {
      this.value = value;
      this.options = options;
   }

   @Nonnull
   @Override
   public Codec<String> getCodec() {
      return Codec.STRING;
   }

   @Nonnull
   public String fromString(@Nonnull String str) {
      for (String option : this.options) {
         if (str.equalsIgnoreCase(option)) {
            return option;
         }
      }

      try {
         int index = Integer.parseInt(str);
         if (index >= 0 && index < this.options.length) {
            return this.options[index];
         }
      } catch (NumberFormatException var6) {
      }

      throw new IllegalArgumentException();
   }

   @Nonnull
   public BuilderToolOptionArg toOptionArgPacket() {
      return new BuilderToolOptionArg(this.value, this.options);
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.Option;
      packet.optionArg = this.toOptionArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "OptionArg{options=" + Arrays.toString((Object[])this.options) + "} " + super.toString();
   }
}
