package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.common.util.BitSetUtil;
import com.hypixel.hytale.protocol.packets.world.PaletteType;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.bytes.Byte2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ByteMap;
import it.unimi.dsi.fastutil.shorts.Short2ByteOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry;
import java.util.BitSet;
import javax.annotation.Nonnull;

public class ByteSectionPalette extends AbstractByteSectionPalette {
   private static final int KEY_MASK = 255;
   public static final int MAX_SIZE = 256;
   public static final int DEMOTE_SIZE = 14;

   public ByteSectionPalette() {
      super(new byte[32768]);
   }

   public ByteSectionPalette(Int2ByteMap externalToInternal, Byte2IntMap internalToExternal, BitSet internalIdSet, Byte2ShortMap internalIdCount, byte[] blocks) {
      super(externalToInternal, internalToExternal, internalIdSet, internalIdCount, blocks);
   }

   public ByteSectionPalette(@Nonnull int[] data, int[] unique, int count) {
      super(new byte[32768], data, unique, count);
   }

   @Nonnull
   @Override
   public PaletteType getPaletteType() {
      return PaletteType.Byte;
   }

   @Override
   protected byte get0(int idx) {
      return this.blocks[idx];
   }

   @Override
   protected void set0(int idx, byte b) {
      this.blocks[idx] = b;
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

   @Override
   protected boolean isValidInternalId(int internalId) {
      return (internalId & 0xFF) == internalId;
   }

   @Override
   protected int unsignedInternalId(byte internalId) {
      return internalId & 0xFF;
   }

   private static int sUnsignedInternalId(byte internalId) {
      return internalId & 0xFF;
   }

   @Nonnull
   public static ByteSectionPalette fromHalfBytePalette(@Nonnull HalfByteSectionPalette section) {
      ByteSectionPalette byteSection = new ByteSectionPalette();
      byteSection.externalToInternal.clear();
      byteSection.externalToInternal.putAll(section.externalToInternal);
      byteSection.internalToExternal.clear();
      byteSection.internalToExternal.putAll(section.internalToExternal);
      BitSetUtil.copyValues(section.internalIdSet, byteSection.internalIdSet);
      byteSection.internalIdCount.clear();
      byteSection.internalIdCount.putAll(section.internalIdCount);

      for (int i = 0; i < byteSection.blocks.length; i++) {
         byteSection.blocks[i] = section.get0(i);
      }

      return byteSection;
   }

   @Nonnull
   public static ByteSectionPalette fromShortPalette(@Nonnull ShortSectionPalette section) {
      if (section.count() > 256) {
         throw new IllegalStateException("Cannot demote short palette to byte palette. Too many blocks! Count: " + section.count());
      } else {
         ByteSectionPalette byteSection = new ByteSectionPalette();
         Short2ByteMap internalIdRemapping = new Short2ByteOpenHashMap();
         byteSection.internalToExternal.clear();
         byteSection.externalToInternal.clear();
         byteSection.internalIdSet.clear();
         byteSection.internalIdCount.clear();
         ObjectIterator i = section.internalToExternal.short2IntEntrySet().iterator();

         while (i.hasNext()) {
            Entry entry = (Entry)i.next();
            short oldInternalId = entry.getShortKey();
            int externalId = entry.getIntValue();
            byte newInternalId = (byte)byteSection.internalIdSet.nextClearBit(0);
            byteSection.internalIdSet.set(sUnsignedInternalId(newInternalId));
            internalIdRemapping.put(oldInternalId, newInternalId);
            byteSection.internalToExternal.put(newInternalId, externalId);
            byteSection.externalToInternal.put(externalId, newInternalId);
            byteSection.internalIdCount.put(newInternalId, section.internalIdCount.get(oldInternalId));
         }

         for (int ix = 0; ix < 32768; ix++) {
            short internalId = section.blocks[ix];
            byte byteInternalId = internalIdRemapping.get(internalId);
            byteSection.blocks[ix] = byteInternalId;
         }

         return byteSection;
      }
   }
}
