package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BrushOrigin;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolBrushOriginArg;
import javax.annotation.Nonnull;

public class BrushOriginArg extends ToolArg<BrushOrigin> {
   public static final EnumCodec<BrushOrigin> BRUSH_ORIGIN_CODEC = new EnumCodec<>(BrushOrigin.class);
   public static final BuilderCodec<BrushOriginArg> CODEC = BuilderCodec.builder(BrushOriginArg.class, BrushOriginArg::new, ToolArg.DEFAULT_CODEC)
      .addField(new KeyedCodec<>("Default", BRUSH_ORIGIN_CODEC), (originArg, o) -> originArg.value = o, originArg -> originArg.value)
      .build();

   public BrushOriginArg() {
   }

   public BrushOriginArg(BrushOrigin value) {
      this.value = value;
   }

   @Nonnull
   @Override
   public Codec<BrushOrigin> getCodec() {
      return BRUSH_ORIGIN_CODEC;
   }

   @Nonnull
   public BrushOrigin fromString(@Nonnull String str) {
      return BrushOrigin.valueOf(str);
   }

   @Nonnull
   public BuilderToolBrushOriginArg toBrushOriginArgPacket() {
      return new BuilderToolBrushOriginArg(this.value);
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.BrushOrigin;
      packet.brushOriginArg = this.toBrushOriginArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "BrushOriginArg{} " + super.toString();
   }
}
