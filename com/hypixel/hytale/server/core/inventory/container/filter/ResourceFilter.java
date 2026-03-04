package com.hypixel.hytale.server.core.inventory.container.filter;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import javax.annotation.Nullable;

public class ResourceFilter implements ItemSlotFilter {
   private final ResourceQuantity resource;

   public ResourceFilter(ResourceQuantity resource) {
      this.resource = resource;
   }

   @Override
   public boolean test(@Nullable Item item) {
      return item == null || this.resource.getResourceType(item) != null;
   }

   public ResourceQuantity getResource() {
      return this.resource;
   }
}
