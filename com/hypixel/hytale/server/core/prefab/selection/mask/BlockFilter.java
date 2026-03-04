package com.hypixel.hytale.server.core.prefab.selection.mask;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.function.FunctionCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BlockTypeListAsset;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.PlaceFluidInteraction;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockFilter {
   public static final BlockFilter[] EMPTY_ARRAY = new BlockFilter[0];
   public static final Codec<BlockFilter> CODEC = new FunctionCodec<>(Codec.STRING, BlockFilter::parse, BlockFilter::toString);
   public static final String BLOCK_SEPARATOR = "|";
   public static final Pattern BLOCK_SEPARATOR_PATTERN = Pattern.compile(Pattern.quote("|"));
   @Nonnull
   private final BlockFilter.FilterType blockFilterType;
   @Nonnull
   private final String[] blocks;
   private final boolean inverted;
   @Nonnull
   private final transient String toString0;
   private IntSet resolvedBlocks;
   private IntSet resolvedFluids;

   public BlockFilter(@Nonnull BlockFilter.FilterType blockFilterType, @Nonnull String[] blocks, boolean inverted) {
      Objects.requireNonNull(blockFilterType);
      Objects.requireNonNull(blocks);
      this.blockFilterType = blockFilterType;
      this.blocks = blocks;
      this.inverted = inverted;
      this.toString0 = this.toString0();
   }

   public void resolve() {
      if (this.resolvedBlocks == null) {
         BlockFilter.BlocksAndFluids result = parseBlocksAndFluids(this.blocks);
         this.resolvedBlocks = result.blocks;
         this.resolvedFluids = result.fluids;
      }
   }

   @Nonnull
   public BlockFilter.FilterType getBlockFilterType() {
      return this.blockFilterType;
   }

   @Nonnull
   public String[] getBlocks() {
      return this.blocks;
   }

   public boolean isInverted() {
      return this.inverted;
   }

   public boolean isExcluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, Vector3i min, Vector3i max, int blockId) {
      return this.isExcluded(accessor, x, y, z, min, max, blockId, -1);
   }

   public boolean isExcluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, Vector3i min, Vector3i max, int blockId, int fluidId) {
      boolean exclude = !this.isIncluded(accessor, x, y, z, min, max, blockId, fluidId);
      return this.inverted != exclude;
   }

   private boolean isIncluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, @Nullable Vector3i min, @Nullable Vector3i max, int blockId) {
      return this.isIncluded(accessor, x, y, z, min, max, blockId, -1);
   }

   private boolean isIncluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, @Nullable Vector3i min, @Nullable Vector3i max, int blockId, int fluidId) {
      switch (this.blockFilterType) {
         case TargetBlock:
            this.resolve();
            boolean matchesBlock = this.resolvedBlocks.contains(blockId);
            boolean matchesFluid = fluidId >= 0 && this.resolvedFluids != null && this.resolvedFluids.contains(fluidId);
            return matchesBlock || matchesFluid;
         case AboveBlock:
            return this.matchesAt(accessor, x, y - 1, z);
         case BelowBlock:
            return this.matchesAt(accessor, x, y + 1, z);
         case AdjacentBlock:
            return this.matchesAt(accessor, x - 1, y, z)
               || this.matchesAt(accessor, x + 1, y, z)
               || this.matchesAt(accessor, x, y, z - 1)
               || this.matchesAt(accessor, x, y, z + 1);
         case NeighborBlock:
            for (int xo = -1; xo < 2; xo++) {
               for (int yo = -1; yo < 2; yo++) {
                  for (int zo = -1; zo < 2; zo++) {
                     if ((xo != 0 || yo != 0 || zo != 0) && this.matchesAt(accessor, x + xo, y + yo, z + zo)) {
                        return true;
                     }
                  }
               }
            }

            return false;
         case NorthBlock:
            return this.matchesAt(accessor, x, y, z - 1);
         case EastBlock:
            return this.matchesAt(accessor, x, y, z + 1);
         case SouthBlock:
            return this.matchesAt(accessor, x, y, z + 1);
         case WestBlock:
            return this.matchesAt(accessor, x, y, z - 1);
         case DiagonalXy:
            return this.matchesAt(accessor, x - 1, y + 1, z)
               || this.matchesAt(accessor, x - 1, y - 1, z)
               || this.matchesAt(accessor, x + 1, y + 1, z)
               || this.matchesAt(accessor, x + 1, y - 1, z);
         case DiagonalXz:
            return this.matchesAt(accessor, x - 1, y, z + 1)
               || this.matchesAt(accessor, x - 1, y, z - 1)
               || this.matchesAt(accessor, x + 1, y, z + 1)
               || this.matchesAt(accessor, x + 1, y, z - 1);
         case DiagonalZy:
            return this.matchesAt(accessor, x, y - 1, z + 1)
               || this.matchesAt(accessor, x, y - 1, z - 1)
               || this.matchesAt(accessor, x, y + 1, z + 1)
               || this.matchesAt(accessor, x, y + 1, z - 1);
         case Selection:
            if (min != null && max != null) {
               return x >= min.x && y >= min.y && z >= min.z && x <= max.x && y <= max.y && z <= max.z;
            }

            return false;
         default:
            throw new IllegalArgumentException("Unknown filter type: " + this.blockFilterType);
      }
   }

   private boolean matchesAt(@Nonnull ChunkAccessor accessor, int x, int y, int z) {
      this.resolve();
      return this.resolvedBlocks.contains(accessor.getBlock(x, y, z))
         ? true
         : this.resolvedFluids != null && this.resolvedFluids.contains(accessor.getFluidId(x, y, z));
   }

   @Nonnull
   @Override
   public String toString() {
      return this.toString0;
   }

   @Nonnull
   public String toString0() {
      return (this.inverted ? "!" : "") + this.blockFilterType.getPrefix() + String.join("|", this.blocks);
   }

   @Nonnull
   public String informativeToString() {
      StringBuilder builder = new StringBuilder();
      String prefix = (this.inverted ? "!" : "") + this.blockFilterType.getPrefix();
      if (this.blocks.length > 1) {
         builder.append("(");
      }

      for (int i = 0; i < this.blocks.length; i++) {
         builder.append(prefix).append(this.blocks[i]);
         if (i != this.blocks.length - 1) {
            builder.append(" OR ");
         }
      }

      if (this.blocks.length > 1) {
         builder.append(")");
      }

      return builder.toString();
   }

   @Nonnull
   public static BlockFilter parse(@Nonnull String str) {
      BlockFilter.ParsedFilterParts parts = parseComponents(str);
      String[] blocks = parts.type.hasBlocks() ? BLOCK_SEPARATOR_PATTERN.split(parts.blocks) : ArrayUtil.EMPTY_STRING_ARRAY;
      return new BlockFilter(parts.type, blocks, parts.inverted);
   }

   @Nonnull
   public static BlockFilter.ParsedFilterParts parseComponents(@Nonnull String str) {
      boolean invert = str.startsWith("!");
      int index = invert ? 1 : 0;
      BlockFilter.FilterType filterType = BlockFilter.FilterType.parse(str, index);
      index += filterType.getPrefix().length();
      String blocks = str.substring(index);
      return new BlockFilter.ParsedFilterParts(filterType, invert, blocks);
   }

   @Nonnull
   public static IntSet parseBlocks(@Nonnull String[] blocksArgs) {
      return parseBlocksAndFluids(blocksArgs).blocks;
   }

   @Nonnull
   private static BlockFilter.BlocksAndFluids parseBlocksAndFluids(@Nonnull String[] blocksArgs) {
      IntSet blocks = new IntOpenHashSet();
      IntSet fluids = new IntOpenHashSet();

      for (String blockArg : blocksArgs) {
         Item item = Item.getAssetMap().getAsset(blockArg);
         if (item != null) {
            int fluidId = getFluidIdFromItem(item);
            if (fluidId >= 0) {
               fluids.add(fluidId);
               continue;
            }
         }

         int blockId = BlockPattern.parseBlock(blockArg);
         BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
         if (blockType != null && blockType.getBlockListAssetId() != null) {
            BlockTypeListAsset blockTypeListAsset = BlockTypeListAsset.getAssetMap().getAsset(blockType.getBlockListAssetId());
            if (blockTypeListAsset != null && blockTypeListAsset.getBlockPattern() != null) {
               Integer[] var11 = blockTypeListAsset.getBlockPattern().getResolvedKeys();
               int var12 = var11.length;

               for (int var13 = 0; var13 < var12; var13++) {
                  int resolvedKey = var11[var13];
                  blocks.add(resolvedKey);
               }
               continue;
            }
         }

         blocks.add(blockId);
      }

      return new BlockFilter.BlocksAndFluids(IntSets.unmodifiable(blocks), fluids.isEmpty() ? null : IntSets.unmodifiable(fluids));
   }

   private static int getFluidIdFromItem(@Nonnull Item item) {
      Map<InteractionType, String> interactions = item.getInteractions();
      String secondaryRootId = interactions.get(InteractionType.Secondary);
      if (secondaryRootId == null) {
         return -1;
      } else {
         RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(secondaryRootId);
         if (rootInteraction == null) {
            return -1;
         } else {
            for (String interactionId : rootInteraction.getInteractionIds()) {
               Interaction interaction = Interaction.getAssetMap().getAsset(interactionId);
               if (interaction instanceof PlaceFluidInteraction placeFluidInteraction) {
                  String fluidKey = placeFluidInteraction.getFluidKey();
                  if (fluidKey != null) {
                     int fluidId = Fluid.getAssetMap().getIndex(fluidKey);
                     if (fluidId >= 0) {
                        return fluidId;
                     }
                  }
               }
            }

            return -1;
         }
      }
   }

   private static class BlocksAndFluids {
      final IntSet blocks;
      final IntSet fluids;

      BlocksAndFluids(IntSet blocks, IntSet fluids) {
         this.blocks = blocks;
         this.fluids = fluids;
      }
   }

   public static enum FilterType {
      TargetBlock(""),
      AboveBlock(">"),
      BelowBlock("<"),
      AdjacentBlock("~"),
      NeighborBlock("^"),
      NorthBlock("+n"),
      EastBlock("+e"),
      SouthBlock("+s"),
      WestBlock("+w"),
      DiagonalXy("%xy"),
      DiagonalXz("%xz"),
      DiagonalZy("%zy"),
      Selection("#", false);

      public static final String INVERT_PREFIX = "!";
      public static final String TARGET_BLOCK_PREFIX = "";
      public static final String ABOVE_BLOCK_PREFIX = ">";
      public static final String BELOW_BLOCK_PREFIX = "<";
      public static final String ADJACENT_BLOCK_PREFIX = "~";
      public static final String NEIGHBOR_BLOCK_PREFIX = "^";
      public static final String SELECTION_PREFIX = "#";
      public static final String CARDINAL_NORTH_PREFIX = "+n";
      public static final String CARDINAL_EAST_PREFIX = "+e";
      public static final String CARDINAL_SOUTH_PREFIX = "+s";
      public static final String CARDINAL_WEST_PREFIX = "+w";
      public static final String DIAGONAL_XY_PREFIX = "%xy";
      public static final String DIAGONAL_XZ_PREFIX = "%xz";
      public static final String DIAGONAL_ZY_PREFIX = "%zy";
      @Nonnull
      private static final BlockFilter.FilterType[] VALUES_TO_PARSE;
      private final String prefix;
      private final boolean hasBlocks;

      private FilterType(String prefix) {
         this.prefix = prefix;
         this.hasBlocks = true;
      }

      private FilterType(String prefix, boolean hasBlocks) {
         this.prefix = prefix;
         this.hasBlocks = hasBlocks;
      }

      public boolean hasBlocks() {
         return this.hasBlocks;
      }

      public String getPrefix() {
         return this.prefix;
      }

      @Nonnull
      public static BlockFilter.FilterType parse(@Nonnull String str, int index) {
         for (BlockFilter.FilterType filterType : VALUES_TO_PARSE) {
            if (str.startsWith(filterType.prefix, index)) {
               return filterType;
            }
         }

         return TargetBlock;
      }

      static {
         BlockFilter.FilterType[] values = values();
         BlockFilter.FilterType[] valuesToParse = new BlockFilter.FilterType[values.length - 1];
         int i = 0;

         for (BlockFilter.FilterType value : values) {
            if (value != TargetBlock) {
               valuesToParse[i++] = value;
            }
         }

         VALUES_TO_PARSE = valuesToParse;
      }
   }

   public record ParsedFilterParts(BlockFilter.FilterType type, boolean inverted, String blocks) {
   }
}
