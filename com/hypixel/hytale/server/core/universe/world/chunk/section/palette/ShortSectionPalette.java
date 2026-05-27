package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.function.consumer.BiIntConsumer;
import com.hypixel.hytale.math.util.NumberUtil;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMaps;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ShortMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.lang.foreign.MemorySegment;
import java.util.BitSet;
import java.util.function.IntConsumer;
import javax.annotation.Nonnull;

public final class ShortSectionPalette extends AbstractSectionPalette {
   private static final int KEY_MASK = 65535;
   public static final int MAX_SIZE = 65536;
   public static final int DEMOTE_SIZE = 251;
   final Int2ShortMap externalToInternal;
   final Short2IntMap internalToExternal;
   final BitSet internalIdSet;
   final Short2ShortMap internalIdCount;
   final short[] blocks;

   public ShortSectionPalette() {
      this.externalToInternal = new Int2ShortOpenHashMap();
      this.internalToExternal = new Short2IntOpenHashMap();
      this.internalIdSet = new BitSet();
      this.internalIdCount = new Short2ShortOpenHashMap();
      this.blocks = new short[32768];
      this.externalToInternal.put(0, (short)0);
      this.internalToExternal.put((short)0, 0);
      this.internalIdSet.set(0);
      this.internalIdCount.put((short)0, (short)-32768);
   }

   ShortSectionPalette(Int2ShortMap externalToInternal, Short2IntMap internalToExternal, BitSet internalIdSet, Short2ShortMap internalIdCount, short[] blocks) {
      this.externalToInternal = externalToInternal;
      this.internalToExternal = internalToExternal;
      this.internalIdSet = internalIdSet;
      this.internalIdCount = internalIdCount;
      this.blocks = blocks;
   }

   public ShortSectionPalette(@Nonnull int[] data, @Nonnull Int2ShortMap externalIdCounts) {
      this.blocks = new short[32768];
      this.externalToInternal = new Int2ShortOpenHashMap(externalIdCounts.size());
      this.internalToExternal = new Short2IntOpenHashMap(externalIdCounts.size());
      this.internalIdSet = new BitSet(externalIdCounts.size());
      this.internalIdCount = new Short2ShortOpenHashMap(externalIdCounts.size());
      short internalIdCounter = 0;

      for (ObjectIterator index = Int2ShortMaps.fastIterable(externalIdCounts).iterator(); index.hasNext(); internalIdCounter++) {
         Entry entry = (Entry)index.next();
         this.internalToExternal.put(internalIdCounter, entry.getIntKey());
         this.externalToInternal.put(entry.getIntKey(), internalIdCounter);
         this.internalIdSet.set(internalIdCounter);
         this.internalIdCount.put(internalIdCounter, entry.getShortValue());
      }

      int index = 0;

      while (index < data.length) {
         int externalId = data[index];
         int start = index;

         do {
            index++;
         } while (index < data.length && data[index] == externalId);

         short internalId = this.externalToInternal.get(externalId);

         for (int i = start; i < index; i++) {
            this.blocks[i] = internalId;
         }
      }
   }

   @Nonnull
   @Override
   public PaletteType getPaletteType() {
      return PaletteType.Short;
   }

   @Override
   public int get(int index) {
      return this.internalToExternal.get(this.blocks[index]);
   }

   @Nonnull
   @Override
   public AbstractSectionPalette.SetResult set(int index, int id) {
      short oldInternalId = this.blocks[index];
      if (this.externalToInternal.containsKey(id)) {
         short newInternalId = this.externalToInternal.get(id);
         if (newInternalId == oldInternalId) {
            return AbstractSectionPalette.SetResult.UNCHANGED;
         } else {
            boolean removed = this.decrementBlockCount(oldInternalId);
            this.incrementBlockCount(newInternalId);
            this.blocks[index] = newInternalId;
            return removed ? AbstractSectionPalette.SetResult.ADDED_OR_REMOVED : AbstractSectionPalette.SetResult.CHANGED;
         }
      } else {
         int next = this.nextInternalId(oldInternalId);
         if (!this.isValidInternalId(next)) {
            return AbstractSectionPalette.SetResult.REQUIRES_PROMOTE;
         } else {
            this.decrementBlockCount(oldInternalId);
            short newInternalId = (short)next;
            this.createBlockId(newInternalId, id);
            this.blocks[index] = newInternalId;
            return AbstractSectionPalette.SetResult.ADDED_OR_REMOVED;
         }
      }
   }

