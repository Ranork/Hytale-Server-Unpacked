package com.hypixel.hytale.builtin.worldgen.modifier.content.common;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.worldgen.modifier.content.Codecs;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.ConstantBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import com.hypixel.hytale.server.worldgen.util.condition.FilteredBlockFluidCondition;
import com.hypixel.hytale.server.worldgen.util.condition.HashSetBlockFluidCondition;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import javax.annotation.Nonnull;

public class BlockMask {
   public static final BuilderCodec<BlockMask> CODEC = BuilderCodec.builder(BlockMask.class, BlockMask::new)
      .documentation("Defines a block mask that filters which blocks and fluids can be replaced, using include and exclude rules to control placement targets")
      .<BlockMask.Rule>append(new KeyedCodec<>("Include", BlockMask.Rule.CODEC), (m, v) -> m.include = v, m -> m.include)
      .documentation("Set of block and fluid types that should be included by this mask")
      .add()
      .<BlockMask.Rule>append(new KeyedCodec<>("Exclude", BlockMask.Rule.CODEC), (m, v) -> m.exclude = v, m -> m.exclude)
      .documentation("Set of block and fluid types that should be excluded by this mask")
      .add()
      .afterDecode(BlockMask::rebuild)
      .build();
   public static final BlockMask REPLACE_ANY = new BlockMask() {
      {
         this.include.any = true;
         this.rebuild();
      }
   };
   public static final BlockMask REPLACE_EMPTY = new BlockMask() {
      {
         this.include.empty = true;
         this.rebuild();
      }
   };
   public static final BlockMask REPLACE_SOLID = new BlockMask() {
      {
         this.include.any = true;
         this.exclude.empty = true;
         this.rebuild();
      }
   };
   protected BlockMask.Rule include = new BlockMask.Rule();
   protected BlockMask.Rule exclude = new BlockMask.Rule();
   @Nonnull
   protected transient BlockMaskCondition mask = BlockMaskCondition.DEFAULT_TRUE;
   @Nonnull
   protected transient IBlockFluidCondition condition = ConstantBlockFluidCondition.DEFAULT_TRUE;

   @Nonnull
   public BlockMaskCondition getBlockMask() {
      return this.mask;
   }

   @Nonnull
   public IBlockFluidCondition getCondition() {
      return this.condition;
   }

   protected void rebuild() {
      this.condition = new FilteredBlockFluidCondition(this.exclude.buildCondition(), this.include.buildCondition());
      this.mask = new BlockMaskCondition();
      this.mask
         .set(
            new BlockMaskCondition.Mask(new BlockMaskCondition.MaskEntry[]{this.exclude.buildMaskEntry(false), this.include.buildMaskEntry(true)}),
            Long2ObjectMaps.emptyMap()
         );
   }

   public static class Rule {
      public static final BuilderCodec<BlockMask.Rule> CODEC = BuilderCodec.builder(BlockMask.Rule.class, BlockMask.Rule::new)
         .documentation("Defines a block mask rule specifying which blocks and fluids to match, using any, empty, or explicit block and fluid lists")
         .<Boolean>append(new KeyedCodec<>("Any", Codec.BOOLEAN), (m, v) -> m.any = v, m -> m.any)
         .documentation("Flag indicating the rule matches on any block or fluid")
         .add()
         .<Boolean>append(new KeyedCodec<>("Empty", Codec.BOOLEAN), (m, v) -> m.empty = v, m -> m.empty)
         .documentation("Flag indicating the rule matches on the empty block")
         .add()
         .<String[]>append(new KeyedCodec<>("Blocks", Codecs.BLOCK_TYPE_ARRAY), (m, v) -> m.blocks = v, m -> m.blocks)
         .documentation("List of blocks that this rule should match on")
         .add()
         .<String[]>append(new KeyedCodec<>("Fluids", Codecs.FLUID_TYPE_ARRAY), (m, v) -> m.fluids = v, m -> m.fluids)
         .documentation("List of fluids that this rule should match on")
         .add()
         .build();
      protected static final ListPool<BlockFluidEntry> POOL = new ListPool<>(10, BlockFluidEntry.EMPTY_ARRAY);
      protected boolean any = false;
      protected boolean empty = false;
      protected String[] blocks = ArrayUtil.EMPTY_STRING_ARRAY;
      protected String[] fluids = ArrayUtil.EMPTY_STRING_ARRAY;

