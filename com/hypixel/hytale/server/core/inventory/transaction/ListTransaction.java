package com.hypixel.hytale.server.core.inventory.transaction;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ListTransaction<T extends Transaction> implements Transaction {
   public static final ListTransaction<?> EMPTY_SUCCESSFUL_TRANSACTION = new ListTransaction(true);
   public static final ListTransaction<?> EMPTY_FAILED_TRANSACTION = new ListTransaction(false);
   private final boolean succeeded;
   @Nonnull
   private final List<T> list;

   public static <T extends Transaction> ListTransaction<T> getEmptyTransaction(boolean succeeded) {
      return (ListTransaction<T>)(succeeded ? EMPTY_SUCCESSFUL_TRANSACTION : EMPTY_FAILED_TRANSACTION);
   }

   private ListTransaction(boolean succeeded) {
      this.succeeded = succeeded;
      this.list = Collections.emptyList();
   }

   public ListTransaction(boolean succeeded, @Nonnull List<T> list) {
      this.succeeded = succeeded;
      this.list = Collections.unmodifiableList(list);
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
         for (T t : this.list) {
            if (t.succeeded() && t.wasSlotModified(slot)) {
               return true;
            }
         }

         return false;
      }
   }

   @Nonnull
   public List<T> getList() {
      return this.list;
   }

   public int size() {
      return this.list.size();
   }

   @Nonnull
   public ListTransaction<T> toParent(ItemContainer parent, short start, ItemContainer container) {
      List<T> list = this.list.stream().map(transaction -> transaction.toParent(parent, start, container)).collect(Collectors.toList());
      return new ListTransaction<>(this.succeeded, list);
   }

   @Nullable
   public ListTransaction<T> fromParent(ItemContainer parent, short start, ItemContainer container) {
      List<T> list = this.list
         .stream()
         .map(transactionx -> transactionx.fromParent(parent, start, container))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
      if (list.isEmpty()) {
         return null;
      } else {
         boolean succeeded = false;

         for (T transaction : list) {
            if (transaction.succeeded()) {
               succeeded = true;
               break;
            }
         }

         return new ListTransaction<>(succeeded, list);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ListTransaction{succeeded=" + this.succeeded + ", list=" + this.list + "}";
   }
}