   private int nextInternalId(short oldInternalId) {
      return this.internalIdCount.get(oldInternalId) == 1 ? oldInternalId : this.internalIdSet.nextClearBit(0);
   }

   private void createBlockId(short internalId, int blockId) {
      this.internalToExternal.put(internalId, blockId);
      this.externalToInternal.put(blockId, internalId);
      this.internalIdSet.set(internalId);
      this.internalIdCount.put(internalId, (short)1);
   }

   private boolean decrementBlockCount(short internalId) {
      short oldCount = this.internalIdCount.get(internalId);
      if (oldCount == 1) {
         this.internalIdCount.remove(internalId);
         int externalId = this.internalToExternal.remove(internalId);
         this.externalToInternal.remove(externalId);
         this.internalIdSet.clear(internalId);
         return true;
      } else {
         this.internalIdCount.mergeShort(internalId, (short)1, NumberUtil::subtract);
         return false;
      }
   }

   private void incrementBlockCount(short internalId) {
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
         short internalId = this.externalToInternal.get(id);
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
      ObjectIterator var2 = this.internalIdCount.short2ShortEntrySet().iterator();

      while (var2.hasNext()) {
         it.unimi.dsi.fastutil.shorts.Short2ShortMap.Entry entry = (it.unimi.dsi.fastutil.shorts.Short2ShortMap.Entry)var2.next();
         short internalId = entry.getShortKey();
         short count = entry.getShortValue();
         int externalId = this.internalToExternal.get(internalId);
         map.put(externalId, count);
      }

      return map;
   }

   @Override
   public boolean shouldDemote() {
      return this.count() <= 251;
   }

   @Nonnull
   public ByteSectionPalette demote() {
      return ByteSectionPalette.fromShortPalette(this);
   }

   @Override
   public AbstractSectionPalette promote() {
      throw new UnsupportedOperationException("Short palette cannot be promoted.");
   }

   private boolean isValidInternalId(int internalId) {
      return (internalId & 65535) == internalId;
   }

