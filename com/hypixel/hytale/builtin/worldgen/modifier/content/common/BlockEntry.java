package com.hypixel.hytale.builtin.worldgen.modifier.content.common;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Codecs;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import javax.annotation.Nonnull;

public class BlockEntry {
   public static final BuilderCodec<BlockEntry> CODEC = BuilderCodec.builder(BlockEntry.class, BlockEntry::new)
      .documentation("Defines a weighted block entry used to randomly select a block type from a list of entries")
      .<String>append(new KeyedCodec<>("Block", Codecs.BLOCK_TYPE), (t, v) -> t.block = v, t -> t.block)
      .documentation("The block type to place")
      .add()
      .<Double>append(new KeyedCodec<>("Weight", Codec.DOUBLE), (t, v) -> t.weight = v, t -> t.weight)
      .addValidator(Validators.range(0.0, 100.0))
      .documentation("The random chance of this block type being chosen")
      .add()
      .build();
   public static final ArrayCodec<BlockEntry> ARRAY_CODEC = new ArrayCodec<>(CODEC, BlockEntry[]::new);
   public static final BlockEntry[] EMPTY_ARRAY = new BlockEntry[0];
   protected String block = "Empty";
   protected double weight = 1.0;

   @Nonnull
   public String block() {
      return this.block;
   }

   public double weight() {
      return this.weight;
   }

   public static IWeightedMap<BlockFluidEntry> build(@Nonnull BlockEntry[] entries) {
      WeightedMap.Builder<BlockFluidEntry> map = WeightedMap.builder(BlockFluidEntry.EMPTY_ARRAY);

      for (BlockEntry block : entries) {
         int blockId = BlockType.getAssetMap().getIndex(block.block());
         map.put(new BlockFluidEntry(blockId, 0, 0), block.weight());
      }

      return map.build();
   }
}
