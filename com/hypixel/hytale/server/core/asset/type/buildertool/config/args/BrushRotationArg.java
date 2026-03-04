package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolRotationArg;
import javax.annotation.Nonnull;

public class BrushRotationArg extends ToolArg<Rotation> {
   public static final Codec<Rotation> ROTATION_CODEC = new EnumCodec<>(Rotation.class);
   public static final BuilderCodec<BrushRotationArg> CODEC = BuilderCodec.builder(BrushRotationArg.class, BrushRotationArg::new, ToolArg.DEFAULT_CODEC)
      .append(new KeyedCodec<>("Default", ROTATION_CODEC), (arg, o) -> arg.value = o, arg -> arg.value)
      .documentation("Represents the amount of rotation to be applied to a brush shape")
      .addValidator(Validators.nonNull())
      .add()
      .build();

   public BrushRotationArg() {
   }

   public BrushRotationArg(Rotation value) {
      this.value = value;
   }

   @Nonnull
   @Override
   public Codec<Rotation> getCodec() {
      return ROTATION_CODEC;
   }

   @Nonnull
   public Rotation fromString(@Nonnull String str) throws ToolArgException {
      return Rotation.valueOf(str);
   }

   @Nonnull
   public BuilderToolRotationArg toRotationArgPacket() {
      return new BuilderToolRotationArg(this.value);
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.Rotation;
      packet.rotationArg = this.toRotationArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "BrushRotationArg{} " + super.toString();
   }
}
