package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResourceTransaction extends ListTransaction<ResourceSlotTransaction> {
   @Nonnull
   private final ActionType action;
   @Nonnull
   private final ResourceQuantity resource;
   private final int remainder;
   private final int consumed;
   private final boolean allOrNothing;
   private final boolean exactAmount;
   private final boolean filter;

   public ResourceTransaction(
      boolean succeeded,
      @Nonnull ActionType action,
      @Nonnull ResourceQuantity resource,
      int remainder,
      int consumed,
      boolean allOrNothing,
      boolean exactAmount,
      boolean filter,
      @Nonnull List<ResourceSlotTransaction> slotTransactions
   ) {
      super(succeeded, slotTransactions);
      this.action = action;
      this.resource = resource;
      this.remainder = remainder;
      this.consumed = consumed;
      this.allOrNothing = allOrNothing;
      this.exactAmount = exactAmount;
      this.filter = filter;
   }

   @Nonnull
   public ActionType getAction() {
      return this.action;
   }

   @Nonnull
   public ResourceQuantity getResource() {
      return this.resource;
   }

   public int getRemainder() {
      return this.remainder;
   }

   public int getConsumed() {
      return this.consumed;
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
   public ResourceTransaction toParent(ItemContainer parent, short start, ItemContainer container) {
      List<ResourceSlotTransaction> slotTransactions = this.getList()
         .stream()
         .map(transaction -> transaction.toParent(parent, start, container))
         .collect(Collectors.toList());
      return new ResourceTransaction(
         this.succeeded(), this.action, this.resource, this.remainder, this.consumed, this.allOrNothing, this.exactAmount, this.filter, slotTransactions
      );
   }

   @Nullable
   public ResourceTransaction fromParent(ItemContainer parent, short start, @Nonnull ItemContainer container) {
      List<ResourceSlotTransaction> slotTransactions = this.getList()
         .stream()
         .map(transactionx -> transactionx.fromParent(parent, start, container))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
      if (slotTransactions.isEmpty()) {
         return null;
      } else {
         boolean succeeded = false;

         for (ResourceSlotTransaction transaction : slotTransactions) {
            if (transaction.succeeded()) {
               succeeded = true;
               break;
            }
         }

         return new ResourceTransaction(
            succeeded, this.action, this.resource, this.remainder, this.consumed, this.allOrNothing, this.exactAmount, this.filter, slotTransactions
         );
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ResourceTransaction{action="
         + this.action
         + ", resource="
         + this.resource
         + ", remainder="
         + this.remainder
         + ", consumed="
         + this.consumed
         + ", allOrNothing="
         + this.allOrNothing
         + ", exactAmount="
         + this.exactAmount
         + ", filter="
         + this.filter
         + "} "
         + super.toString();
   }
}
