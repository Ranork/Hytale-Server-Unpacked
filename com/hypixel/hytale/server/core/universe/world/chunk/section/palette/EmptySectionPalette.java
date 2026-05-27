package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.function.consumer.BiIntConsumer;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.foreign.MemorySegment;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;

public final class EmptySectionPalette extends AbstractSectionPalette {
   public static final int EMPTY_ID = 0;
   public static final EmptySectionPalette INSTANCE = new EmptySectionPalette();

   private EmptySectionPalette() {
   }

   @Nonnull
   @Override
   public PaletteType getPaletteType() {
      return PaletteType.Empty;
   }

   @Nonnull
   @Override
   public AbstractSectionPalette.SetResult set(int index, int id) {
      return id == 0 ? AbstractSectionPalette.SetResult.UNCHANGED : AbstractSectionPalette.SetResult.REQUIRES_PROMOTE;
   }

   @Override
   public int get(int index) {
      return 0;
   }

   @Override
   public boolean shouldDemote() {
      return false;
   }

   @Override
   public AbstractSectionPalette demote() {
      throw new UnsupportedOperationException("Cannot demote empty chunk section!");
   }

   @Nonnull
   @Override
   public AbstractSectionPalette promote() {
      return new HalfByteSectionPalette();
   }

   @Override
   public boolean contains(int id) {
      return id == 0;
   }

   @Override
   public boolean containsAny(@Nonnull IntList ids) {
      return ids.contains(0);
   }

   @Override
   public boolean isSolid(int id) {
      return id == 0;
   }

   @Override
   public int count() {
      return 1;
   }

   @Override
   public int count(int id) {
      return id == 0 ? 32768 : 0;
   }

   @Nonnull
   @Override
   public IntSet values() {
      IntSet set = new IntOpenHashSet();
      set.add(0);
      return set;
   }

   @Override
   public void forEachValue(@Nonnull IntConsumer consumer) {
      consumer.accept(0);
   }

   @Nonnull
   @Override
   public Int2ShortMap valueCounts() {
      Int2ShortMap map = new Int2ShortOpenHashMap();
      map.put(0, (short)-32768);
      return map;
   }

   @Override
   public void find(@Nonnull IntList ids, @Nonnull IntConsumer indexConsumer) {
      if (ids.contains(0)) {
         for (int i = 0; i < 32768; i++) {
            indexConsumer.accept(i);
         }
      }
   }

   @Override
   public void find(@Nonnull IntList ids, @Nonnull BiIntConsumer indexBlockConsumer) {
      if (ids.contains(0)) {
         for (int i = 0; i < 32768; i++) {
            indexBlockConsumer.accept(i, 0);
         }
      }
   }

   @Override
   public int serializedPacketByteSize() {
      return 0;
   }

   @Override
   public int serializeForPacket(MemorySegment memorySegment, int offset) {
      return 0;
   }

   @Override
   public int serializedByteSize(AbstractSectionPalette.KeyMemorySerializer keySerializer) {
      return 0;
   }

   @Override
   public int serialize(AbstractSectionPalette.KeyMemorySerializer keySerializer, MemorySegment memorySegment, int offset) {
      return 0;
   }

   @Override
   public int deserialize(AbstractSectionPalette.KeyMemoryDeserializer deserializer, MemorySegment memorySegment, int offset) {
      return 0;
   }
}
