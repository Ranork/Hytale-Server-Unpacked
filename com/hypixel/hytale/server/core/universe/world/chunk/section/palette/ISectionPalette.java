package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.function.consumer.BiIntConsumer;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;

public interface ISectionPalette {
   PaletteType getPaletteType();

   ISectionPalette.SetResult set(int var1, int var2);

   int get(int var1);

   boolean contains(int var1);

   boolean containsAny(IntList var1);

   default boolean isSolid(int id) {
      return this.count() == 1 && this.contains(id);
   }

   int count();

   int count(int var1);

   IntSet values();

   void forEachValue(IntConsumer var1);

   Int2ShortMap valueCounts();

   void find(@Nonnull IntList var1, @Nonnull IntConsumer var2);

   void find(@Nonnull IntList var1, @Nonnull BiIntConsumer var2);

   @Deprecated(since = "2026-02-26", forRemoval = true)
   default void find(@Nonnull IntList ids, @Nonnull IntSet ignoredInternalIdHolder, @Nonnull IntConsumer indexConsumer) {
      this.find(ids, indexConsumer);
   }

   boolean shouldDemote();

   ISectionPalette demote();

   ISectionPalette promote();

   void serializeForPacket(ByteBuf var1);

   void serialize(ISectionPalette.KeySerializer var1, ByteBuf var2);

   void deserialize(ToIntFunction<ByteBuf> var1, ByteBuf var2, int var3);

   @Nonnull
   static ISectionPalette from(@Nonnull int[] data, @Nonnull Int2ShortMap idCounts) {
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

   @FunctionalInterface
   public interface KeySerializer {
      void serialize(ByteBuf var1, int var2);
   }

   public static enum SetResult {
      ADDED_OR_REMOVED,
      CHANGED,
      UNCHANGED,
      REQUIRES_PROMOTE;
   }
}
