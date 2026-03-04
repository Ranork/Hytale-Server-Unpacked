package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClearTransaction implements Transaction {
   public static final ClearTransaction EMPTY = new ClearTransaction(true, (short)0, ItemStack.EMPTY_ARRAY);
   private final boolean succeeded;
   private final short start;
   @Nonnull
   private final ItemStack[] items;

   public ClearTransaction(boolean succeeded, short start, @Nonnull ItemStack[] items) {
      this.succeeded = succeeded;
      this.start = start;
      this.items = items;
   }

   @Override
   public boolean succeeded() {
      return this.succeeded;
   }

   @Override
   public boolean wasSlotModified(short slot) {
      if (!this.succeeded) {
         return false;
      } else {
         slot = (short)(slot - this.start);
         return slot >= 0 && slot < this.items.length && this.items[slot] != null && !this.items[slot].isEmpty();
      }
   }

   @Nonnull
   public ItemStack[] getItems() {
      return this.items;
   }

   @Nonnull
   public ClearTransaction toParent(ItemContainer parent, short start, ItemContainer container) {
      short newStart = (short)(start + this.start);
      return new ClearTransaction(this.succeeded, newStart, this.items);
   }

   @Nullable
   public ClearTransaction fromParent(ItemContainer parent, short start, @Nonnull ItemContainer container) {
      short newStart = (short)(this.start - start);
      short capacity = container.getCapacity();
      if (newStart < 0) {
         int from = -newStart;
         return this.items.length + newStart > capacity
            ? new ClearTransaction(this.succeeded, (short)0, Arrays.copyOfRange(this.items, from, from + capacity))
            : new ClearTransaction(this.succeeded, (short)0, Arrays.copyOfRange(this.items, from, this.items.length));
      } else {
         return this.items.length > capacity
            ? new ClearTransaction(this.succeeded, newStart, Arrays.copyOf(this.items, capacity))
            : new ClearTransaction(this.succeeded, newStart, this.items);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ClearTransaction{items=" + Arrays.toString((Object[])this.items) + "}";
   }
}