   @Override
   public void find(@Nonnull IntList ids, @Nonnull IntConsumer indexConsumer) {
      ShortSet internalIds = this.buildInternalShortSet(ids);
      if (!internalIds.isEmpty()) {
         int index = 0;
         short type = this.blocks[0];

         while (index < 32768) {
            int start = index;
            short runType = type;

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
      ShortSet internalIds = this.buildInternalShortSet(ids);
      if (!internalIds.isEmpty()) {
         int index = 0;
         short type = this.blocks[0];

         while (index < 32768) {
            int start = index;
            short runType = type;

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

   private ShortSet buildInternalShortSet(IntList ids) {
      ShortSet internalIds = PaletteSetProvider.get().getShortSet(ids.size());

      for (int i = 0; i < ids.size(); i++) {
         short internal = this.externalToInternal.getOrDefault(ids.getInt(i), (short)-32768);
         if (internal != -32768) {
            internalIds.add(internal);
         }
      }

      return internalIds;
   }

   @Override
   public int serializedPacketByteSize() {
      return 2 + 8 * this.internalToExternal.size() + this.blocks.length * 2;
   }

   @Override
   public int serializeForPacket(MemorySegment memorySegment, int baseOffset) {
      memorySegment.set(MemorySegmentUtil.SHORT_LE, (long)baseOffset, (short)this.internalToExternal.size());
      int offset = baseOffset + 2;

      for (ObjectIterator var4 = this.internalToExternal.short2IntEntrySet().iterator(); var4.hasNext(); offset += 8) {
         it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry entry = (it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry)var4.next();
         short internalId = entry.getShortKey();
         int externalId = entry.getIntValue();
         memorySegment.set(MemorySegmentUtil.SHORT_LE, (long)offset, internalId);
         memorySegment.set(MemorySegmentUtil.INT_LE, (long)(offset + 2), externalId);
         memorySegment.set(MemorySegmentUtil.SHORT_LE, (long)(offset + 2 + 4), this.internalIdCount.get(internalId));
      }

      MemorySegment.copy(this.blocks, 0, memorySegment, MemorySegmentUtil.SHORT_LE, offset, this.blocks.length);
      offset += this.blocks.length * 2;
      return offset - baseOffset;
   }

   @Override
   public int serializedByteSize(AbstractSectionPalette.KeyMemorySerializer keySerializer) {
      int size = 2 + 4 * this.internalToExternal.size() + this.blocks.length * 2;
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
      ObjectIterator var5 = this.internalToExternal.short2IntEntrySet().iterator();

      while (var5.hasNext()) {
         it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry entry = (it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry)var5.next();
         short internalId = entry.getShortKey();
         int externalId = entry.getIntValue();
         memorySegment.set(MemorySegmentUtil.SHORT_BE, (long)offset, internalId);
         offset += 2 + keySerializer.serialize(memorySegment, offset + 2, externalId);
         memorySegment.set(MemorySegmentUtil.SHORT_BE, (long)offset, this.internalIdCount.get(internalId));
         offset += 2;
      }

      MemorySegment.copy(this.blocks, 0, memorySegment, MemorySegmentUtil.SHORT_BE, offset, this.blocks.length);
      offset += this.blocks.length * 2;
      return offset - baseOffset;
   }

   @Override
   public int deserialize(AbstractSectionPalette.KeyMemoryDeserializer deserializer, MemorySegment memorySegment, int baseOffset) {
      this.externalToInternal.clear();
      this.internalToExternal.clear();
      this.internalIdSet.clear();
      this.internalIdCount.clear();
      Short2ShortMap internalIdRemapping = null;
      int blockCount = memorySegment.get(MemorySegmentUtil.SHORT_BE, (long)baseOffset);
      int offset = baseOffset + 2;

      for (int i = 0; i < blockCount; i++) {
         short internalId = memorySegment.get(MemorySegmentUtil.SHORT_BE, (long)offset);
         offset += 2;
         int externalId = deserializer.deserialize(memorySegment, offset);
         offset += deserializer.keySize(memorySegment, offset);
         short count = memorySegment.get(MemorySegmentUtil.SHORT_BE, (long)offset);
         offset += 2;
         if (this.externalToInternal.containsKey(externalId)) {
            short existingInternalId = this.externalToInternal.get(externalId);
            if (internalIdRemapping == null) {
               internalIdRemapping = new Short2ShortOpenHashMap();
            }

            internalIdRemapping.put(internalId, existingInternalId);
            this.internalIdCount.mergeShort(existingInternalId, count, NumberUtil::sum);
         } else {
            this.externalToInternal.put(externalId, internalId);
            this.internalToExternal.put(internalId, externalId);
            this.internalIdSet.set(internalId);
            this.internalIdCount.put(internalId, count);
         }
      }

      MemorySegment.copy(memorySegment, MemorySegmentUtil.SHORT_BE, offset, this.blocks, 0, this.blocks.length);
      offset += (int)(this.blocks.length * MemorySegmentUtil.SHORT_BE.byteSize());
      if (internalIdRemapping != null) {
         for (int ix = 0; ix < 32768; ix++) {
            short oldInternalId = this.blocks[ix];
            if (internalIdRemapping.containsKey(oldInternalId)) {
               this.blocks[ix] = internalIdRemapping.get(oldInternalId);
            }
         }
      }

      return offset - baseOffset;
   }

   @Nonnull
   public static ShortSectionPalette fromBytePalette(@Nonnull ByteSectionPalette section) {
      Int2ShortMap shortExternalToInternal = new Int2ShortOpenHashMap(section.count());
      Short2IntMap shortInternalToExternal = new Short2IntOpenHashMap(section.count());
      BitSet shortInternalIdSet = new BitSet(section.count());
      Short2ShortMap shortInternalIdCount = new Short2ShortOpenHashMap(section.count());
      ObjectIterator newBlocks = section.internalToExternal.byte2IntEntrySet().iterator();

      while (newBlocks.hasNext()) {
         it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry entry = (it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry)newBlocks.next();
         short internal = (short)(entry.getByteKey() & 255);
         int external = entry.getIntValue();
         shortInternalToExternal.put(internal, external);
         shortExternalToInternal.put(external, internal);
         shortInternalIdSet.set(internal);
         shortInternalIdCount.put(internal, section.internalIdCount.get(entry.getByteKey()));
      }

      short[] newBlocksx = new short[32768];

      for (int i = 0; i < 32768; i++) {
         newBlocksx[i] = (short)(section.blocks[i] & 255);
      }

      return new ShortSectionPalette(shortExternalToInternal, shortInternalToExternal, shortInternalIdSet, shortInternalIdCount, newBlocksx);
   }
}
