package com.hypixel.hytale.server.core.inventory.container.filter;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import javax.annotation.Nullable;

public class TagFilter implements ItemSlotFilter {
   private final int tagIndex;

   public TagFilter(int tagIndex) {
      this.tagIndex = tagIndex;
   }

   @Override
   public boolean test(@Nullable Item item) {
      return item == null || item.getData().getExpandedTagIndexes().contains(this.tagIndex);
   }
}
