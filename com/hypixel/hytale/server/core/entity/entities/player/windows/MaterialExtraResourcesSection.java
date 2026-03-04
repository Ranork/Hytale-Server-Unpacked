package com.hypixel.hytale.server.core.entity.entities.player.windows;

import com.hypixel.hytale.protocol.ExtraResources;
import com.hypixel.hytale.protocol.ItemQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

public class MaterialExtraResourcesSection {
   private boolean valid;
   private ItemContainer itemContainer;
   private ItemQuantity[] extraMaterials;

   public void setExtraMaterials(ItemQuantity[] extraMaterials) {
      this.extraMaterials = extraMaterials;
   }

   public boolean isValid() {
      return this.valid;
   }

   public void setValid(boolean valid) {
      this.valid = valid;
   }

   public ExtraResources toPacket() {
      ExtraResources packet = new ExtraResources();
      packet.resources = this.extraMaterials;
      return packet;
   }

   public ItemContainer getItemContainer() {
      return this.itemContainer;
   }

   public void setItemContainer(ItemContainer itemContainer) {
      this.itemContainer = itemContainer;
   }
}
