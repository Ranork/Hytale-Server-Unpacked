package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolMaskArg;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import javax.annotation.Nonnull;

public class MaskArg extends ToolArg<BlockMask> {
   public static final MaskArg EMPTY = new MaskArg(BlockMask.EMPTY, false);
   public static final BuilderCodec<MaskArg> CODEC = BuilderCodec.builder(MaskArg.class, MaskArg::new, ToolArg.DEFAULT_CODEC)
      .addField(new KeyedCodec<>("Default", BlockMask.CODEC), (maskArg, d) -> maskArg.value = d, maskArg -> maskArg.value)
      .build();

   public MaskArg() {
   }

   public MaskArg(BlockMask value) {
      this.value = value;
   }

   public MaskArg(BlockMask value, boolean required) {
      this.value = value;
      this.required = required;
   }

   @Nonnull
   @Override
   public Codec<BlockMask> getCodec() {
      return BlockMask.CODEC;
   }

   @Nonnull
   public BlockMask fromString(@Nonnull String str) {
      return BlockMask.parse(str);
   }

   @Nonnull
   public BuilderToolMaskArg toMaskArgPacket() {
      return new BuilderToolMaskArg(this.value.toString());
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.Mask;
      packet.maskArg = this.toMaskArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "MaskArg{} " + super.toString();
   }
}
