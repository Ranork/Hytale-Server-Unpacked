package com.hypixel.hytale.server.npc.asset.builder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import javax.annotation.Nullable;

public class SlotMapper {
   public static final int NO_SLOT = Integer.MIN_VALUE;
   private final Object2IntMap<String> mappings = new Object2IntOpenHashMap();
   @Nullable
   private final Int2ObjectMap<String> nameMap;
   private int nextSlot;

   public SlotMapper() {
      this(false);
   }

   public SlotMapper(boolean trackNames) {
      this.nameMap = trackNames ? new Int2ObjectOpenHashMap() : null;
      this.mappings.defaultReturnValue(Integer.MIN_VALUE);
   }

   public int getSlot(String name) {
      int slot = this.mappings.getInt(name);
      if (slot == Integer.MIN_VALUE) {
         slot = this.nextSlot++;
         this.mappings.put(name, slot);
         if (this.nameMap != null) {
            this.nameMap.put(slot, name);
         }
      }

      return slot;
   }

   public int slotCount() {
      return this.mappings.size();
   }

   @Nullable
   public Object2IntMap<String> getSlotMappings() {
      return this.mappings.isEmpty() ? null : this.mappings;
   }

   @Nullable
   public Int2ObjectMap<String> getNameMap() {
      return this.nameMap;
   }
}
