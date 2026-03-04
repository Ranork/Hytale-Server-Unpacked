package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolBlockArg;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import javax.annotation.Nonnull;

public class BlockArg extends ToolArg<BlockPattern> {
   public static final BlockArg[] EMPTY_ARRAY = new BlockArg[0];
   public static final BuilderCodec<BlockArg> CODEC = BuilderCodec.builder(BlockArg.class, BlockArg::new, ToolArg.DEFAULT_CODEC)
      .addField(new KeyedCodec<>("Default", BlockPattern.CODEC), (blockArg, d) -> blockArg.value = d, blockArg -> blockArg.value)
      .addField(new KeyedCodec<>("AllowPattern", Codec.BOOLEAN), (blockArg, d) -> blockArg.allowPattern = d, blockArg -> blockArg.allowPattern)
      .build();
   protected boolean allowPattern;

   public BlockArg() {
   }

   public BlockArg(BlockPattern value, boolean allowPattern) {
      this.value = value;
      this.allowPattern = allowPattern;
   }

   @Nonnull
   @Override
   public Codec<BlockPattern> getCodec() {
      return BlockPattern.CODEC;
   }

   @Nonnull
   public BlockPattern fromString(@Nonnull String str) {
      return BlockPattern.parse(str);
   }

   @Nonnull
   public BuilderToolBlockArg toBlockArgPacket() {
      return new BuilderToolBlockArg(this.value.toString(), this.allowPattern);
   }

   @Override
   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.Block;
      packet.blockArg = this.toBlockArgPacket();
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockArg{allowPattern=" + this.allowPattern + "} " + super.toString();
   }
}
