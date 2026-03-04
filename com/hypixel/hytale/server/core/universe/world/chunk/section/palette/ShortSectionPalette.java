package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.protocol.packets.world.PaletteType;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import java.util.BitSet;
import javax.annotation.Nonnull;

public class ShortSectionPalette extends AbstractShortSectionPalette {
   private static final int KEY_MASK = 65535;
   public static final int MAX_SIZE = 65536;
   public static final int DEMOTE_SIZE = 251;

   public ShortSectionPalette() {
      super(new short[32768]);
   }

   public ShortSectionPalette(
      Int2ShortMap externalToInternal, Short2IntMap internalToExternal, BitSet internalIdSet, Short2ShortMap internalIdCount, short[] blocks
   ) {
      super(externalToInternal, internalToExternal, internalIdSet, internalIdCount, blocks);
   }

   public ShortSectionPalette(@Nonnull int[] data, int[] unique, int count) {
      super(new short[32768], data, unique, count);
   }

   @Nonnull
   @Override
   public PaletteType getPaletteType() {
      return PaletteType.Short;
   }

   @Override
   protected short get0(int idx) {
      return this.blocks[idx];
   }

   @Override
   protected void set0(int idx, short s) {
      this.blocks[idx] = s;
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
   public ISectionPalette promote() {
      throw new UnsupportedOperationException("Short palette cannot be promoted.");
   }

   @Override
   protected boolean isValidInternalId(int internalId) {
      return (internalId & 65535) == internalId;
   }

   @Nonnull
   public static ShortSectionPalette fromBytePalette(@Nonnull ByteSectionPalette section) {
      Int2ShortMap shortExternalToInternal = new Int2ShortOpenHashMap();
      Short2IntMap shortInternalToExternal = new Short2IntOpenHashMap();
      BitSet shortInternalIdSet = new BitSet(section.internalToExternal.size());
      Short2ShortMap shortInternalIdCount = new Short2ShortOpenHashMap();
      ObjectIterator shortBlocks = section.internalToExternal.byte2IntEntrySet().iterator();

      while (shortBlocks.hasNext()) {
         Entry entry = (Entry)shortBlocks.next();
         short internal = (short)(entry.getByteKey() & 255);
         int external = entry.getIntValue();
         shortInternalToExternal.put(internal, external);
         shortExternalToInternal.put(external, internal);
         shortInternalIdSet.set(internal);
         shortInternalIdCount.put(internal, section.internalIdCount.get(entry.getByteKey()));
      }

      short[] shortBlocksx = new short[32768];

      for (int i = 0; i < 32768; i++) {
         shortBlocksx[i] = (short)(section.blocks[i] & 255);
      }

      return new ShortSectionPalette(shortExternalToInternal, shortInternalToExternal, shortInternalIdSet, shortInternalIdCount, shortBlocksx);
   }
}
