package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.math.util.NumberUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2ShortMap;
import it.unimi.dsi.fastutil.bytes.Byte2ShortOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2ShortMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.BitSet;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;

public abstract class AbstractByteSectionPalette implements ISectionPalette {
   protected final Int2ByteMap externalToInternal;
   protected final Byte2IntMap internalToExternal;
   protected final BitSet internalIdSet;
   protected final Byte2ShortMap internalIdCount;
   protected final byte[] blocks;

   protected AbstractByteSectionPalette(byte[] blocks) {
      this(new Int2ByteOpenHashMap(), new Byte2IntOpenHashMap(), new BitSet(), new Byte2ShortOpenHashMap(), blocks);
      this.externalToInternal.put(0, (byte)0);
      this.internalToExternal.put((byte)0, 0);
      this.internalIdSet.set(0);
      this.internalIdCount.put((byte)0, (short)-32768);
   }

   public AbstractByteSectionPalette(byte[] blocks, @Nonnull int[] data, int[] unique, int count) {
      this(new Int2ByteOpenHashMap(count), new Byte2IntOpenHashMap(count), new BitSet(count), new Byte2ShortOpenHashMap(count), blocks);

      for (int internalId = 0; internalId < count; internalId++) {
         int blockId = unique[internalId];
         this.internalToExternal.put((byte)internalId, blockId);
         this.externalToInternal.put(blockId, (byte)internalId);
         this.internalIdSet.set(this.unsignedInternalId((byte)internalId));
         this.internalIdCount.put((byte)internalId, (short)0);
      }

      for (int index = 0; index < data.length; index++) {
         int id = data[index];
         byte internalId = this.externalToInternal.get(id);
         this.incrementBlockCount(internalId);
         this.set0(index, internalId);
      }
   }

   protected AbstractByteSectionPalette(
      Int2ByteMap externalToInternal, Byte2IntMap internalToExternal, BitSet internalIdSet, Byte2ShortMap internalIdCount, byte[] blocks
   ) {
      this.externalToInternal = externalToInternal;
      this.internalToExternal = internalToExternal;
      this.internalIdSet = internalIdSet;
      this.internalIdCount = internalIdCount;
      this.blocks = blocks;
   }

   @Override
   public int get(int index) {
      byte internalId = this.get0(index);
      return this.internalToExternal.get(internalId);
   }

   @Nonnull
   @Override
   public ISectionPalette.SetResult set(int index, int id) {
      byte oldInternalId = this.get0(index);
      if (this.externalToInternal.containsKey(id)) {
         byte newInternalId = this.externalToInternal.get(id);
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
            byte newInternalId = (byte)nextInternalId;
            this.createBlockId(newInternalId, id);
            this.set0(index, newInternalId);
            return ISectionPalette.SetResult.ADDED_OR_REMOVED;
         }
      }
   }

   protected abstract byte get0(int var1);

   protected abstract void set0(int var1, byte var2);

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
   public void forEachValue(IntConsumer consumer) {
      this.externalToInternal.keySet().forEach(consumer);
   }

   @Nonnull
   @Override
   public Int2ShortMap valueCounts() {
      Int2ShortMap map = new Int2ShortOpenHashMap();
      ObjectIterator var2 = this.internalIdCount.byte2ShortEntrySet().iterator();

      while (var2.hasNext()) {
         Entry entry = (Entry)var2.next();
         byte internalId = entry.getByteKey();
         short count = entry.getShortValue();
         int externalId = this.internalToExternal.get(internalId);
         map.put(externalId, count);
      }

      return map;
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

   private int nextInternalId(byte oldInternalId) {
      return this.internalIdCount.get(oldInternalId) == 1 ? this.unsignedInternalId(oldInternalId) : this.internalIdSet.nextClearBit(0);
   }

   protected abstract boolean isValidInternalId(int var1);

   protected abstract int unsignedInternalId(byte var1);

   @Override
   public void serializeForPacket(@Nonnull ByteBuf buf) {
      buf.writeShortLE(this.internalToExternal.size());
      ObjectIterator var2 = this.internalToExternal.byte2IntEntrySet().iterator();

      while (var2.hasNext()) {
         it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry entry = (it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry)var2.next();
         byte internalId = entry.getByteKey();
         int externalId = entry.getIntValue();
         buf.writeByte(internalId);
         buf.writeIntLE(externalId);
         buf.writeShortLE(this.internalIdCount.get(internalId));
      }

      buf.writeBytes(this.blocks);
   }

   @Override
   public void serialize(@Nonnull ISectionPalette.KeySerializer keySerializer, @Nonnull ByteBuf buf) {
      buf.writeShort(this.internalToExternal.size());
      ObjectIterator var3 = this.internalToExternal.byte2IntEntrySet().iterator();

      while (var3.hasNext()) {
         it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry entry = (it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry)var3.next();
         byte internalId = entry.getByteKey();
         int externalId = entry.getIntValue();
         buf.writeByte(internalId);
         keySerializer.serialize(buf, externalId);
         buf.writeShort(this.internalIdCount.get(internalId));
      }

      buf.writeBytes(this.blocks);
   }

   @Override
   public void deserialize(@Nonnull ToIntFunction<ByteBuf> deserializer, @Nonnull ByteBuf buf, int version) {
      this.externalToInternal.clear();
      this.internalToExternal.clear();
      this.internalIdSet.clear();
      this.internalIdCount.clear();
      Byte2ByteMap internalIdRemapping = null;
      int blockCount = buf.readShort();

      for (int i = 0; i < blockCount; i++) {
         byte internalId = buf.readByte();
         int externalId = deserializer.applyAsInt(buf);
         short count = buf.readShort();
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

      buf.readBytes(this.blocks);
      if (internalIdRemapping != null) {
         for (int ix = 0; ix < 32768; ix++) {
            byte oldInternalId = this.get0(ix);
            if (internalIdRemapping.containsKey(oldInternalId)) {
               this.set0(ix, internalIdRemapping.get(oldInternalId));
            }
         }
      }
   }

   @Override
   public void find(@Nonnull IntList ids, @Nonnull IntSet internalIdHolder, @Nonnull IntConsumer indexConsumer) {
      for (int i = 0; i < ids.size(); i++) {
         byte internal = this.externalToInternal.getOrDefault(ids.getInt(i), (byte)-128);
         if (internal != -128) {
            internalIdHolder.add(internal);
         }
      }

      for (int ix = 0; ix < 32768; ix++) {
         byte type = this.get0(ix);
         if (internalIdHolder.contains(type)) {
            indexConsumer.accept(ix);
         }
      }
   }
}
