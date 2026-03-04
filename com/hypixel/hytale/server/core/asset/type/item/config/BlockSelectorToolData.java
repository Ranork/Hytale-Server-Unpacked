package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class BlockSelectorToolData implements NetworkSerializable<com.hypixel.hytale.protocol.BlockSelectorToolData> {
   public static final BuilderCodec<BlockSelectorToolData> CODEC = BuilderCodec.builder(BlockSelectorToolData.class, BlockSelectorToolData::new)
      .append(new KeyedCodec<>("DurabilityLossOnUse", Codec.DOUBLE), (data, x) -> data.durabilityLossOnUse = x.floatValue(), data -> data.durabilityLossOnUse)
      .add()
      .build();
   protected double durabilityLossOnUse;

   protected BlockSelectorToolData() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockSelectorToolData toPacket() {
      com.hypixel.hytale.protocol.BlockSelectorToolData packet = new com.hypixel.hytale.protocol.BlockSelectorToolData();
      packet.durabilityLossOnUse = (float)this.durabilityLossOnUse;
      return packet;
   }

   public double getDurabilityLossOnUse() {
      return this.durabilityLossOnUse;
   }
}
