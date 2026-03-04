package com.hypixel.hytale.server.spawning.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;

public class FloodFillEntryPoolSimple {
   private static final int ENTRY_SIZE = 5;
   private final List<int[]> entryPool = new ObjectArrayList();

   public int[] allocate() {
      return this.entryPool.isEmpty() ? new int[5] : this.entryPool.removeLast();
   }

   public void deallocate(int[] entry) {
      this.entryPool.add(entry);
   }
}
