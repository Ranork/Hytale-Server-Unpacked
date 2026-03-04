package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import com.hypixel.hytale.server.core.inventory.transaction.TagSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.TagTransaction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class InternalContainerUtilTag {
   protected static TagSlotTransaction internal_removeTagFromSlot(
      @Nonnull ItemContainer itemContainer, short slot, int tagIndex, int quantity, boolean allOrNothing, boolean filter
   ) {
      ItemContainer.validateSlotIndex(slot, itemContainer.getCapacity());
      ItemContainer.validateQuantity(quantity);
      return itemContainer.writeAction(
         () -> {
            if (filter && itemContainer.cantRemoveFromSlot(slot)) {
               ItemStack itemStack = itemContainer.internal_getSlot(slot);
               return new TagSlotTransaction(false, ActionType.REMOVE, slot, itemStack, itemStack, null, allOrNothing, false, filter, tagIndex, quantity);
            } else {
               ItemStack slotItemStack = itemContainer.internal_getSlot(slot);
               if (slotItemStack == null) {
                  return new TagSlotTransaction(false, ActionType.REMOVE, slot, null, null, null, allOrNothing, false, filter, tagIndex, quantity);
               } else {
                  Item slotItem = slotItemStack.getItem();
                  int quantityInItems = slotItemStack.getQuantity();
                  if (slotItem.getData() != null && slotItem.getData().getExpandedTagIndexes().contains(tagIndex)) {
                     int quantityInItemsAdjustment = Math.min(quantityInItems, quantity);
                     int newItemStackQuantity = quantityInItems - quantityInItemsAdjustment;
                     int quantityRemaining = quantity - quantityInItemsAdjustment;
                     if (allOrNothing && quantityRemaining > 0) {
                        return new TagSlotTransaction(
                           false, ActionType.REMOVE, slot, slotItemStack, slotItemStack, null, allOrNothing, false, filter, tagIndex, quantity
                        );
                     } else {
                        ItemStack slotNewItemStack = slotItemStack.withQuantity(newItemStackQuantity);
                        itemContainer.internal_setSlot(slot, slotNewItemStack);
                        ItemStack newStack = slotItemStack.withQuantity(quantityInItemsAdjustment);
                        return new TagSlotTransaction(
                           true, ActionType.REMOVE, slot, slotItemStack, slotNewItemStack, newStack, allOrNothing, false, filter, tagIndex, quantityRemaining
                        );
                     }
                  } else {
                     return new TagSlotTransaction(
                        false, ActionType.REMOVE, slot, slotItemStack, slotItemStack, null, allOrNothing, false, filter, tagIndex, quantity
                     );
                  }
               }
            }
         }
      );
   }

   protected static TagTransaction internal_removeTag(
      @Nonnull ItemContainer itemContainer, int tagIndex, int quantity, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return itemContainer.writeAction(() -> {
         if (allOrNothing || exactAmount) {
            int testQuantityRemaining = testRemoveTagFromItems(itemContainer, tagIndex, quantity, filter);
            if (testQuantityRemaining > 0) {
               return new TagTransaction(false, ActionType.REMOVE, tagIndex, quantity, allOrNothing, exactAmount, filter, Collections.emptyList());
            }

            if (exactAmount && testQuantityRemaining < 0) {
               return new TagTransaction(false, ActionType.REMOVE, tagIndex, quantity, allOrNothing, exactAmount, filter, Collections.emptyList());
            }
         }

         List<TagSlotTransaction> list = new ObjectArrayList();
         int quantityRemaining = quantity;

         for (short i = 0; i < itemContainer.getCapacity() && quantityRemaining > 0; i++) {
            TagSlotTransaction transaction = internal_removeTagFromSlot(itemContainer, i, tagIndex, quantityRemaining, allOrNothing, filter);
            list.add(transaction);
            quantityRemaining = transaction.getRemainder();
         }

         return new TagTransaction(true, ActionType.REMOVE, tagIndex, quantityRemaining, allOrNothing, exactAmount, filter, list);
      });
   }

   protected static int testRemoveTagFromItems(@Nonnull ItemContainer container, int tagIndex, int testQuantityRemaining, boolean filter) {
      for (short i = 0; i < container.getCapacity() && testQuantityRemaining > 0; i++) {
         if (!filter || !container.cantRemoveFromSlot(i)) {
            ItemStack slotItemStack = container.internal_getSlot(i);
            if (!ItemStack.isEmpty(slotItemStack)) {
               AssetExtraInfo.Data slotItemData = slotItemStack.getItem().getData();
               if (slotItemData != null && slotItemData.getExpandedTagIndexes().contains(tagIndex)) {
                  int quantityInItems = slotItemStack.getQuantity();
                  testQuantityRemaining -= Math.min(quantityInItems, testQuantityRemaining);
               }
            }
         }
      }

      return testQuantityRemaining;
   }

   protected static TestRemoveItemSlotResult testRemoveTagSlotFromItems(
      @Nonnull ItemContainer container, int tagIndex, int testQuantityRemaining, boolean filter
   ) {
      TestRemoveItemSlotResult result = new TestRemoveItemSlotResult(testQuantityRemaining);

      for (short i = 0; i < container.getCapacity() && result.quantityRemaining > 0; i++) {
         if (!filter || !container.cantRemoveFromSlot(i)) {
            ItemStack slotItemStack = container.internal_getSlot(i);
            if (!ItemStack.isEmpty(slotItemStack)) {
               AssetExtraInfo.Data slotItemData = slotItemStack.getItem().getData();
               if (slotItemData != null && slotItemData.getExpandedTagIndexes().contains(tagIndex)) {
                  int quantityInItems = slotItemStack.getQuantity();
                  result.quantityRemaining = result.quantityRemaining - Math.min(quantityInItems, result.quantityRemaining);
               }
            }
         }
      }

      return result;
   }

   protected static int testRemoveTagFromSlot(@Nonnull ItemContainer container, short slot, int tagIndex, int testQuantityRemaining, boolean filter) {
      if (filter && container.cantRemoveFromSlot(slot)) {
         return testQuantityRemaining;
      } else {
         ItemStack slotItemStack = container.internal_getSlot(slot);
         if (ItemStack.isEmpty(slotItemStack)) {
            return testQuantityRemaining;
         } else {
            Item slotItem = slotItemStack.getItem();
            if (!slotItem.getData().getExpandedTagIndexes().contains(tagIndex)) {
               return testQuantityRemaining;
            } else {
               int quantityInItems = slotItemStack.getQuantity();
               return testQuantityRemaining - Math.min(quantityInItems, testQuantityRemaining);
            }
         }
      }
   }
}
