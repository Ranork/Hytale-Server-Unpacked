package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.common.util.BitUtil;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.bytes.Byte2ShortMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.BitSet;
import javax.annotation.Nonnull;

public class HalfByteSectionPalette extends AbstractByteSectionPalette {
   private static final int KEY_MASK = 15;
   public static final int MAX_SIZE = 16;

   public HalfByteSectionPalette() {
      super(new byte[16384]);
   }

   public HalfByteSectionPalette(
      Int2ByteMap externalToInternal, Byte2IntMap internalToExternal, BitSet internalIdSet, Byte2ShortMap internalIdCount, byte[] blocks
   ) {
      super(externalToInternal, internalToExternal, internalIdSet, internalIdCount, blocks);
   }

   public HalfByteSectionPalette(@Nonnull int[] data, int[] unique, int count) {
      super(new byte[16384], data, unique, count);
   }

   @Nonnull
   @Override
   public PaletteType getPaletteType() {
      return PaletteType.HalfByte;
   }

   @Override
   protected void set0(int idx, byte b) {
      BitUtil.setNibble(this.blocks, idx, b);
   }

   @Override
   protected byte get0(int idx) {
      return BitUtil.getNibble(this.blocks, idx);
   }

   @Override
   public boolean shouldDemote() {
      return this.isSolid(0);
   }

   @Nonnull
   @Override
   public ISectionPalette demote() {
      return EmptySectionPalette.INSTANCE;
   }

   @Nonnull
   public ByteSectionPalette promote() {
      return ByteSectionPalette.fromHalfBytePalette(this);
   }

   @Override
   protected boolean isValidInternalId(int internalId) {
      return (internalId & 15) == internalId;
   }

   @Override
   protected int unsignedInternalId(byte internalId) {
      return internalId & 15;
   }

   private static int sUnsignedInternalId(byte internalId) {
      return internalId & 15;
   }

   @Nonnull
   public static HalfByteSectionPalette fromBytePalette(@Nonnull ByteSectionPalette section) {
      if (section.count() > 16) {
         throw new IllegalStateException("Cannot demote byte palette to half byte palette. Too many blocks! Count: " + section.count());
      } else {
         HalfByteSectionPalette halfByteSection = new HalfByteSectionPalette();
         Byte2ByteMap internalIdRemapping = new Byte2ByteOpenHashMap();
         halfByteSection.internalToExternal.clear();
         halfByteSection.externalToInternal.clear();
         halfByteSection.internalIdSet.clear();
         ObjectIterator i = section.internalToExternal.byte2IntEntrySet().iterator();

         while (i.hasNext()) {
            Entry entry = (Entry)i.next();
            byte oldInternalId = entry.getByteKey();
            int externalId = entry.getIntValue();
            byte newInternalId = (byte)halfByteSection.internalIdSet.nextClearBit(0);
            halfByteSection.internalIdSet.set(sUnsignedInternalId(newInternalId));
            internalIdRemapping.put(oldInternalId, newInternalId);
            halfByteSection.internalToExternal.put(newInternalId, externalId);
            halfByteSection.externalToInternal.put(externalId, newInternalId);
         }

         halfByteSection.internalIdCount.clear();
         i = section.internalIdCount.byte2ShortEntrySet().iterator();

         while (i.hasNext()) {
            it.unimi.dsi.fastutil.bytes.Byte2ShortMap.Entry entry = (it.unimi.dsi.fastutil.bytes.Byte2ShortMap.Entry)i.next();
            byte internalId = entry.getByteKey();
            short count = entry.getShortValue();
            byte newInternalId = internalIdRemapping.get(internalId);
            halfByteSection.internalIdCount.put(newInternalId, count);
         }

         for (int ix = 0; ix < section.blocks.length; ix++) {
            byte internalId = section.blocks[ix];
            byte byteInternalId = internalIdRemapping.get(internalId);
            halfByteSection.set0(ix, byteInternalId);
         }

         return halfByteSection;
      }
   }
}
