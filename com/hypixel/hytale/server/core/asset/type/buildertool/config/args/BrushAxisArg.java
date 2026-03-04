package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.packets.buildertools.BrushAxis;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolBrushAxisArg;
import javax.annotation.Nonnull;

public class BrushAxisArg extends ToolArg<BrushAxis> {
   public static final Codec<BrushAxis> BRUSH_AXIS_CODEC = new EnumCodec<>(BrushAxis.class);
   public static final BuilderCodec<BrushAxisArg> CODEC = BuilderCodec.builder(BrushAxisArg.class, BrushAxisArg::new, ToolArg.DEFAULT_CODEC)
      .append(new KeyedCodec<>("Default", BRUSH_AXIS_CODEC), (arg, o) -> arg.value = o, arg -> arg.value)
      .documentation("Represents the type of axis to be used when performing transformations on brushes")
      .addValidator(Validators.nonNull())
      .add()
      .build();

   public BrushAxisArg() {
   }

   public BrushAxisArg(BrushAxis value) {
      this.value = value;
   }

   @Nonnull
   @Override
   public Codec<BrushAxis> getCodec() {
      return BRUSH_AXIS_CODEC;
   }

   @Nonnull
   public BrushAxis fromString(@Nonnull String str) throws ToolArgException {
      return BrushAxis.valueOf(str);
   }

   @Nonnull
   public BuilderToolBrushAxisArg toBrushAxisArgPacket() {
      return new BuilderToolBrushAxisArg(this.value);
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.BrushAxis;
      packet.brushAxisArg = this.toBrushAxisArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "BrushAxisArg{} " + super.toString();
   }
}
