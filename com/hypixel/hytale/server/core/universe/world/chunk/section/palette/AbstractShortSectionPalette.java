package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.function.consumer.BiIntConsumer;
import com.hypixel.hytale.math.util.NumberUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMaps;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
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
import java.util.BitSet;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;

public abstract class AbstractShortSectionPalette implements ISectionPalette {
   protected final Int2ShortMap externalToInternal;
   protected final Short2IntMap internalToExternal;
   protected final BitSet internalIdSet;
   protected final Short2ShortMap internalIdCount;
   protected final short[] blocks;

   public AbstractShortSectionPalette(short[] blocks) {
      this(new Int2ShortOpenHashMap(), new Short2IntOpenHashMap(), new BitSet(), new Short2ShortOpenHashMap(), blocks);
      this.externalToInternal.put(0, (short)0);
      this.internalToExternal.put((short)0, 0);
      this.internalIdSet.set(0);
      this.internalIdCount.put((short)0, (short)-32768);
   }

   public AbstractShortSectionPalette(@Nonnull short[] blocks, @Nonnull int[] data, @Nonnull Int2ShortMap externalIdCounts) {
      this(
         new Int2ShortOpenHashMap(externalIdCounts.size()),
         new Short2IntOpenHashMap(externalIdCounts.size()),
         new BitSet(externalIdCounts.size()),
         new Short2ShortOpenHashMap(externalIdCounts.size()),
         blocks
      );
      ObjectIterator<Entry> externalIdCountIter = Int2ShortMaps.fastIterator(externalIdCounts);

      for (short internalIdCounter = 0; externalIdCountIter.hasNext(); internalIdCounter++) {
         Entry entry = (Entry)externalIdCountIter.next();
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
            this.set0(i, internalId);
         }
      }
   }

   protected AbstractShortSectionPalette(
      Int2ShortMap externalToInternal, Short2IntMap internalToExternal, BitSet internalIdSet, Short2ShortMap internalIdCount, short[] blocks
   ) {
      this.externalToInternal = externalToInternal;
      this.internalToExternal = internalToExternal;
      this.internalIdSet = internalIdSet;
      this.internalIdCount = internalIdCount;
      this.blocks = blocks;
   }

   @Override
   public int get(int index) {
      short internalId = this.get0(index);
      return this.internalToExternal.get(internalId);
   }

   @Nonnull
   @Override
   public ISectionPalette.SetResult set(int index, int id) {
      short oldInternalId = this.get0(index);
      if (this.externalToInternal.containsKey(id)) {
         short newInternalId = this.externalToInternal.get(id);
         if (newInternalId == oldInternalId) {
            return ISectionPalette.SetResult.UNCHANGED;
         } else {
            boolean removed = this.decrementBlockCount(oldInternalId);
            this.incrementBlockCount(newInternalId);
            this.set0(index, newInternalId);
            return removed ? ISectionPalette.SetResult.ADDED_OR_REMOVED : ISectionPalette.SetResult.CHANGED;
         }
      } else {
         int nextInternalId = this.nextInternalId(oldInternalId);
         if (!this.isValidInternalId(nextInternalId)) {
            return ISectionPalette.SetResult.REQUIRES_PROMOTE;
         } else {
            this.decrementBlockCount(oldInternalId);
            short newInternalId = (short)nextInternalId;
            this.createBlockId(newInternalId, id);
            this.set0(index, newInternalId);
            return ISectionPalette.SetResult.ADDED_OR_REMOVED;
         }
      }
   }

   protected abstract short get0(int var1);

   protected abstract void set0(int var1, short var2);

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
   public void forEachValue(IntConsumer consumer) {
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

   private int nextInternalId(short oldInternalId) {
      return this.internalIdCount.get(oldInternalId) == 1 ? oldInternalId : this.internalIdSet.nextClearBit(0);
   }

   protected abstract boolean isValidInternalId(int var1);

   @Override
   public void serializeForPacket(@Nonnull ByteBuf buf) {
      buf.writeShortLE(this.internalToExternal.size());
      ObjectIterator i = this.internalToExternal.short2IntEntrySet().iterator();

      while (i.hasNext()) {
         it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry entry = (it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry)i.next();
         short internalId = entry.getShortKey();
         int externalId = entry.getIntValue();
         buf.writeShortLE(internalId & '\uffff');
         buf.writeIntLE(externalId);
         buf.writeShortLE(this.internalIdCount.get(internalId));
      }

      for (int ix = 0; ix < this.blocks.length; ix++) {
         buf.writeShortLE(this.blocks[ix]);
      }
   }

   @Override
   public void serialize(@Nonnull ISectionPalette.KeySerializer keySerializer, @Nonnull ByteBuf buf) {
      buf.writeShort(this.internalToExternal.size());
      ObjectIterator i = this.internalToExternal.short2IntEntrySet().iterator();

      while (i.hasNext()) {
         it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry entry = (it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry)i.next();
         short internalId = entry.getShortKey();
         int externalId = entry.getIntValue();
         buf.writeShort(internalId & '\uffff');
         keySerializer.serialize(buf, externalId);
         buf.writeShort(this.internalIdCount.get(internalId));
      }

      for (int ix = 0; ix < this.blocks.length; ix++) {
         buf.writeShort(this.blocks[ix]);
      }
   }

   @Override
   public void deserialize(@Nonnull ToIntFunction<ByteBuf> deserializer, @Nonnull ByteBuf buf, int version) {
      this.externalToInternal.clear();
      this.internalToExternal.clear();
      this.internalIdSet.clear();
      this.internalIdCount.clear();
      Short2ShortMap internalIdRemapping = null;
      int blockCount = buf.readShort();

      for (int i = 0; i < blockCount; i++) {
         short internalId = buf.readShort();
         int externalId = deserializer.applyAsInt(buf);
         short count = buf.readShort();
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

      for (int ix = 0; ix < this.blocks.length; ix++) {
         this.blocks[ix] = buf.readShort();
      }

      if (internalIdRemapping != null) {
         for (int ix = 0; ix < 32768; ix++) {
            short oldInternalId = this.get0(ix);
            if (internalIdRemapping.containsKey(oldInternalId)) {
               this.set0(ix, internalIdRemapping.get(oldInternalId));
            }
         }
      }
   }

   @Override
   public void find(@Nonnull IntList ids, @Nonnull IntConsumer indexConsumer) {
      ShortSet internalIds = this.getThreadLocalInternalIdSet(ids);
      if (!internalIds.isEmpty()) {
         int index = 0;
         short type = this.get0(index);

         while (index < 32768) {
            int start = index;
            short runType = type;

            do {
               index++;
            } while (index < 32768 && (type = this.get0(index)) == runType);

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
      ShortSet internalIds = this.getThreadLocalInternalIdSet(ids);
      if (!internalIds.isEmpty()) {
         int index = 0;
         short type = this.get0(index);

         while (index < 32768) {
            int start = index;
            short runType = type;

            do {
               index++;
            } while (index < 32768 && (type = this.get0(index)) == runType);

            if (internalIds.contains(runType)) {
               int external = this.internalToExternal.get(runType);

               for (int i = start; i < index; i++) {
                  indexBlockConsumer.accept(i, external);
               }
            }
         }
      }
   }

   protected ShortSet getThreadLocalInternalIdSet(IntList ids) {
      ShortSet internalIds = PaletteSetProvider.get().getShortSet(ids.size());

      for (int i = 0; i < ids.size(); i++) {
         short internal = this.externalToInternal.getOrDefault(ids.getInt(i), (short)-32768);
         if (internal != -32768) {
            internalIds.add(internal);
         }
      }

      return internalIds;
   }
}
