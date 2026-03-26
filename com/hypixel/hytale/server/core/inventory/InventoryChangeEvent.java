package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class InventoryChangeEvent extends EcsEvent {
   private final ComponentType<EntityStore, ? extends InventoryComponent> componentType;
   private final InventoryComponent inventory;
   private final ItemContainer itemContainer;
   private final Transaction transaction;

   public InventoryChangeEvent(
      ComponentType<EntityStore, ? extends InventoryComponent> componentType,
      InventoryComponent inventory,
      ItemContainer itemContainer,
      Transaction transaction
   ) {
      this.componentType = componentType;
      this.inventory = inventory;
      this.itemContainer = itemContainer;
      this.transaction = transaction;
   }

   public ComponentType<EntityStore, ? extends InventoryComponent> getComponentType() {
      return this.componentType;
   }

   public InventoryComponent getInventory() {
      return this.inventory;
   }

   public ItemContainer getItemContainer() {
      return this.itemContainer;
   }

   public Transaction getTransaction() {
      return this.transaction;
   }
}
