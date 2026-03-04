package com.hypixel.hytale.server.core.prefab.selection.mask;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.function.FunctionCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMask {
   public static final BlockMask EMPTY = new BlockMask(BlockFilter.EMPTY_ARRAY);
   public static final Codec<BlockMask> CODEC = new FunctionCodec<>(Codec.STRING, BlockMask::parse, BlockMask::toString);
   public static final String MASK_SEPARATOR = ",";
   public static final String ALT_MASK_SEPARATOR = ";";
   public static final String EMPTY_MASK_CHARACTER = "-";
   private final BlockFilter[] filters;
   private boolean inverted;

   public BlockMask(BlockFilter[] filters) {
      this.filters = filters;
   }

   @Nonnull
   public BlockMask withOptions(@Nonnull BlockFilter.FilterType filterType, boolean inverted) {
      if (this == EMPTY) {
         return this;
      } else {
         BlockFilter[] filters = this.filters;

         for (BlockFilter filter : filters) {
            if (filter.getBlockFilterType() != filterType || filter.isInverted() != inverted) {
               filters = new BlockFilter[filters.length];
               break;
            }
         }

         if (filters == this.filters) {
            return this;
         } else {
            for (int i = 0; i < filters.length; i++) {
               BlockFilter filterx = this.filters[i];
               if (filterx.getBlockFilterType() != filterType || filterx.isInverted() != inverted) {
                  filterx = new BlockFilter(filterType, filterx.getBlocks(), inverted);
               }

               filters[i] = filterx;
            }

            return new BlockMask(filters);
         }
      }
   }

   public BlockFilter[] getFilters() {
      return this.filters;
   }

   public void setInverted(boolean inverted) {
      this.inverted = inverted;
   }

   public boolean isInverted() {
      return this.inverted;
   }

   public boolean isExcluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, Vector3i min, Vector3i max, int blockId) {
      return this.isExcluded(accessor, x, y, z, min, max, blockId, -1);
   }

   public boolean isExcluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, Vector3i min, Vector3i max, int blockId, int fluidId) {
      boolean excluded = false;

      for (BlockFilter filter : this.filters) {
         if (filter.isExcluded(accessor, x, y, z, min, max, blockId, fluidId)) {
            excluded = true;
            break;
         }
      }

      return this.inverted != excluded;
   }

   @Nonnull
   @Override
   public String toString() {
      if (this.filters.length == 0) {
         return "-";
      } else {
         String base = joinElements(",", this.filters);
         return this.inverted ? "!" + base : base;
      }
   }

   @Nonnull
   public String informativeToString() {
      if (this.filters.length == 0) {
         return "-";
      } else {
         StringBuilder builder = new StringBuilder();
         if (this.inverted) {
            builder.append("NOT(");
         }

         if (this.filters.length > 1) {
            builder.append("(");
         }

         for (int i = 0; i < this.filters.length; i++) {
            builder.append(this.filters[i].informativeToString());
            if (i != this.filters.length - 1) {
               builder.append(" AND ");
            }
         }

         if (this.filters.length > 1) {
            builder.append(")");
         }

         if (this.inverted) {
            builder.append(")");
         }

         return builder.toString();
      }
   }

   @Nonnull
   protected static String joinElements(String separator, @Nonnull Object[] elements) {
      StringBuilder sb = new StringBuilder();

      for (Object o : elements) {
         if (!sb.isEmpty()) {
            sb.append(separator);
         }

         sb.append(o);
      }

      return sb.toString();
   }

   public static BlockMask parse(@Nonnull String masks) {
      if (!masks.isEmpty() && !masks.equals("-")) {
         masks = masks.replace(";", ",");
         return parse(masks.split(","));
      } else {
         return EMPTY;
      }
   }

   public static BlockMask parse(@Nonnull String[] masks) {
      if (masks.length == 0) {
         return EMPTY;
      } else if (masks.length == 1) {
         return new BlockMask(new BlockFilter[]{BlockFilter.parse(masks[0])});
      } else {
         BlockFilter[] parsedFilters = new BlockFilter[masks.length];

         for (int i = 0; i < masks.length; i++) {
            parsedFilters[i] = BlockFilter.parse(masks[i]);
         }

         return groupFilters(parsedFilters);
      }
   }

   public static BlockMask combine(@Nullable BlockMask... masks) {
      if (masks != null && masks.length != 0) {
         int totalFilters = 0;

         for (BlockMask mask : masks) {
            if (mask != null && mask != EMPTY) {
               totalFilters += mask.getFilters().length;
            }
         }

         if (totalFilters == 0) {
            return EMPTY;
         } else {
            BlockFilter[] allFilters = new BlockFilter[totalFilters];
            int idx = 0;

            for (BlockMask maskx : masks) {
               if (maskx != null && maskx != EMPTY) {
                  for (BlockFilter filter : maskx.getFilters()) {
                     allFilters[idx++] = filter;
                  }
               }
            }

            return groupFilters(allFilters);
         }
      } else {
         return EMPTY;
      }
   }

   private static BlockMask groupFilters(@Nonnull BlockFilter[] inputFilters) {
      if (inputFilters.length == 0) {
         return EMPTY;
      } else if (inputFilters.length == 1) {
         return new BlockMask(inputFilters);
      } else {
         Int2ObjectLinkedOpenHashMap<List<String>> groups = new Int2ObjectLinkedOpenHashMap();

         for (BlockFilter filter : inputFilters) {
            int key = filter.getBlockFilterType().ordinal() << 1 | (filter.isInverted() ? 1 : 0);
            List<String> list = (List<String>)groups.computeIfAbsent(key, k -> new ArrayList());

            for (String block : filter.getBlocks()) {
               list.add(block);
            }
         }

         if (groups.size() == inputFilters.length) {
            return new BlockMask(inputFilters);
         } else {
            BlockFilter[] filters = new BlockFilter[groups.size()];
            int i = 0;
            ObjectBidirectionalIterator var14 = groups.int2ObjectEntrySet().iterator();

            while (var14.hasNext()) {
               Entry<List<String>> entry = (Entry<List<String>>)var14.next();
               int key = entry.getIntKey();
               BlockFilter.FilterType filterType = BlockFilter.FilterType.values()[key >> 1];
               boolean inverted = (key & 1) != 0;
               String[] blocks = ((List)entry.getValue()).toArray(new String[0]);
               filters[i++] = new BlockFilter(filterType, blocks, inverted);
            }

            return new BlockMask(filters);
         }
      }
   }
}
