package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InternalContainerUtilItemStack {
   protected static int testAddToExistingSlot(
      @Nonnull ItemContainer abstractItemContainer, short slot, ItemStack itemStack, int itemMaxStack, int testQuantityRemaining, boolean filter
   ) {
      ItemStack slotItemStack = abstractItemContainer.internal_getSlot(slot);
      if (ItemStack.isEmpty(slotItemStack)) {
         return testQuantityRemaining;
      } else if (!slotItemStack.isStackableWith(itemStack)) {
         return testQuantityRemaining;
      } else if (filter && abstractItemContainer.cantAddToSlot(slot, itemStack, slotItemStack)) {
         return testQuantityRemaining;
      } else {
         int quantity = slotItemStack.getQuantity();
         int quantityAdjustment = Math.min(itemMaxStack - quantity, testQuantityRemaining);
         return testQuantityRemaining - quantityAdjustment;
      }
   }

   @Nonnull
   protected static ItemStackSlotTransaction internal_addToExistingSlot(
      @Nonnull ItemContainer container, short slot, @Nonnull ItemStack itemStack, int itemMaxStack, boolean filter
   ) {
      ItemStack slotItemStack = container.internal_getSlot(slot);
      if (ItemStack.isEmpty(slotItemStack)) {
         return new ItemStackSlotTransaction(false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, false, false, filter, true, itemStack, itemStack);
      } else if (!slotItemStack.isStackableWith(itemStack)) {
         return new ItemStackSlotTransaction(false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, false, false, filter, true, itemStack, itemStack);
      } else if (filter && container.cantAddToSlot(slot, itemStack, slotItemStack)) {
         return new ItemStackSlotTransaction(false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, false, false, filter, true, itemStack, itemStack);
      } else {
         int quantityRemaining = itemStack.getQuantity();
         int quantity = slotItemStack.getQuantity();
         int quantityAdjustment = Math.min(itemMaxStack - quantity, quantityRemaining);
         int newQuantity = quantity + quantityAdjustment;
         quantityRemaining -= quantityAdjustment;
         if (quantityAdjustment <= 0) {
            return new ItemStackSlotTransaction(
               false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, false, false, filter, true, itemStack, itemStack
            );
         } else {
            ItemStack slotNew = slotItemStack.withQuantity(newQuantity);
            if (newQuantity > 0) {
               container.internal_setSlot(slot, slotNew);
            } else {
               container.internal_removeSlot(slot);
            }

            ItemStack remainder = quantityRemaining != itemStack.getQuantity() ? itemStack.withQuantity(quantityRemaining) : itemStack;
            return new ItemStackSlotTransaction(true, ActionType.ADD, slot, slotItemStack, slotNew, null, false, false, filter, true, itemStack, remainder);
         }
      }
   }

   @Nonnull
   protected static ItemStackSlotTransaction internal_addToEmptySlot(
      @Nonnull ItemContainer container, short slot, @Nonnull ItemStack itemStack, int itemMaxStack, boolean filter
   ) {
      ItemStack slotItemStack = container.internal_getSlot(slot);
      if (slotItemStack != null && !slotItemStack.isEmpty()) {
         return new ItemStackSlotTransaction(false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, false, false, filter, false, itemStack, itemStack);
      } else if (filter && container.cantAddToSlot(slot, itemStack, slotItemStack)) {
         return new ItemStackSlotTransaction(false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, false, false, filter, false, itemStack, itemStack);
      } else {
         int quantityRemaining = itemStack.getQuantity();
         int quantityAdjustment = Math.min(itemMaxStack, quantityRemaining);
         quantityRemaining -= quantityAdjustment;
         ItemStack slotNew = itemStack.withQuantity(quantityAdjustment);
         container.internal_setSlot(slot, slotNew);
         ItemStack remainder = itemStack.getQuantity() != quantityRemaining ? itemStack.withQuantity(quantityRemaining) : itemStack;
         return new ItemStackSlotTransaction(true, ActionType.ADD, slot, slotItemStack, slotNew, null, false, false, filter, false, itemStack, remainder);
      }
   }

   protected static int testAddToEmptySlots(@Nonnull ItemContainer container, ItemStack itemStack, int itemMaxStack, int testQuantityRemaining, boolean filter) {
      for (short i = 0; i < container.getCapacity() && testQuantityRemaining > 0; i++) {
         ItemStack slotItemStack = container.internal_getSlot(i);
         if ((slotItemStack == null || slotItemStack.isEmpty()) && (!filter || !container.cantAddToSlot(i, itemStack, slotItemStack))) {
            int quantityAdjustment = Math.min(itemMaxStack, testQuantityRemaining);
            testQuantityRemaining -= quantityAdjustment;
         }
      }

      return testQuantityRemaining;
   }

   protected static ItemStackSlotTransaction internal_addItemStackToSlot(
      @Nonnull ItemContainer itemContainer, short slot, @Nonnull ItemStack itemStack, boolean allOrNothing, boolean filter
   ) {
      ItemContainer.validateSlotIndex(slot, itemContainer.getCapacity());
      return itemContainer.writeAction(
         () -> {
            int quantityRemaining = itemStack.getQuantity();
            ItemStack slotItemStack = itemContainer.internal_getSlot(slot);
            if (filter && itemContainer.cantAddToSlot(slot, itemStack, slotItemStack)) {
               return new ItemStackSlotTransaction(
                  false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, allOrNothing, false, filter, false, itemStack, itemStack
               );
            } else if (slotItemStack == null) {
               itemContainer.internal_setSlot(slot, itemStack);
               return new ItemStackSlotTransaction(true, ActionType.ADD, slot, null, itemStack, null, allOrNothing, false, filter, false, itemStack, null);
            } else {
               int quantity = slotItemStack.getQuantity();
               if (!itemStack.isStackableWith(slotItemStack)) {
                  return new ItemStackSlotTransaction(
                     false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, allOrNothing, false, filter, false, itemStack, itemStack
                  );
               } else {
                  int quantityAdjustment = Math.min(slotItemStack.getItem().getMaxStack() - quantity, quantityRemaining);
                  int newQuantity = quantity + quantityAdjustment;
                  quantityRemaining -= quantityAdjustment;
                  if (allOrNothing && quantityRemaining > 0) {
                     return new ItemStackSlotTransaction(
                        false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, allOrNothing, false, filter, false, itemStack, itemStack
                     );
                  } else if (quantityAdjustment <= 0) {
                     return new ItemStackSlotTransaction(
                        false, ActionType.ADD, slot, slotItemStack, slotItemStack, null, allOrNothing, false, filter, false, itemStack, itemStack
                     );
                  } else {
                     ItemStack newItemStack = slotItemStack.withQuantity(newQuantity);
                     itemContainer.internal_setSlot(slot, newItemStack);
                     ItemStack remainder = itemStack.withQuantity(quantityRemaining);
                     return new ItemStackSlotTransaction(
                        true, ActionType.ADD, slot, slotItemStack, newItemStack, null, allOrNothing, false, filter, false, itemStack, remainder
                     );
                  }
               }
            }
         }
      );
   }

   @Nonnull
   protected static ItemStackSlotTransaction internal_setItemStackForSlot(@Nonnull ItemContainer itemContainer, short slot, ItemStack itemStack, boolean filter) {
      ItemContainer.validateSlotIndex(slot, itemContainer.getCapacity());
      return itemContainer.writeAction(
         () -> {
            ItemStack slotItemStack = itemContainer.internal_getSlot(slot);
            if (filter && itemContainer.cantAddToSlot(slot, itemStack, slotItemStack)) {
               return new ItemStackSlotTransaction(
                  false, ActionType.SET, slot, slotItemStack, slotItemStack, null, false, false, filter, false, itemStack, itemStack
               );
            } else {
               ItemStack oldItemStack = itemContainer.internal_setSlot(slot, itemStack);
               return new ItemStackSlotTransaction(true, ActionType.SET, slot, oldItemStack, itemStack, null, false, false, filter, false, itemStack, null);
            }
         }
      );
   }

   @Nonnull
   protected static SlotTransaction internal_removeItemStackFromSlot(@Nonnull ItemContainer itemContainer, short slot, boolean filter) {
      ItemContainer.validateSlotIndex(slot, itemContainer.getCapacity());
      return itemContainer.writeAction(() -> {
         if (filter && itemContainer.cantRemoveFromSlot(slot)) {
            ItemStack itemStack = itemContainer.internal_getSlot(slot);
            return new ItemStackSlotTransaction(false, ActionType.REMOVE, slot, itemStack, itemStack, null, false, false, filter, false, null, itemStack);
         } else {
            ItemStack oldItemStack = itemContainer.internal_removeSlot(slot);
            return new SlotTransaction(true, ActionType.REMOVE, slot, oldItemStack, null, oldItemStack, false, false, false);
         }
      });
   }

   protected static ItemStackSlotTransaction internal_removeItemStackFromSlot(
      @Nonnull ItemContainer itemContainer, short slot, int quantityToRemove, boolean allOrNothing, boolean filter
   ) {
      ItemContainer.validateSlotIndex(slot, itemContainer.getCapacity());
      ItemContainer.validateQuantity(quantityToRemove);
      return itemContainer.writeAction(
         () -> {
            if (filter && itemContainer.cantRemoveFromSlot(slot)) {
               ItemStack itemStack = itemContainer.internal_getSlot(slot);
               return new ItemStackSlotTransaction(
                  false,
                  ActionType.REMOVE,
                  slot,
                  itemStack,
                  itemStack,
                  null,
                  allOrNothing,
                  false,
                  filter,
                  false,
                  null,
                  itemStack.withQuantity(quantityToRemove)
               );
            } else {
               ItemStack slotItemStack = itemContainer.internal_getSlot(slot);
               if (slotItemStack == null) {
                  return new ItemStackSlotTransaction(false, ActionType.REMOVE, slot, null, null, null, allOrNothing, false, filter, false, null, null);
               } else {
                  int quantity = slotItemStack.getQuantity();
                  int quantityAdjustment = Math.min(quantity, quantityToRemove);
                  int newQuantity = quantity - quantityAdjustment;
                  int quantityRemaining = quantityToRemove - quantityAdjustment;
                  if (allOrNothing && quantityRemaining > 0) {
                     return new ItemStackSlotTransaction(
                        false,
                        ActionType.REMOVE,
                        slot,
                        slotItemStack,
                        slotItemStack,
                        null,
                        allOrNothing,
                        false,
                        filter,
                        false,
                        null,
                        slotItemStack.withQuantity(quantityRemaining)
                     );
                  } else {
                     ItemStack itemStack = slotItemStack.withQuantity(newQuantity);
                     itemContainer.internal_setSlot(slot, itemStack);
                     ItemStack newStack = slotItemStack.withQuantity(quantityAdjustment);
                     ItemStack remainder = slotItemStack.withQuantity(quantityRemaining);
                     return new ItemStackSlotTransaction(
                        true, ActionType.REMOVE, slot, slotItemStack, itemStack, newStack, allOrNothing, false, filter, false, null, remainder
                     );
                  }
               }
            }
         }
      );
   }

   protected static ItemStackSlotTransaction internal_removeItemStackFromSlot(
      @Nonnull ItemContainer itemContainer, short slot, @Nullable ItemStack itemStackToRemove, int quantityToRemove, boolean allOrNothing, boolean filter
   ) {
      return internal_removeItemStackFromSlot(
         itemContainer, slot, itemStackToRemove, quantityToRemove, allOrNothing, filter, (a, b) -> ItemStack.isStackableWith(a, b)
      );
   }

   protected static ItemStackSlotTransaction internal_removeItemStackFromSlot(
      @Nonnull ItemContainer itemContainer,
      short slot,
      @Nullable ItemStack itemStackToRemove,
      int quantityToRemove,
      boolean allOrNothing,
      boolean filter,
      BiPredicate<ItemStack, ItemStack> predicate
   ) {
      ItemContainer.validateSlotIndex(slot, itemContainer.getCapacity());
      ItemContainer.validateQuantity(quantityToRemove);
      return itemContainer.writeAction(
         () -> {
            if (filter && itemContainer.cantRemoveFromSlot(slot)) {
               ItemStack itemStack = itemContainer.internal_getSlot(slot);
               return new ItemStackSlotTransaction(
                  false, ActionType.REMOVE, slot, itemStack, itemStack, null, allOrNothing, false, filter, false, itemStackToRemove, itemStackToRemove
               );
            } else {
               ItemStack slotItemStack = itemContainer.internal_getSlot(slot);
               if ((slotItemStack != null || itemStackToRemove == null)
                  && (slotItemStack == null || itemStackToRemove != null)
                  && (slotItemStack == null || predicate.test(slotItemStack, itemStackToRemove))) {
                  if (slotItemStack == null) {
                     return new ItemStackSlotTransaction(
                        true, ActionType.REMOVE, slot, null, null, null, allOrNothing, false, filter, false, itemStackToRemove, itemStackToRemove
                     );
                  } else {
                     int quantity = slotItemStack.getQuantity();
                     int quantityAdjustment = Math.min(quantity, quantityToRemove);
                     int newQuantity = quantity - quantityAdjustment;
                     int quantityRemaining = quantityToRemove - quantityAdjustment;
                     if (allOrNothing && quantityRemaining > 0) {
                        return new ItemStackSlotTransaction(
                           false,
                           ActionType.REMOVE,
                           slot,
                           slotItemStack,
                           slotItemStack,
                           null,
                           allOrNothing,
                           false,
                           filter,
                           false,
                           itemStackToRemove,
                           itemStackToRemove
                        );
                     } else {
                        ItemStack itemStack = slotItemStack.withQuantity(newQuantity);
                        itemContainer.internal_setSlot(slot, itemStack);
                        ItemStack newStack = slotItemStack.withQuantity(quantityAdjustment);
                        ItemStack remainder = itemStackToRemove.withQuantity(quantityRemaining);
                        return new ItemStackSlotTransaction(
                           true, ActionType.REMOVE, slot, slotItemStack, itemStack, newStack, allOrNothing, false, filter, false, itemStackToRemove, remainder
                        );
                     }
                  }
               } else {
                  return new ItemStackSlotTransaction(
                     false,
                     ActionType.REMOVE,
                     slot,
                     slotItemStack,
                     slotItemStack,
                     null,
                     allOrNothing,
                     false,
                     filter,
                     false,
                     itemStackToRemove,
                     itemStackToRemove
                  );
               }
            }
         }
      );
   }

   protected static int testRemoveItemStackFromSlot(
      @Nonnull ItemContainer container, short slot, ItemStack itemStack, int testQuantityRemaining, boolean filter
   ) {
      return testRemoveItemStackFromSlot(container, slot, itemStack, testQuantityRemaining, filter, (a, b) -> ItemStack.isStackableWith(a, b));
   }

   protected static int testRemoveItemStackFromSlot(
      @Nonnull ItemContainer container, short slot, ItemStack itemStack, int testQuantityRemaining, boolean filter, BiPredicate<ItemStack, ItemStack> predicate
   ) {
      if (filter && container.cantRemoveFromSlot(slot)) {
         return testQuantityRemaining;
      } else {
         ItemStack slotItemStack = container.internal_getSlot(slot);
         if (ItemStack.isEmpty(slotItemStack)) {
            return testQuantityRemaining;
         } else if (!predicate.test(slotItemStack, itemStack)) {
            return testQuantityRemaining;
         } else {
            int quantity = slotItemStack.getQuantity();
            int quantityAdjustment = Math.min(quantity, testQuantityRemaining);
            return testQuantityRemaining - quantityAdjustment;
         }
      }
   }

   protected static ItemStackTransaction internal_addItemStack(
      @Nonnull ItemContainer itemContainer, @Nonnull ItemStack itemStack, boolean allOrNothing, boolean fullStacks, boolean filter
   ) {
      Item item = itemStack.getItem();
      if (item == null) {
         throw new IllegalArgumentException(itemStack.getItemId() + " is an invalid item!");
      } else {
         int itemMaxStack = item.getMaxStack();
         return itemContainer.writeAction(() -> {
            if (allOrNothing) {
               int testQuantityRemaining = itemStack.getQuantity();
               if (!fullStacks) {
                  testQuantityRemaining = testAddToExistingItemStacks(itemContainer, itemStack, itemMaxStack, testQuantityRemaining, filter);
               }

               testQuantityRemaining = testAddToEmptySlots(itemContainer, itemStack, itemMaxStack, testQuantityRemaining, filter);
               if (testQuantityRemaining > 0) {
                  return new ItemStackTransaction(false, ActionType.ADD, itemStack, itemStack, allOrNothing, filter, Collections.emptyList());
               }
            }

            List<ItemStackSlotTransaction> list = new ObjectArrayList();
            ItemStack remaining = itemStack;
            if (!fullStacks) {
               for (short i = 0; i < itemContainer.getCapacity() && !ItemStack.isEmpty(remaining); i++) {
                  ItemStackSlotTransaction transaction = internal_addToExistingSlot(itemContainer, i, remaining, itemMaxStack, filter);
                  list.add(transaction);
                  remaining = transaction.getRemainder();
               }
            }

            for (short i = 0; i < itemContainer.getCapacity() && !ItemStack.isEmpty(remaining); i++) {
               ItemStackSlotTransaction transaction = internal_addToEmptySlot(itemContainer, i, remaining, itemMaxStack, filter);
               list.add(transaction);
               remaining = transaction.getRemainder();
            }

            return new ItemStackTransaction(true, ActionType.ADD, itemStack, remaining, allOrNothing, filter, list);
         });
      }
   }

   protected static ListTransaction<ItemStackTransaction> internal_addItemStacks(
      @Nonnull ItemContainer itemContainer, @Nullable List<ItemStack> itemStacks, boolean allOrNothing, boolean fullStacks, boolean filter
   ) {
      return itemStacks != null && !itemStacks.isEmpty()
         ? itemContainer.writeAction(
            () -> {
               if (allOrNothing) {
                  for (ItemStack itemStack : itemStacks) {
                     int itemMaxStack = itemStack.getItem().getMaxStack();
                     int testQuantityRemaining = itemStack.getQuantity();
                     if (!fullStacks) {
                        testQuantityRemaining = testAddToExistingItemStacks(itemContainer, itemStack, itemMaxStack, testQuantityRemaining, filter);
                     }

                     testQuantityRemaining = testAddToEmptySlots(itemContainer, itemStack, itemMaxStack, testQuantityRemaining, filter);
                     if (testQuantityRemaining > 0) {
                        return new ListTransaction<>(
                           false,
                           itemStacks.stream()
                              .map(i -> new ItemStackTransaction(false, ActionType.ADD, itemStack, itemStack, allOrNothing, filter, Collections.emptyList()))
                              .collect(Collectors.toList())
                        );
                     }
                  }
               }

               List<ItemStackTransaction> remainingItemStacks = new ObjectArrayList();

               for (ItemStack itemStack : itemStacks) {
                  remainingItemStacks.add(internal_addItemStack(itemContainer, itemStack, allOrNothing, fullStacks, filter));
               }

               return new ListTransaction<>(true, remainingItemStacks);
            }
         )
         : ListTransaction.getEmptyTransaction(true);
   }

   protected static ListTransaction<ItemStackSlotTransaction> internal_addItemStacksOrdered(
      @Nonnull ItemContainer itemContainer, short offset, @Nullable List<ItemStack> itemStacks, boolean allOrNothing, boolean filter
   ) {
      if (itemStacks != null && !itemStacks.isEmpty()) {
         ItemContainer.validateSlotIndex(offset, itemContainer.getCapacity());
         ItemContainer.validateSlotIndex((short)(offset + itemStacks.size()), itemContainer.getCapacity());
         return itemContainer.writeAction(
            () -> {
               if (allOrNothing) {
                  for (short i = 0; i < itemStacks.size(); i++) {
                     short slot = (short)(offset + i);
                     ItemStack itemStack = itemStacks.get(i);
                     int itemMaxStack = itemStack.getItem().getMaxStack();
                     int testQuantityRemaining = itemStack.getQuantity();
                     testQuantityRemaining = testAddToExistingSlot(itemContainer, slot, itemStack, itemMaxStack, testQuantityRemaining, filter);
                     if (testQuantityRemaining > 0) {
                        List<ItemStackSlotTransaction> list = new ObjectArrayList();

                        for (short i1 = 0; i1 < itemStacks.size(); i1++) {
                           short islot = (short)(offset + i1);
                           list.add(
                              new ItemStackSlotTransaction(
                                 false, ActionType.ADD, islot, null, null, null, allOrNothing, false, filter, false, itemStack, itemStack
                              )
                           );
                        }

                        return new ListTransaction<>(false, list);
                     }
                  }
               }

               List<ItemStackSlotTransaction> remainingItemStacks = new ObjectArrayList();

               for (short ix = 0; ix < itemStacks.size(); ix++) {
                  short slot = (short)(offset + ix);
                  remainingItemStacks.add(internal_addItemStackToSlot(itemContainer, slot, itemStacks.get(ix), allOrNothing, filter));
               }

               return new ListTransaction<>(true, remainingItemStacks);
            }
         );
      } else {
         return ListTransaction.getEmptyTransaction(true);
      }
   }

   protected static int testAddToExistingItemStacks(
      @Nonnull ItemContainer container, ItemStack itemStack, int itemMaxStack, int testQuantityRemaining, boolean filter
   ) {
      for (short i = 0; i < container.getCapacity() && testQuantityRemaining > 0; i++) {
         testQuantityRemaining = testAddToExistingSlot(container, i, itemStack, itemMaxStack, testQuantityRemaining, filter);
      }

      return testQuantityRemaining;
   }

   protected static ItemStackTransaction internal_removeItemStack(
      @Nonnull ItemContainer itemContainer, @Nonnull ItemStack itemStack, boolean allOrNothing, boolean filter
   ) {
      Item item = itemStack.getItem();
      if (item == null) {
         throw new IllegalArgumentException(itemStack.getItemId() + " is an invalid item!");
      } else {
         return itemContainer.writeAction(() -> {
            if (allOrNothing) {
               int testQuantityRemaining = testRemoveItemStackFromItems(itemContainer, itemStack, itemStack.getQuantity(), filter);
               if (testQuantityRemaining > 0) {
                  return new ItemStackTransaction(false, ActionType.REMOVE, itemStack, itemStack, allOrNothing, filter, Collections.emptyList());
               }
            }

            List<ItemStackSlotTransaction> transactions = new ObjectArrayList();
            int quantityRemaining = itemStack.getQuantity();

            for (short i = 0; i < itemContainer.getCapacity() && quantityRemaining > 0; i++) {
               ItemStack slotItemStack = itemContainer.internal_getSlot(i);
               if (!ItemStack.isEmpty(slotItemStack) && slotItemStack.isStackableWith(itemStack)) {
                  ItemStackSlotTransaction transaction = internal_removeItemStackFromSlot(itemContainer, i, quantityRemaining, false, filter);
                  transactions.add(transaction);
                  quantityRemaining = transaction.getRemainder() != null ? transaction.getRemainder().getQuantity() : 0;
               }
            }

            ItemStack remainder = quantityRemaining > 0 ? itemStack.withQuantity(quantityRemaining) : null;
            return new ItemStackTransaction(true, ActionType.REMOVE, itemStack, remainder, allOrNothing, filter, transactions);
         });
      }
   }

   protected static ListTransaction<ItemStackTransaction> internal_removeItemStacks(
      @Nonnull ItemContainer itemContainer, @Nullable List<ItemStack> itemStacks, boolean allOrNothing, boolean filter
   ) {
      if (itemStacks != null && !itemStacks.isEmpty()) {
         for (ItemStack itemStack : itemStacks) {
            Item item = itemStack.getItem();
            if (item == null) {
               throw new IllegalArgumentException(itemStack.getItemId() + " is an invalid item!");
            }
         }

         return itemContainer.writeAction(
            () -> {
               if (allOrNothing) {
                  for (ItemStack itemStackx : itemStacks) {
                     int testQuantityRemaining = testRemoveItemStackFromItems(itemContainer, itemStackx, itemStackx.getQuantity(), filter);
                     if (testQuantityRemaining > 0) {
                        return new ListTransaction<>(
                           false,
                           itemStacks.stream()
                              .map(i -> new ItemStackTransaction(false, ActionType.ADD, itemStackx, itemStackx, allOrNothing, filter, Collections.emptyList()))
                              .collect(Collectors.toList())
                        );
                     }
                  }
               }

               List<ItemStackTransaction> transactions = new ObjectArrayList();

               for (short i = 0; i < itemStacks.size(); i++) {
                  transactions.add(internal_removeItemStack(itemContainer, itemStacks.get(i), allOrNothing, filter));
               }

               return new ListTransaction<>(true, transactions);
            }
         );
      } else {
         return ListTransaction.getEmptyTransaction(true);
      }
   }

   protected static int testRemoveItemStackFromItems(@Nonnull ItemContainer container, ItemStack itemStack, int testQuantityRemaining, boolean filter) {
      for (short i = 0; i < container.getCapacity() && testQuantityRemaining > 0; i++) {
         if (!filter || !container.cantRemoveFromSlot(i)) {
            ItemStack slotItemStack = container.internal_getSlot(i);
            if (!ItemStack.isEmpty(slotItemStack) && slotItemStack.isStackableWith(itemStack)) {
               int quantity = slotItemStack.getQuantity();
               int quantityAdjustment = Math.min(quantity, testQuantityRemaining);
               testQuantityRemaining -= quantityAdjustment;
            }
         }
      }

      return testQuantityRemaining;
   }

   protected static TestRemoveItemSlotResult testRemoveItemStackSlotFromItems(
      @Nonnull ItemContainer container, ItemStack itemStack, int testQuantityRemaining, boolean filter
   ) {
      return testRemoveItemStackSlotFromItems(container, itemStack, testQuantityRemaining, filter, (a, b) -> ItemStack.isStackableWith(a, b));
   }

   protected static TestRemoveItemSlotResult testRemoveItemStackSlotFromItems(
      @Nonnull ItemContainer container, ItemStack itemStack, int testQuantityRemaining, boolean filter, BiPredicate<ItemStack, ItemStack> predicate
   ) {
      TestRemoveItemSlotResult result = new TestRemoveItemSlotResult(testQuantityRemaining);

      for (short i = 0; i < container.getCapacity() && result.quantityRemaining > 0; i++) {
         if (!filter || !container.cantRemoveFromSlot(i)) {
            ItemStack slotItemStack = container.internal_getSlot(i);
            if (!ItemStack.isEmpty(slotItemStack) && predicate.test(slotItemStack, itemStack)) {
               int quantity = slotItemStack.getQuantity();
               int quantityAdjustment = Math.min(quantity, result.quantityRemaining);
               result.quantityRemaining -= quantityAdjustment;
               result.picked.put(i, quantityAdjustment);
            }
         }
      }

      return result;
   }
}
