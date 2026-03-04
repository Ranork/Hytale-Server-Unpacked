package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.ItemResourceType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ResourceSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ResourceTransaction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InternalContainerUtilResource {
   protected static ResourceSlotTransaction internal_removeResourceFromSlot(
      @Nonnull ItemContainer itemContainer, short slot, @Nonnull ResourceQuantity resource, boolean allOrNothing, boolean filter
   ) {
      ItemContainer.validateSlotIndex(slot, itemContainer.getCapacity());
      ItemContainer.validateQuantity(resource.getQuantity());
      return itemContainer.writeAction(
         () -> {
            if (filter && itemContainer.cantRemoveFromSlot(slot)) {
               ItemStack itemStack = itemContainer.internal_getSlot(slot);
               return new ResourceSlotTransaction(
                  false, ActionType.REMOVE, slot, itemStack, itemStack, null, allOrNothing, false, filter, resource, resource.getQuantity(), 0
               );
            } else {
               ItemStack slotItemStack = itemContainer.internal_getSlot(slot);
               if (slotItemStack == null) {
                  return new ResourceSlotTransaction(
                     false, ActionType.REMOVE, slot, null, null, null, allOrNothing, false, filter, resource, resource.getQuantity(), 0
                  );
               } else {
                  Item slotItem = slotItemStack.getItem();
                  int quantityInItems = slotItemStack.getQuantity();
                  ItemResourceType resourceType = resource.getResourceType(slotItem);
                  if (resourceType == null) {
                     return new ResourceSlotTransaction(
                        false, ActionType.REMOVE, slot, slotItemStack, slotItemStack, null, allOrNothing, false, filter, resource, resource.getQuantity(), 0
                     );
                  } else {
                     int resourceTypeQuantity = resourceType.quantity;
                     int quantityRemaining = resource.getQuantity();
                     int quantityInItemsRemaining = MathUtil.ceil((double)quantityRemaining / resourceTypeQuantity);
                     int quantityInItemsAdjustment = Math.min(quantityInItems, quantityInItemsRemaining);
                     int newItemStackQuantity = Math.max(quantityInItems - quantityInItemsAdjustment, 0);
                     int quantityAdjustment = quantityInItemsAdjustment * resourceTypeQuantity;
                     quantityRemaining -= quantityAdjustment;
                     if (allOrNothing && quantityRemaining > 0) {
                        return new ResourceSlotTransaction(
                           false, ActionType.REMOVE, slot, slotItemStack, slotItemStack, null, allOrNothing, false, filter, resource, resource.getQuantity(), 0
                        );
                     } else if (quantityAdjustment <= 0) {
                        return new ResourceSlotTransaction(
                           false, ActionType.REMOVE, slot, slotItemStack, slotItemStack, null, allOrNothing, false, filter, resource, resource.getQuantity(), 0
                        );
                     } else {
                        ItemStack slotNewItemStack = slotItemStack.withQuantity(newItemStackQuantity);
                        itemContainer.internal_setSlot(slot, slotNewItemStack);
                        ItemStack newStack = slotItemStack.withQuantity(quantityInItemsAdjustment);
                        return new ResourceSlotTransaction(
                           true,
                           ActionType.REMOVE,
                           slot,
                           slotItemStack,
                           slotNewItemStack,
                           newStack,
                           allOrNothing,
                           false,
                           filter,
                           resource,
                           quantityRemaining,
                           quantityAdjustment
                        );
                     }
                  }
               }
            }
         }
      );
   }

   protected static ResourceTransaction internal_removeResource(
      @Nonnull ItemContainer itemContainer, @Nonnull ResourceQuantity resource, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return itemContainer.writeAction(
         () -> {
            if (allOrNothing || exactAmount) {
               int testQuantityRemaining = testRemoveResourceFromItems(itemContainer, resource, resource.getQuantity(), filter);
               if (testQuantityRemaining > 0) {
                  return new ResourceTransaction(
                     false, ActionType.REMOVE, resource, resource.getQuantity(), 0, allOrNothing, exactAmount, filter, Collections.emptyList()
                  );
               }

               if (exactAmount && testQuantityRemaining < 0) {
                  return new ResourceTransaction(
                     false, ActionType.REMOVE, resource, resource.getQuantity(), 0, allOrNothing, exactAmount, filter, Collections.emptyList()
                  );
               }
            }

            List<ResourceSlotTransaction> list = new ObjectArrayList();
            int consumed = 0;
            int quantityRemaining = resource.getQuantity();

            for (short i = 0; i < itemContainer.getCapacity() && quantityRemaining > 0; i++) {
               ResourceQuantity clone = resource.clone(quantityRemaining);
               ResourceSlotTransaction transaction = internal_removeResourceFromSlot(itemContainer, i, clone, false, filter);
               if (transaction.succeeded()) {
                  list.add(transaction);
                  quantityRemaining = transaction.getRemainder();
                  consumed += transaction.getConsumed();
               }
            }

            return new ResourceTransaction(
               quantityRemaining != resource.getQuantity(), ActionType.REMOVE, resource, quantityRemaining, consumed, allOrNothing, exactAmount, filter, list
            );
         }
      );
   }

   protected static ListTransaction<ResourceTransaction> internal_removeResources(
      @Nonnull ItemContainer itemContainer, @Nullable List<ResourceQuantity> resources, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return resources != null && !resources.isEmpty()
         ? itemContainer.writeAction(
            () -> {
               if (allOrNothing || exactAmount) {
                  for (ResourceQuantity resource : resources) {
                     int testQuantityRemaining = testRemoveResourceFromItems(itemContainer, resource, resource.getQuantity(), filter);
                     if (testQuantityRemaining > 0) {
                        return new ListTransaction<>(
                           false,
                           resources.stream()
                              .map(
                                 remainder -> new ResourceTransaction(
                                    false, ActionType.REMOVE, resource, resource.getQuantity(), 0, allOrNothing, exactAmount, filter, Collections.emptyList()
                                 )
                              )
                              .collect(Collectors.toList())
                        );
                     }

                     if (exactAmount && testQuantityRemaining < 0) {
                        return new ListTransaction<>(
                           false,
                           resources.stream()
                              .map(
                                 remainder -> new ResourceTransaction(
                                    false, ActionType.REMOVE, resource, resource.getQuantity(), 0, allOrNothing, exactAmount, filter, Collections.emptyList()
                                 )
                              )
                              .collect(Collectors.toList())
                        );
                     }
                  }
               }

               List<ResourceTransaction> transactions = new ObjectArrayList();

               for (ResourceQuantity resource : resources) {
                  transactions.add(internal_removeResource(itemContainer, resource, allOrNothing, exactAmount, filter));
               }

               return new ListTransaction<>(true, transactions);
            }
         )
         : ListTransaction.getEmptyTransaction(true);
   }

   public static int testRemoveResourceFromItems(
      @Nonnull ItemContainer container, @Nonnull ResourceQuantity resource, int testQuantityRemaining, boolean filter
   ) {
      for (short i = 0; i < container.getCapacity() && testQuantityRemaining > 0; i++) {
         testQuantityRemaining = testRemoveResourceFromSlot(container, i, resource, testQuantityRemaining, filter);
      }

      return testQuantityRemaining;
   }

   public static TestRemoveItemSlotResult testRemoveResourceSlotFromItems(
      @Nonnull ItemContainer container, @Nonnull ResourceQuantity resource, int testQuantityRemaining, boolean filter
   ) {
      TestRemoveItemSlotResult result = new TestRemoveItemSlotResult(testQuantityRemaining);

      for (short i = 0; i < container.getCapacity() && result.quantityRemaining > 0; i++) {
         int newValue = testRemoveResourceFromSlot(container, i, resource, result.quantityRemaining, filter);
         if (newValue != result.quantityRemaining) {
            int diff = result.quantityRemaining - newValue;
            result.quantityRemaining = newValue;
            result.picked.put(i, diff);
         }
      }

      return result;
   }

   public static int testRemoveResourceFromSlot(
      @Nonnull ItemContainer container, short slot, @Nonnull ResourceQuantity resource, int testQuantityRemaining, boolean filter
   ) {
      if (filter && container.cantRemoveFromSlot(slot)) {
         return testQuantityRemaining;
      } else {
         ItemStack slotItemStack = container.internal_getSlot(slot);
         if (ItemStack.isEmpty(slotItemStack)) {
            return testQuantityRemaining;
         } else {
            Item slotItem = slotItemStack.getItem();
            ItemResourceType resourceType = resource.getResourceType(slotItem);
            if (resourceType == null) {
               return testQuantityRemaining;
            } else {
               int resourceTypeQuantity = resourceType.quantity;
               int quantityInItemsRemaining = MathUtil.ceil((double)testQuantityRemaining / resourceTypeQuantity);
               int quantityInItems = slotItemStack.getQuantity();
               int quantityInItemsAdjustment = Math.min(quantityInItems, quantityInItemsRemaining);
               int quantityAdjustment = quantityInItemsAdjustment * resourceTypeQuantity;
               return testQuantityRemaining - quantityAdjustment;
            }
         }
      }
   }
}
