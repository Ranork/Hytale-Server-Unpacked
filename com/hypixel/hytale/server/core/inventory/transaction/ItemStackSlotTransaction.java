package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemStackSlotTransaction extends SlotTransaction {
   private final boolean addToExistingSlot;
   @Nullable
   private final ItemStack query;
   @Nullable
   private final ItemStack remainder;

   public ItemStackSlotTransaction(
      boolean succeeded,
      @Nonnull ActionType action,
      short slot,
      @Nullable ItemStack slotBefore,
      @Nullable ItemStack slotAfter,
      @Nullable ItemStack output,
      boolean allOrNothing,
      boolean exactAmount,
      boolean filter,
      boolean addToExistingSlot,
      @Nullable ItemStack query,
      @Nullable ItemStack remainder
   ) {
      super(succeeded, action, slot, slotBefore, slotAfter, output, allOrNothing, exactAmount, filter);
      this.addToExistingSlot = addToExistingSlot;
      this.query = query;
      this.remainder = remainder;
   }

   public boolean isAddToExistingSlot() {
      return this.addToExistingSlot;
   }

   @Nullable
   public ItemStack getQuery() {
      return this.query;
   }

   @Nullable
   public ItemStack getRemainder() {
      return this.remainder;
   }

   @Nonnull
   public ItemStackSlotTransaction toParent(ItemContainer parent, short start, ItemContainer container) {
      short newSlot = (short)(start + this.getSlot());
      return new ItemStackSlotTransaction(
         this.succeeded(),
         this.getAction(),
         newSlot,
         this.getSlotBefore(),
         this.getSlotAfter(),
         this.getOutput(),
         this.isAllOrNothing(),
         this.isExactAmount(),
         this.isFilter(),
         this.addToExistingSlot,
         this.query,
         this.remainder
      );
   }

   @Nullable
   public ItemStackSlotTransaction fromParent(ItemContainer parent, short start, @Nonnull ItemContainer container) {
      short newSlot = (short)(this.getSlot() - start);
      return newSlot >= 0 && newSlot < container.getCapacity()
         ? new ItemStackSlotTransaction(
            this.succeeded(),
            this.getAction(),
            newSlot,
            this.getSlotBefore(),
            this.getSlotAfter(),
            this.getOutput(),
            this.isAllOrNothing(),
            this.isExactAmount(),
            this.isFilter(),
            this.addToExistingSlot,
            this.query,
            this.remainder
         )
         : null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemStackSlotTransaction{addToExistingSlot="
         + this.addToExistingSlot
         + ", query="
         + this.query
         + ", remainder="
         + this.remainder
         + "} "
         + super.toString();
   }
}
