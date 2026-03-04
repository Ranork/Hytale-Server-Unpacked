package com.hypixel.hytale.server.core.inventory.container;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Set;

public class TestRemoveItemSlotResult {
   Map<Short, Integer> picked = new Object2IntOpenHashMap();
   int quantityRemaining;

   public TestRemoveItemSlotResult(int testQuantityRemaining) {
      this.quantityRemaining = testQuantityRemaining;
   }

   public boolean hasResult() {
      return !this.picked.isEmpty();
   }

   public Set<Short> getPickedSlots() {
      return this.picked.keySet();
   }
}
