package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemStackTransaction implements Transaction {
   public static final ItemStackTransaction FAILED_ADD = new ItemStackTransaction(false, ActionType.ADD, null, null, false, false, Collections.emptyList());
   private final boolean succeeded;
   @Nullable
   private final ActionType action;
   @Nullable
   private final ItemStack query;
   @Nullable
   private final ItemStack remainder;
   private final boolean allOrNothing;
   private final boolean filter;
   @Nonnull
   private final List<ItemStackSlotTransaction> slotTransactions;

   public ItemStackTransaction(
      boolean succeeded,
      @Nullable ActionType action,
      @Nullable ItemStack query,
      @Nullable ItemStack remainder,
      boolean allOrNothing,
      boolean filter,
      @Nonnull List<ItemStackSlotTransaction> slotTransactions
   ) {
      this.succeeded = succeeded;
      this.action = action;
      this.query = query;
      this.remainder = remainder;
      this.allOrNothing = allOrNothing;
      this.filter = filter;
      this.slotTransactions = slotTransactions;
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
         for (ItemStackSlotTransaction t : this.slotTransactions) {
            if (t.succeeded() && t.wasSlotModified(slot)) {
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   public ActionType getAction() {
      return this.action;
   }

   @Nullable
   public ItemStack getQuery() {
      return this.query;
   }

   @Nullable
   public ItemStack getRemainder() {
      return this.remainder;
   }

   public boolean isAllOrNothing() {
      return this.allOrNothing;
   }

   public boolean isFilter() {
      return this.filter;
   }

   @Nonnull
   public List<ItemStackSlotTransaction> getSlotTransactions() {
      return this.slotTransactions;
   }

   @Nonnull
   public ItemStackTransaction toParent(ItemContainer parent, short start, ItemContainer container) {
      List<ItemStackSlotTransaction> slotTransactions = this.slotTransactions
         .stream()
         .map(transaction -> transaction.toParent(parent, start, container))
         .collect(Collectors.toList());
      return new ItemStackTransaction(this.succeeded, this.action, this.query, this.remainder, this.allOrNothing, this.filter, slotTransactions);
   }

   @Nullable
   public ItemStackTransaction fromParent(ItemContainer parent, short start, @Nonnull ItemContainer container) {
      List<ItemStackSlotTransaction> slotTransactions = this.slotTransactions
         .stream()
         .map(transactionx -> transactionx.fromParent(parent, start, container))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
      if (slotTransactions.isEmpty()) {
         return null;
      } else {
         boolean succeeded = false;

         for (ItemStackSlotTransaction transaction : slotTransactions) {
            if (transaction.succeeded()) {
               succeeded = true;
               break;
            }
         }

         return new ItemStackTransaction(succeeded, this.action, this.query, this.remainder, this.allOrNothing, this.filter, slotTransactions);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemStackTransaction{succeeded="
         + this.succeeded
         + ", action="
         + this.action
         + ", query="
         + this.query
         + ", remainder="
         + this.remainder
         + ", allOrNothing="
         + this.allOrNothing
         + ", filter="
         + this.filter
         + ", slotTransactions="
         + this.slotTransactions
         + "}";
   }
}
