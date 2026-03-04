package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SlotTransaction implements Transaction {
   public static final SlotTransaction FAILED_ADD = new SlotTransaction(false, ActionType.ADD, (short)-1, null, null, null, false, false, false);
   private final boolean succeeded;
   @Nonnull
   private final ActionType action;
   private final short slot;
   @Nullable
   private final ItemStack slotBefore;
   @Nullable
   private final ItemStack slotAfter;
   @Nullable
   private final ItemStack output;
   private final boolean allOrNothing;
   private final boolean exactAmount;
   private final boolean filter;

   public SlotTransaction(
      boolean succeeded,
      @Nonnull ActionType action,
      short slot,
      @Nullable ItemStack slotBefore,
      @Nullable ItemStack slotAfter,
      @Nullable ItemStack output,
      boolean allOrNothing,
      boolean exactAmount,
      boolean filter
   ) {
      this.succeeded = succeeded;
      this.action = action;
      this.slot = slot;
      this.slotBefore = slotBefore;
      this.slotAfter = slotAfter;
      this.output = output;
      this.allOrNothing = allOrNothing;
      this.exactAmount = exactAmount;
      this.filter = filter;
   }

   @Override
   public boolean succeeded() {
      return this.succeeded;
   }

   @Override
   public boolean wasSlotModified(short slot) {
      return !this.succeeded ? false : this.slot == slot;
   }

   @Nonnull
   public ActionType getAction() {
      return this.action;
   }

   public short getSlot() {
      return this.slot;
   }

   @Nullable
   public ItemStack getSlotBefore() {
      return this.slotBefore;
   }

   @Nullable
   public ItemStack getSlotAfter() {
      return this.slotAfter;
   }

   @Nullable
   public ItemStack getOutput() {
      return this.output;
   }

   public boolean isAllOrNothing() {
      return this.allOrNothing;
   }

   public boolean isExactAmount() {
      return this.exactAmount;
   }

   public boolean isFilter() {
      return this.filter;
   }

   @Nonnull
   public SlotTransaction toParent(ItemContainer parent, short start, ItemContainer container) {
      short newSlot = (short)(start + this.slot);
      return new SlotTransaction(
         this.succeeded, this.action, newSlot, this.slotBefore, this.slotAfter, this.output, this.allOrNothing, this.exactAmount, this.filter
      );
   }

   @Nullable
   public SlotTransaction fromParent(ItemContainer parent, short start, @Nonnull ItemContainer container) {
      short newSlot = (short)(this.slot - start);
      return newSlot >= 0 && newSlot < container.getCapacity()
         ? new SlotTransaction(
            this.succeeded, this.action, newSlot, this.slotBefore, this.slotAfter, this.output, this.allOrNothing, this.exactAmount, this.filter
         )
         : null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SlotTransaction{succeeded="
         + this.succeeded
         + ", action="
         + this.action
         + ", slot="
         + this.slot
         + ", slotBefore="
         + this.slotBefore
         + ", slotAfter="
         + this.slotAfter
         + ", allOrNothing="
         + this.allOrNothing
         + ", exactAmount="
         + this.exactAmount
         + ", filter="
         + this.filter
         + "}";
   }
}
