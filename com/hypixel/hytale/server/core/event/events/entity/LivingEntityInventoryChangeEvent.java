package com.hypixel.hytale.server.core.event.events.entity;

import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import javax.annotation.Nonnull;

public class LivingEntityInventoryChangeEvent extends EntityEvent<LivingEntity, String> {
   private ItemContainer itemContainer;
   private Transaction transaction;

   public LivingEntityInventoryChangeEvent(LivingEntity entity, ItemContainer itemContainer, Transaction transaction) {
      super(entity);
      this.itemContainer = itemContainer;
      this.transaction = transaction;
   }

   public ItemContainer getItemContainer() {
      return this.itemContainer;
   }

   public Transaction getTransaction() {
      return this.transaction;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LivingEntityInventoryChangeEvent{itemContainer=" + this.itemContainer + ", transaction=" + this.transaction + "} " + super.toString();
   }
}
