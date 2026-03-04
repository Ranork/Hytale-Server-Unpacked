package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResourceSlotTransaction extends SlotTransaction {
   @Nonnull
   private final ResourceQuantity query;
   private final int remainder;
   private final int consumed;

   public ResourceSlotTransaction(
      boolean succeeded,
      @Nonnull ActionType action,
      short slot,
      @Nullable ItemStack slotBefore,
      @Nullable ItemStack slotAfter,
      @Nullable ItemStack output,
      boolean allOrNothing,
      boolean exactAmount,
      boolean filter,
      @Nonnull ResourceQuantity query,
      int remainder,
      int consumed
   ) {
      super(succeeded, action, slot, slotBefore, slotAfter, output, allOrNothing, exactAmount, filter);
      this.query = query;
      this.remainder = remainder;
      this.consumed = consumed;
   }

   @Nonnull
   public ResourceQuantity getQuery() {
      return this.query;
   }

   public int getRemainder() {
      return this.remainder;
   }

   public int getConsumed() {
      return this.consumed;
   }

   @Nonnull
   public ResourceSlotTransaction toParent(ItemContainer parent, short start, ItemContainer container) {
      short newSlot = (short)(start + this.getSlot());
      return new ResourceSlotTransaction(
         this.succeeded(),
         this.getAction(),
         newSlot,
         this.getSlotBefore(),
         this.getSlotAfter(),
         this.getOutput(),
         this.isAllOrNothing(),
         this.isExactAmount(),
         this.isFilter(),
         this.query,
         this.remainder,
         this.consumed
      );
   }

   @Nullable
   public ResourceSlotTransaction fromParent(ItemContainer parent, short start, @Nonnull ItemContainer container) {
      short newSlot = (short)(this.getSlot() - start);
      return newSlot >= 0 && newSlot < container.getCapacity()
         ? new ResourceSlotTransaction(
            this.succeeded(),
            this.getAction(),
            newSlot,
            this.getSlotBefore(),
            this.getSlotAfter(),
            this.getOutput(),
            this.isAllOrNothing(),
            this.isExactAmount(),
            this.isFilter(),
            this.query,
            this.remainder,
            this.consumed
         )
         : null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ResourceSlotTransaction{query=" + this.query + ", remainder=" + this.remainder + ", consumed=" + this.consumed + "} " + super.toString();
   }
}
