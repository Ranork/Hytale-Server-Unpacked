package com.hypixel.hytale.server.core.inventory.container.filter;

import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import javax.annotation.Nullable;

public class ArmorSlotAddFilter implements ItemSlotFilter {
   private final ItemArmorSlot itemArmorSlot;

   public ArmorSlotAddFilter(ItemArmorSlot itemArmorSlot) {
      this.itemArmorSlot = itemArmorSlot;
   }

   @Override
   public boolean test(@Nullable Item item) {
      return item == null || item.getArmor() != null && item.getArmor().getArmorSlot() == this.itemArmorSlot;
   }

   public ItemArmorSlot getItemArmorSlot() {
      return this.itemArmorSlot;
   }
}
