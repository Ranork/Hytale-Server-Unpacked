package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterInventory;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFilterInventory extends EntityFilterBase {
   public static final int COST = 300;
   @Nullable
   protected final List<String> items;
   protected final int minCount;
   protected final int maxCount;
   protected final int minFreeSlots;
   protected final int maxFreeSlots;
   protected final boolean checkFreeSlots;

   public EntityFilterInventory(@Nonnull BuilderEntityFilterInventory builder, @Nonnull BuilderSupport support) {
      String[] itemArray = builder.getItems(support);
      this.items = itemArray != null ? List.of(itemArray) : null;
      int[] countRange = builder.getCount(support);
      this.minCount = countRange[0];
      this.maxCount = countRange[1];
      int[] freeSlotsRange = builder.getFreeSlotsRange(support);
      this.minFreeSlots = freeSlotsRange[0];
      this.maxFreeSlots = freeSlotsRange[1];
      this.checkFreeSlots = this.minFreeSlots != BuilderEntityFilterInventory.DEFAULT_FREE_SLOT_RANGE[0]
         || this.maxFreeSlots != BuilderEntityFilterInventory.DEFAULT_FREE_SLOT_RANGE[1];
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      LivingEntity entity = (LivingEntity)EntityUtils.getEntity(targetRef, store);
      Inventory inventory = entity.getInventory();
      CombinedItemContainer container = inventory.getCombinedHotbarUtilityConsumableStorage();
      int count = InventoryHelper.countItems(container, this.items);
      if (count < this.minCount || count > this.maxCount) {
         return false;
      } else if (!this.checkFreeSlots) {
         return true;
      } else {
         int freeSlots = InventoryHelper.countFreeSlots(container);
         return freeSlots >= this.minFreeSlots && freeSlots <= this.maxFreeSlots;
      }
   }

   @Override
   public int cost() {
      return 300;
   }
}
