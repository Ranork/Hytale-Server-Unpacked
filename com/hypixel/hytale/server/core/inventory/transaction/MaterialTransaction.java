package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MaterialTransaction extends ListTransaction<MaterialSlotTransaction> {
   @Nonnull
   private final ActionType action;
   @Nonnull
   private final MaterialQuantity material;
   private final int remainder;
   private final boolean allOrNothing;
   private final boolean exactAmount;
   private final boolean filter;

   public MaterialTransaction(
      boolean succeeded,
      @Nonnull ActionType action,
      @Nonnull MaterialQuantity material,
      int remainder,
      boolean allOrNothing,
      boolean exactAmount,
      boolean filter,
      @Nonnull List<MaterialSlotTransaction> slotTransactions
   ) {
      super(succeeded, slotTransactions);
      this.action = action;
      this.material = material;
      this.remainder = remainder;
      this.allOrNothing = allOrNothing;
      this.exactAmount = exactAmount;
      this.filter = filter;
   }

   @Nonnull
   public ActionType getAction() {
      return this.action;
   }

   @Nonnull
   public MaterialQuantity getMaterial() {
      return this.material;
   }

   public int getRemainder() {
      return this.remainder;
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
   public MaterialTransaction toParent(ItemContainer parent, short start, ItemContainer container) {
      List<MaterialSlotTransaction> slotTransactions = this.getList()
         .stream()
         .map(transaction -> transaction.toParent(parent, start, container))
         .collect(Collectors.toList());
      return new MaterialTransaction(
         this.succeeded(), this.action, this.material, this.remainder, this.allOrNothing, this.exactAmount, this.filter, slotTransactions
      );
   }

   @Nullable
   public MaterialTransaction fromParent(ItemContainer parent, short start, @Nonnull ItemContainer container) {
      List<MaterialSlotTransaction> slotTransactions = this.getList()
         .stream()
         .map(transactionx -> transactionx.fromParent(parent, start, container))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
      if (slotTransactions.isEmpty()) {
         return null;
      } else {
         boolean succeeded = false;

         for (MaterialSlotTransaction transaction : slotTransactions) {
            if (transaction.succeeded()) {
               succeeded = true;
               break;
            }
         }

         return new MaterialTransaction(
            succeeded, this.action, this.material, this.remainder, this.allOrNothing, this.exactAmount, this.filter, slotTransactions
         );
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "MaterialTransaction{action="
         + this.action
         + ", material="
         + this.material
         + ", remainder="
         + this.remainder
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
