package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MoveTransaction<T extends Transaction> implements Transaction {
   private final boolean succeeded;
   @Nonnull
   private final SlotTransaction removeTransaction;
   @Nonnull
   private final MoveType moveType;
   @Nonnull
   private final ItemContainer otherContainer;
   private final T addTransaction;

   public MoveTransaction(
      boolean succeeded, @Nonnull SlotTransaction removeTransaction, @Nonnull MoveType moveType, @Nonnull ItemContainer otherContainer, T addTransaction
   ) {
      this.succeeded = succeeded;
      this.removeTransaction = removeTransaction;
      this.moveType = moveType;
      this.otherContainer = otherContainer;
      this.addTransaction = addTransaction;
   }

   @Override
   public boolean succeeded() {
      return this.succeeded;
   }

   @Nonnull
   public SlotTransaction getRemoveTransaction() {
      return this.removeTransaction;
   }

   @Nonnull
   public MoveType getMoveType() {
      return this.moveType;
   }

   @Nonnull
   public ItemContainer getOtherContainer() {
      return this.otherContainer;
   }

   public T getAddTransaction() {
      return this.addTransaction;
   }

   @Nonnull
   public MoveTransaction<T> toInverted(@Nonnull ItemContainer itemContainer) {
      return new MoveTransaction<>(this.succeeded(), this.removeTransaction, MoveType.MOVE_TO_SELF, itemContainer, this.addTransaction);
   }

   @Override
   public boolean wasSlotModified(short slot) {
      return !this.succeeded
         ? false
         : this.addTransaction.succeeded() && this.addTransaction.wasSlotModified(slot)
            || this.removeTransaction.succeeded() && this.removeTransaction.wasSlotModified(slot);
   }

   @Nonnull
   public MoveTransaction<T> toParent(ItemContainer parent, short start, ItemContainer container) {
      MoveType moveType = this.getMoveType();

      return switch (moveType) {
         case MOVE_TO_SELF -> new MoveTransaction(
            this.succeeded(), this.removeTransaction, moveType, this.getOtherContainer(), (T)this.addTransaction.toParent(parent, start, container)
         );
         case MOVE_FROM_SELF -> new MoveTransaction(
            this.succeeded(), this.removeTransaction.toParent(parent, start, container), moveType, this.getOtherContainer(), this.addTransaction
         );
      };
   }

   @Nullable
   public MoveTransaction<T> fromParent(ItemContainer parent, short start, @Nonnull ItemContainer container) {
      MoveType moveType = this.getMoveType();
      switch (moveType) {
         case MOVE_TO_SELF:
            T newAddTransaction = (T)this.addTransaction.fromParent(parent, start, container);
            if (newAddTransaction == null) {
               return null;
            }

            return new MoveTransaction<>(this.succeeded(), this.getRemoveTransaction(), this.getMoveType(), this.getOtherContainer(), newAddTransaction);
         case MOVE_FROM_SELF:
            SlotTransaction newRemoveTransaction = this.getRemoveTransaction().fromParent(parent, start, container);
            if (newRemoveTransaction == null) {
               return null;
            }

            return new MoveTransaction<>(this.succeeded(), newRemoveTransaction, this.getMoveType(), this.getOtherContainer(), this.addTransaction);
         default:
            throw new IllegalStateException("Unexpected value: " + moveType);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "MoveTransaction{succeeded="
         + this.succeeded
         + ", removeTransaction="
         + this.removeTransaction
         + ", moveType="
         + this.moveType
         + ", otherContainer="
         + this.otherContainer
         + ", addTransaction="
         + this.addTransaction
         + "}";
   }
}
