package com.hypixel.hytale.builtin.adventure.objectives.transaction;

import javax.annotation.Nonnull;

public class WorldTransactionRecord extends TransactionRecord {
   @Override
   public void revert() {
   }

   @Override
   public void complete() {
   }

   @Override
   public void unload() {
   }

   @Override
   public boolean shouldBeSerialized() {
      return false;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WorldTransactionRecord{} " + super.toString();
   }
}
