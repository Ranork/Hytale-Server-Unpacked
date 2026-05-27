package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.common.util.BitSetUtil;
import com.hypixel.hytale.common.util.BitUtil;
import com.hypixel.hytale.function.consumer.BiIntConsumer;
import com.hypixel.hytale.math.util.NumberUtil;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2ShortMap;
import it.unimi.dsi.fastutil.bytes.Byte2ShortOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMaps;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ShortMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ByteMap;
import it.unimi.dsi.fastutil.shorts.Short2ByteOpenHashMap;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.BitSet;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ByteSectionPalette extends AbstractSectionPalette {
   private static final int KEY_MASK = 255;
   public static final int MAX_SIZE = 256;
   public static final int DEMOTE_SIZE = 14;
   final Int2ByteMap externalToInternal;
   final Byte2IntMap internalToExternal;
   final BitSet internalIdSet;
   final Byte2ShortMap internalIdCount;
   final byte[] blocks;

   public ByteSectionPalette() {
      this.externalToInternal = new Int2ByteOpenHashMap();
      this.internalToExternal = new Byte2IntOpenHashMap();
      this.internalIdSet = new BitSet(256);
      this.internalIdCount = new Byte2ShortOpenHashMap();
      this.blocks = new byte[32768];
      this.externalToInternal.put(0, (byte)0);
      this.internalToExternal.put((byte)0, 0);
      this.internalIdSet.set(0);
      this.internalIdCount.put((byte)0, (short)-32768);
   }

   ByteSectionPalette(Int2ByteMap externalToInternal, Byte2IntMap internalToExternal, BitSet internalIdSet, Byte2ShortMap internalIdCount, byte[] blocks) {
      this.externalToInternal = externalToInternal;
      this.internalToExternal = internalToExternal;
      this.internalIdSet = internalIdSet;
      this.internalIdCount = internalIdCount;
      this.blocks = blocks;
   }

   public ByteSectionPalette(@Nonnull int[] data, @Nonnull Int2ShortMap externalIdCounts) {
      this.blocks = new byte[32768];
      this.externalToInternal = new Int2ByteOpenHashMap(externalIdCounts.size());
      this.internalToExternal = new Byte2IntOpenHashMap(externalIdCounts.size());
      this.internalIdSet = new BitSet(externalIdCounts.size());
      this.internalIdCount = new Byte2ShortOpenHashMap(externalIdCounts.size());
      byte internalIdCounter = 0;

      for (ObjectIterator index = Int2ShortMaps.fastIterable(externalIdCounts).iterator(); index.hasNext(); internalIdCounter++) {
         Entry entry = (Entry)index.next();
         this.internalToExternal.put(internalIdCounter, entry.getIntKey());
         this.externalToInternal.put(entry.getIntKey(), internalIdCounter);
         this.internalIdSet.set(this.unsignedInternalId(internalIdCounter));
         this.internalIdCount.put(internalIdCounter, entry.getShortValue());
      }

      int index = 0;

      while (index < data.length) {
         int externalId = data[index];
         int start = index;

         do {
            index++;
         } while (index < data.length && data[index] == externalId);

         byte internalId = this.externalToInternal.get(externalId);

         for (int i = start; i < index; i++) {
            this.blocks[i] = internalId;
         }
      }
   }

   @Nonnull
   @Override
   public PaletteType getPaletteType() {
      return PaletteType.Byte;
   }

   @Override
   public int get(int index) {
      return this.internalToExternal.get(this.blocks[index]);
   }

   @Nonnull
   @Override
   public AbstractSectionPalette.SetResult set(int index, int id) {
      byte oldInternalId = this.blocks[index];
      if (this.externalToInternal.containsKey(id)) {
         byte newInternalId = this.externalToInternal.get(id);
         if (newInternalId == oldInternalId) {
            return AbstractSectionPalette.SetResult.UNCHANGED;
         } else {
            boolean removed = this.decrementBlockCount(oldInternalId);
            this.incrementBlockCount(newInternalId);
            this.blocks[index] = newInternalId;
            return removed ? AbstractSectionPalette.SetResult.ADDED_OR_REMOVED : AbstractSectionPalette.SetResult.CHANGED;
         }
      } else {
         int nextInternalId = this.nextInternalId(oldInternalId);
         if (!this.isValidInternalId(nextInternalId)) {
            return AbstractSectionPalette.SetResult.REQUIRES_PROMOTE;
         } else {
            this.decrementBlockCount(oldInternalId);
            byte newInternalId = (byte)nextInternalId;
            this.createBlockId(newInternalId, id);
            this.blocks[index] = newInternalId;
            return AbstractSectionPalette.SetResult.ADDED_OR_REMOVED;
         }
      }
   }

   private int nextInternalId(byte oldInternalId) {
      return this.internalIdCount.get(oldInternalId) == 1 ? this.unsignedInternalId(oldInternalId) : this.internalIdSet.nextClearBit(0);
   }

   private void createBlockId(byte internalId, int blockId) {
      this.internalToExternal.put(internalId, blockId);
      this.externalToInternal.put(blockId, internalId);
      this.internalIdSet.set(this.unsignedInternalId(internalId));
      this.internalIdCount.put(internalId, (short)1);
   }

   private boolean decrementBlockCount(byte internalId) {
      short oldCount = this.internalIdCount.get(internalId);
      if (oldCount == 1) {
         this.internalIdCount.remove(internalId);
         int externalId = this.internalToExternal.remove(internalId);
         this.externalToInternal.remove(externalId);
         this.internalIdSet.clear(this.unsignedInternalId(internalId));
         return true;
      } else {
         this.internalIdCount.mergeShort(internalId, (short)1, NumberUtil::subtract);
         return false;
      }
   }

   private void incrementBlockCount(byte internalId) {
      this.internalIdCount.mergeShort(internalId, (short)1, NumberUtil::sum);
   }

   @Override
   public boolean contains(int id) {
      return this.externalToInternal.containsKey(id);
   }

   @Override
   public boolean containsAny(@Nonnull IntList ids) {
      for (int i = 0; i < ids.size(); i++) {
         if (this.externalToInternal.containsKey(ids.getInt(i))) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int count() {
      return this.internalIdCount.size();
   }

   @Override
   public int count(int id) {
      if (this.externalToInternal.containsKey(id)) {
         byte internalId = this.externalToInternal.get(id);
         return this.internalIdCount.get(internalId);
      } else {
         return 0;
      }
   }

   @Nonnull
   @Override
   public IntSet values() {
      return new IntOpenHashSet(this.externalToInternal.keySet());
   }

   @Override
   public void forEachValue(@Nonnull IntConsumer consumer) {
      this.externalToInternal.keySet().forEach(consumer);
   }

   @Nonnull
   @Override
   public Int2ShortMap valueCounts() {
      Int2ShortMap map = new Int2ShortOpenHashMap();
      ObjectIterator var2 = this.internalIdCount.byte2ShortEntrySet().iterator();

      while (var2.hasNext()) {
         it.unimi.dsi.fastutil.bytes.Byte2ShortMap.Entry entry = (it.unimi.dsi.fastutil.bytes.Byte2ShortMap.Entry)var2.next();
         byte internalId = entry.getByteKey();
         short count = entry.getShortValue();
         int externalId = this.internalToExternal.get(internalId);
         map.put(externalId, count);
      }

      return map;
   }

   @Override
   public boolean shouldDemote() {
      return this.count() <= 14;
   }

   @Nonnull
   public HalfByteSectionPalette demote() {
      return HalfByteSectionPalette.fromBytePalette(this);
   }

   @Nonnull
   public ShortSectionPalette promote() {
      return ShortSectionPalette.fromBytePalette(this);
   }

   private boolean isValidInternalId(int internalId) {
      return (internalId & 0xFF) == internalId;
   }

   private int unsignedInternalId(byte internalId) {
      return internalId & 0xFF;
   }

   private static int sUnsignedInternalId(byte internalId) {
      return internalId & 0xFF;
   }

   @Override
   public void find(@Nonnull IntList ids, @Nonnull IntConsumer indexConsumer) {
      ByteSet internalIds = this.buildInternalByteSet(ids);
      if (!internalIds.isEmpty()) {
         int index = 0;
         byte type = this.blocks[0];

         while (index < 32768) {
            int start = index;
            byte runType = type;

            do {
               index++;
            } while (index < 32768 && (type = this.blocks[index]) == runType);

            if (internalIds.contains(runType)) {
               for (int i = start; i < index; i++) {
                  indexConsumer.accept(i);
               }
            }
         }
      }
   }

   @Override
   public void find(@Nonnull IntList ids, @Nonnull BiIntConsumer indexBlockConsumer) {
      ByteSet internalIds = this.buildInternalByteSet(ids);
      if (!internalIds.isEmpty()) {
         int index = 0;
         byte type = this.blocks[0];

         while (index < 32768) {
            int start = index;
            byte runType = type;

            do {
               index++;
            } while (index < 32768 && (type = this.blocks[index]) == runType);

            if (internalIds.contains(runType)) {
               int external = this.internalToExternal.get(runType);

               for (int i = start; i < index; i++) {
                  indexBlockConsumer.accept(i, external);
               }
            }
         }
      }
   }

   private ByteSet buildInternalByteSet(IntList ids) {
      ByteSet internalIds = PaletteSetProvider.get().getByteSet(ids.size());

      for (int i = 0; i < ids.size(); i++) {
         byte internal = this.externalToInternal.getOrDefault(ids.getInt(i), (byte)-128);
         if (internal != -128) {
            internalIds.add(internal);
         }
      }

      return internalIds;
   }

   @Override
   public int serializedPacketByteSize() {
      return 2 + 7 * this.internalToExternal.size() + this.blocks.length;
   }

   @Override
   public int serializeForPacket(MemorySegment memorySegment, int baseOffset) {
      memorySegment.set(MemorySegmentUtil.SHORT_LE, (long)baseOffset, (short)this.internalToExternal.size());
      int offset = baseOffset + 2;

      for (ObjectIterator var4 = this.internalToExternal.byte2IntEntrySet().iterator(); var4.hasNext(); offset += 7) {
         it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry entry = (it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry)var4.next();
         byte internalId = entry.getByteKey();
         int externalId = entry.getIntValue();
         memorySegment.set(ValueLayout.JAVA_BYTE, (long)offset, internalId);
         memorySegment.set(MemorySegmentUtil.INT_LE, (long)(offset + 1), externalId);
         memorySegment.set(MemorySegmentUtil.SHORT_LE, (long)(offset + 1 + 4), this.internalIdCount.get(internalId));
      }

      MemorySegment.copy(this.blocks, 0, memorySegment, ValueLayout.JAVA_BYTE, offset, this.blocks.length);
      offset += this.blocks.length;
      return offset - baseOffset;
   }

   @Override
   public int serializedByteSize(AbstractSectionPalette.KeyMemorySerializer keySerializer) {
      int size = 2 + 3 * this.internalToExternal.size() + this.blocks.length;
      IntIterator var3 = this.internalToExternal.values().iterator();

      while (var3.hasNext()) {
         int externalId = (Integer)var3.next();
         size += keySerializer.keySize(externalId);
      }

      return size;
   }

   @Override
   public int serialize(AbstractSectionPalette.KeyMemorySerializer keySerializer, MemorySegment memorySegment, int baseOffset) {
      memorySegment.set(MemorySegmentUtil.SHORT_BE, (long)baseOffset, (short)this.internalToExternal.size());
      int offset = baseOffset + 2;
      ObjectIterator var5 = this.internalToExternal.byte2IntEntrySet().iterator();

      while (var5.hasNext()) {
         it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry entry = (it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry)var5.next();
         byte internalId = entry.getByteKey();
         int externalId = entry.getIntValue();
         memorySegment.set(ValueLayout.JAVA_BYTE, (long)offset, internalId);
         offset += 1 + keySerializer.serialize(memorySegment, offset + 1, externalId);
         memorySegment.set(MemorySegmentUtil.SHORT_BE, (long)offset, this.internalIdCount.get(internalId));
         offset += 2;
      }

      MemorySegment.copy(this.blocks, 0, memorySegment, ValueLayout.JAVA_BYTE, offset, this.blocks.length);
      offset += this.blocks.length;
      return offset - baseOffset;
   }

   @Override
   public int deserialize(AbstractSectionPalette.KeyMemoryDeserializer deserializer, MemorySegment memorySegment, int baseOffset) {
      this.externalToInternal.clear();
      this.internalToExternal.clear();
      this.internalIdSet.clear();
      this.internalIdCount.clear();
      Byte2ByteMap internalIdRemapping = null;
      int blockCount = memorySegment.get(MemorySegmentUtil.SHORT_BE, (long)baseOffset);
      int offset = baseOffset + 2;

      for (int i = 0; i < blockCount; i++) {
         byte internalId = memorySegment.get(ValueLayout.JAVA_BYTE, (long)offset);
         int externalId = deserializer.deserialize(memorySegment, ++offset);
         offset += deserializer.keySize(memorySegment, offset);
         short count = memorySegment.get(MemorySegmentUtil.SHORT_BE, (long)offset);
         offset += 2;
         if (this.externalToInternal.containsKey(externalId)) {
            byte existingInternalId = this.externalToInternal.get(externalId);
            if (internalIdRemapping == null) {
               internalIdRemapping = new Byte2ByteOpenHashMap();
            }

            internalIdRemapping.put(internalId, existingInternalId);
            this.internalIdCount.mergeShort(existingInternalId, count, NumberUtil::sum);
         } else {
            this.externalToInternal.put(externalId, internalId);
            this.internalToExternal.put(internalId, externalId);
            this.internalIdSet.set(this.unsignedInternalId(internalId));
            this.internalIdCount.put(internalId, count);
         }
      }

      MemorySegment.copy(memorySegment, ValueLayout.JAVA_BYTE, offset, this.blocks, 0, this.blocks.length);
      offset += this.blocks.length;
      this.remapBlocks(internalIdRemapping);
      return offset - baseOffset;
   }

   private void remapBlocks(@Nullable Byte2ByteMap internalIdRemapping) {
      if (internalIdRemapping != null) {
         for (int i = 0; i < 32768; i++) {
            byte oldInternalId = this.blocks[i];
            if (internalIdRemapping.containsKey(oldInternalId)) {
               this.blocks[i] = internalIdRemapping.get(oldInternalId);
            }
         }
      }
   }

   @Nonnull
   public static ByteSectionPalette fromHalfBytePalette(@Nonnull HalfByteSectionPalette section) {
      Int2ByteMap externalToInternal = new Int2ByteOpenHashMap(section.externalToInternal);
      Byte2IntMap internalToExternal = new Byte2IntOpenHashMap(section.internalToExternal);
      BitSet internalIdSet = new BitSet(256);
      BitSetUtil.copyValues(section.internalIdSet, internalIdSet);
      Byte2ShortMap internalIdCount = new Byte2ShortOpenHashMap(section.internalIdCount);
      byte[] newBlocks = new byte[32768];

      for (int i = 0; i < 32768; i++) {
         newBlocks[i] = BitUtil.getNibble(section.blocks, i);
      }

      return new ByteSectionPalette(externalToInternal, internalToExternal, internalIdSet, internalIdCount, newBlocks);
   }

   @Nonnull
   public static ByteSectionPalette fromShortPalette(@Nonnull ShortSectionPalette section) {
      if (section.count() > 256) {
         throw new IllegalStateException("Cannot demote short palette to byte palette. Too many blocks! Count: " + section.count());
      } else {
         Int2ByteMap externalToInternal = new Int2ByteOpenHashMap(section.count());
         Byte2IntMap internalToExternal = new Byte2IntOpenHashMap(section.count());
         BitSet internalIdSet = new BitSet(256);
         Byte2ShortMap internalIdCount = new Byte2ShortOpenHashMap(section.count());
         Short2ByteMap internalIdRemapping = new Short2ByteOpenHashMap(section.count());
         byte nextId = 0;
         ObjectIterator newBlocks = section.internalToExternal.short2IntEntrySet().iterator();

         while (newBlocks.hasNext()) {
            it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry entry = (it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry)newBlocks.next();
            short oldInternal = entry.getShortKey();
            int external = entry.getIntValue();
            byte newInternal = nextId++;
            internalToExternal.put(newInternal, external);
            externalToInternal.put(external, newInternal);
            internalIdSet.set(sUnsignedInternalId(newInternal));
            internalIdCount.put(newInternal, section.internalIdCount.get(oldInternal));
            internalIdRemapping.put(oldInternal, newInternal);
         }

         byte[] newBlocksx = new byte[32768];

         for (int i = 0; i < 32768; i++) {
            short internalId = section.blocks[i];
            byte byteInternalId = internalIdRemapping.get(internalId);
            newBlocksx[i] = byteInternalId;
         }

         return new ByteSectionPalette(externalToInternal, internalToExternal, internalIdSet, internalIdCount, newBlocksx);
      }
   }
}
