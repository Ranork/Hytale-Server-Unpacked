package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.function.consumer.BiIntConsumer;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.foreign.MemorySegment;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;

public abstract sealed class AbstractSectionPalette permits EmptySectionPalette, HalfByteSectionPalette, ByteSectionPalette, ShortSectionPalette {
   public abstract PaletteType getPaletteType();

   public abstract AbstractSectionPalette.SetResult set(int var1, int var2);

   public abstract int get(int var1);

   public abstract boolean contains(int var1);

   public abstract boolean containsAny(IntList var1);

   public boolean isSolid(int id) {
      return this.count() == 1 && this.contains(id);
   }

   public abstract int count();

   public abstract int count(int var1);

   public abstract IntSet values();

   public abstract void forEachValue(IntConsumer var1);

   public abstract Int2ShortMap valueCounts();

   public abstract void find(@Nonnull IntList var1, @Nonnull IntConsumer var2);

   public abstract void find(@Nonnull IntList var1, @Nonnull BiIntConsumer var2);

   @Deprecated(since = "2026-02-26", forRemoval = true)
   public void find(@Nonnull IntList ids, @Nonnull IntSet ignoredInternalIdHolder, @Nonnull IntConsumer indexConsumer) {
      this.find(ids, indexConsumer);
   }

   public abstract boolean shouldDemote();

   public abstract AbstractSectionPalette demote();

   public abstract AbstractSectionPalette promote();

   public abstract int serializedPacketByteSize();

   public abstract int serializeForPacket(MemorySegment var1, int var2);

   public abstract int serializedByteSize(AbstractSectionPalette.KeyMemorySerializer var1);

   public abstract int serialize(AbstractSectionPalette.KeyMemorySerializer var1, MemorySegment var2, int var3);

   public abstract int deserialize(AbstractSectionPalette.KeyMemoryDeserializer var1, MemorySegment var2, int var3);

   @Nonnull
   public static AbstractSectionPalette from(@Nonnull int[] data, @Nonnull Int2ShortMap idCounts) {
      if (idCounts.size() == 1 && idCounts.containsKey(0)) {
         return EmptySectionPalette.INSTANCE;
      } else if (idCounts.size() < 16) {
         return new HalfByteSectionPalette(data, idCounts);
      } else if (idCounts.size() < 256) {
         return new ByteSectionPalette(data, idCounts);
      } else if (idCounts.size() < 65536) {
         return new ShortSectionPalette(data, idCounts);
      } else {
         throw new UnsupportedOperationException("Too many block types for palette.");
      }
   }

   public interface KeyMemoryDeserializer {
      int deserialize(MemorySegment var1, int var2);

      int keySize(MemorySegment var1, int var2);
   }

   public interface KeyMemorySerializer {
      int serialize(MemorySegment var1, int var2, int var3);

      int keySize(int var1);
   }

   public static enum SetResult {
      ADDED_OR_REMOVED,
      CHANGED,
      UNCHANGED,
      REQUIRES_PROMOTE;
   }
}
