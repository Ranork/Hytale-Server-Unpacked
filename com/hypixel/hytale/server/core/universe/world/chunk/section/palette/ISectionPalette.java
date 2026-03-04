package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

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

   void find(IntList var1, IntSet var2, IntConsumer var3);

   boolean shouldDemote();

   ISectionPalette demote();

   ISectionPalette promote();

   void serializeForPacket(ByteBuf var1);

   void serialize(ISectionPalette.KeySerializer var1, ByteBuf var2);

   void deserialize(ToIntFunction<ByteBuf> var1, ByteBuf var2, int var3);

   @Nonnull
   static ISectionPalette from(@Nonnull int[] data, int[] unique, int count) {
      if (count == 1 && unique[0] == 0) {
         return EmptySectionPalette.INSTANCE;
      } else if (count < 16) {
         return new HalfByteSectionPalette(data, unique, count);
      } else if (count < 256) {
         return new ByteSectionPalette(data, unique, count);
      } else if (count < 65536) {
         return new ShortSectionPalette(data, unique, count);
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
