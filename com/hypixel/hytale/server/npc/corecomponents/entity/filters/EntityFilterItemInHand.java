package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterItemInHand;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFilterItemInHand extends EntityFilterBase {
   public static final int COST = 300;
   @Nullable
   protected final List<String> items;
   protected final EntityFilterItemInHand.WieldingHand hand;

   public EntityFilterItemInHand(@Nonnull BuilderEntityFilterItemInHand builder, @Nonnull BuilderSupport builderSupport) {
      String[] itemArray = builder.getItems(builderSupport);
      this.items = itemArray != null ? List.of(itemArray) : null;
      this.hand = builder.getHand(builderSupport);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      return switch (this.hand) {
         case Main -> {
            ItemStack itemInHand = InventoryComponent.getItemInHand(store, targetRef);
            yield itemInHand != null && InventoryHelper.matchesItem(this.items, itemInHand);
         }
         case OffHand -> {
            InventoryComponent.Utility utilityComponent = store.getComponent(targetRef, InventoryComponent.Utility.getComponentType());
            ItemStack utilityItem = utilityComponent != null ? utilityComponent.getActiveItem() : null;
            yield utilityItem != null && InventoryHelper.matchesItem(this.items, utilityItem);
         }
         default -> {
            ItemStack itemInHand = InventoryComponent.getItemInHand(store, targetRef);
            InventoryComponent.Utility utilityComponent = store.getComponent(targetRef, InventoryComponent.Utility.getComponentType());
            ItemStack utilityItem = utilityComponent != null ? utilityComponent.getActiveItem() : null;
            yield itemInHand != null && InventoryHelper.matchesItem(this.items, itemInHand)
               || utilityItem != null && InventoryHelper.matchesItem(this.items, utilityItem);
         }
      };
   }

   @Override
   public int cost() {
      return 300;
   }

   public static enum WieldingHand implements Supplier<String> {
      Main("The main hand"),
      OffHand("The off-hand"),
      Both("Both hands");

      private final String description;

      private WieldingHand(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
