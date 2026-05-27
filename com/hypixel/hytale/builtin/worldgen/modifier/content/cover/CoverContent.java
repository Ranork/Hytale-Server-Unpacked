package com.hypixel.hytale.builtin.worldgen.modifier.content.cover;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.BlockEntry;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.BlockMask;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.HeightMask;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.NoiseMask;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;

public abstract class CoverContent implements Content {
   public static final BuilderCodec<CoverContent> CODEC = BuilderCodec.abstractBuilder(CoverContent.class)
      .append(new KeyedCodec<>("Entries", new ArrayCodec<>(CoverContent.Entry.CODEC, CoverContent.Entry[]::new)), (c, v) -> c.entries = v, c -> c.entries)
      .documentation("A list of blocks that can be placed as a cover")
      .add()
      .<NoiseMask>append(new KeyedCodec<>("NoiseMask", NoiseMask.CODEC), (c, v) -> c.noiseMask = v, c -> c.noiseMask)
      .documentation("A mask used to determine where the cover should be placed based on noise")
      .add()
      .<HeightMask>append(new KeyedCodec<>("HeightMask", HeightMask.CODEC), (c, v) -> c.heightMask = v, c -> c.heightMask)
      .documentation("A mask used to determine where the cover should be placed based on height")
      .add()
      .<BlockMask>append(new KeyedCodec<>("ParentMask", BlockMask.CODEC), (c, v) -> c.parentMask = v, c -> c.parentMask)
      .documentation("A mask used to filter which blocks can be covered")
      .add()
      .<Double>append(new KeyedCodec<>("Chance", Codec.DOUBLE), (c, v) -> c.chance = v, c -> c.chance)
      .addValidator(Validators.range(0.0, 1.0))
      .documentation("The random chance that the cover will be placed at a given position")
      .add()
      .build();
   protected CoverContent.Entry[] entries = CoverContent.Entry.EMPTY_ARRAY;
   protected NoiseMask noiseMask = NoiseMask.DEFAULT;
   protected HeightMask heightMask = HeightMask.DEFAULT;
   protected BlockMask parentMask = BlockMask.REPLACE_SOLID;
   protected double chance = 1.0;

   protected <T> IWeightedMap<T> buildEntries(@Nonnull T[] empty, @Nonnull BiFunction<CoverContent.Entry, BlockFluidEntry, T> constructor) {
      WeightedMap.Builder<T> map = WeightedMap.builder(empty);

      for (CoverContent.Entry entry : this.entries) {
         int blockId = BlockType.getAssetMap().getIndex(entry.block());
         BlockFluidEntry blockFluid = new BlockFluidEntry(blockId, 0, 0);
         map.put(constructor.apply(entry, blockFluid), entry.weight());
      }

      return map.build();
   }

   public static class Entry extends BlockEntry {
      public static final BuilderCodec<CoverContent.Entry> CODEC = BuilderCodec.builder(CoverContent.Entry.class, CoverContent.Entry::new, BlockEntry.CODEC)
         .documentation("Define a block to be placed as a cover")
         .<Integer>append(new KeyedCodec<>("Offset", Codec.INTEGER), (c, v) -> c.offset = v, c -> c.offset)
         .documentation("The vertical offset from the surface for this cover entry")
         .add()
         .build();
      public static final CoverContent.Entry[] EMPTY_ARRAY = new CoverContent.Entry[0];
      protected int offset = 0;
   }
}