      public IBlockFluidCondition buildCondition() {
         if (this.any) {
            return ConstantBlockFluidCondition.DEFAULT_TRUE;
         } else if (!this.empty && this.blocks.length == 0 && this.fluids.length == 0) {
            return ConstantBlockFluidCondition.DEFAULT_FALSE;
         } else {
            IndexedLookupTableAssetMap<String, Fluid> fluidTypes = Fluid.getAssetMap();
            BlockTypeAssetMap<String, BlockType> blockTypes = BlockType.getAssetMap();
            LongOpenHashSet ids = new LongOpenHashSet();
            if (this.empty) {
               ids.add(MathUtil.packLong(0, 0));
            }

            for (String block : this.blocks) {
               ObjectSet<String> variants = blockTypes.getSubKeys(block);
               ObjectIterator var9 = variants.iterator();

               while (var9.hasNext()) {
                  String name = (String)var9.next();
                  int blockId = blockTypes.getIndex(name);
                  if (blockId != 1) {
                     ids.add(MathUtil.packLong(blockId, 0));

                     for (String fluid : this.fluids) {
                        int fluidId = fluidTypes.getIndex(fluid);
                        if (fluidId != 1) {
                           ids.add(MathUtil.packLong(blockId, fluidId));
                        }
                     }
                  }
               }
            }

            for (String fluidx : this.fluids) {
               int fluidId = fluidTypes.getIndex(fluidx);
               if (fluidId != 1) {
                  ids.add(MathUtil.packLong(0, fluidId));
               }
            }

            return new HashSetBlockFluidCondition(ids);
         }
      }

      public BlockMaskCondition.MaskEntry buildMaskEntry(boolean match) {
         if (this.any) {
            return match ? BlockMaskCondition.MaskEntry.WILDCARD_TRUE : BlockMaskCondition.MaskEntry.WILDCARD_FALSE;
         } else if (!this.empty && this.blocks.length == 0 && this.fluids.length == 0) {
            return match ? BlockMaskCondition.MaskEntry.WILDCARD_FALSE : BlockMaskCondition.MaskEntry.WILDCARD_TRUE;
         } else {
            IndexedLookupTableAssetMap<String, Fluid> fluidTypes = Fluid.getAssetMap();
            BlockTypeAssetMap<String, BlockType> blockTypes = BlockType.getAssetMap();

            BlockMaskCondition.MaskEntry var23;
            try (ListPool.Resource<BlockFluidEntry> list = POOL.acquire()) {
               if (this.empty) {
                  list.add(BlockFluidEntry.EMPTY);
               }

               for (String block : this.blocks) {
                  ObjectSet<String> variants = blockTypes.getSubKeys(block);
                  ObjectIterator var10 = variants.iterator();

                  while (var10.hasNext()) {
                     String name = (String)var10.next();
                     int blockId = blockTypes.getIndex(name);
                     if (blockId != 1) {
                        list.add(new BlockFluidEntry(blockId, 0, 0));

                        for (String fluid : this.fluids) {
                           int fluidId = fluidTypes.getIndex(fluid);
                           if (fluidId != 1) {
                              list.add(new BlockFluidEntry(blockId, 0, fluidId));
                           }
                        }
                     }
                  }
               }

               for (String fluidx : this.fluids) {
                  int fluidId = fluidTypes.getIndex(fluidx);
                  if (fluidId != 1) {
                     list.add(new BlockFluidEntry(0, 0, fluidId));
                  }
               }

               BlockMaskCondition.MaskEntry mask = new BlockMaskCondition.MaskEntry(false, match);
               mask.set(new ResolvedBlockArray(list.toArray()), match);
               var23 = mask;
            }

            return var23;
         }
      }
   }
}
