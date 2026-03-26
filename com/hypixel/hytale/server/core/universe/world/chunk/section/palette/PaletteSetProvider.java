package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;

public class PaletteSetProvider {
   private static final ThreadLocal<PaletteSetProvider> LOCAL = ThreadLocal.withInitial(PaletteSetProvider::new);
   private final ByteOpenHashSet byteHashSet = new ByteOpenHashSet();
   private final ShortOpenHashSet shortHashSet = new ShortOpenHashSet();

   public ByteSet getByteSet(int size) {
      this.byteHashSet.clear();
      this.byteHashSet.ensureCapacity(size);
      return this.byteHashSet;
   }

   public ShortSet getShortSet(int size) {
      this.shortHashSet.clear();
      this.shortHashSet.ensureCapacity(size);
      return this.shortHashSet;
   }

   protected static PaletteSetProvider get() {
      return LOCAL.get();
   }
}
