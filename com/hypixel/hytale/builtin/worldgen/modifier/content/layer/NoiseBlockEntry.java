package com.hypixel.hytale.builtin.worldgen.modifier.content.layer;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Codecs;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.NoiseMask;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.ConstantNoiseProperty;
import com.hypixel.hytale.server.worldgen.util.NoiseBlockArray;
import javax.annotation.Nonnull;

public class NoiseBlockEntry {
   public static final BuilderCodec<NoiseBlockEntry> CODEC = BuilderCodec.builder(NoiseBlockEntry.class, NoiseBlockEntry::new)
      .documentation("Defines a block entry that is repeated based on a noise mask")
      .<String>append(new KeyedCodec<>("Block", Codecs.BLOCK_TYPE), (t, v) -> t.block = v, t -> t.block)
      .documentation("The block type of the entry")
      .add()
      .<NoiseMask>append(new KeyedCodec<>("Noise", NoiseMask.CODEC), (t, v) -> t.noise = v, t -> t.noise)
      .documentation("The noise mask used to determine where the block should be placed")
      .add()
      .<IntRange>append(new KeyedCodec<>("Repetitions", Codecs.INT_RANGE), (t, v) -> t.repetitions = v, t -> t.repetitions)
      .documentation("The number of times the block should be repeated")
      .add()
      .build();
   public static final ArrayCodec<NoiseBlockEntry> ARRAY_CODEC = new ArrayCodec<>(CODEC, NoiseBlockEntry[]::new);
   public static final NoiseBlockEntry[] EMPTY_ARRAY = new NoiseBlockEntry[0];
   protected static final IntRange DEFAULT_REPETITIONS = new IntRange(1, 1);
   protected static final NoiseBlockArray.Entry NOOP_ENTRY = new NoiseBlockArray.Entry(
      "Empty", BlockFluidEntry.EMPTY, DoubleRange.ZERO, ConstantNoiseProperty.DEFAULT_ZERO
   );
   protected String block = "Unknown";
   protected NoiseMask noise = NoiseMask.DEFAULT;
   protected IntRange repetitions = DEFAULT_REPETITIONS;

   @Nonnull
   public NoiseBlockArray.Entry buildEntry(@Nonnull SeedString<SeedStringResource> seed) {
      int blockId = BlockType.getAssetMap().getIndex(this.block);
      return blockId == 1
         ? NOOP_ENTRY
         : new NoiseBlockArray.Entry(
            this.block,
            new BlockFluidEntry(blockId, 0, 0),
            new DoubleRange.Normal(this.repetitions.getInclusiveMin(), this.repetitions.getInclusiveMax()),
            this.noise.buildNoise(seed)
         );
   }

   public static NoiseBlockArray buildArray(@Nonnull SeedString<SeedStringResource> seed, NoiseBlockEntry[] blocks) {
      NoiseBlockArray.Entry[] entries = new NoiseBlockArray.Entry[blocks.length];

      for (int i = 0; i < blocks.length; i++) {
         entries[i] = blocks[i].buildEntry(seed);
      }

      return new NoiseBlockArray(entries);
   }
}
