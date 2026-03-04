package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ResourceSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.TagSlotTransaction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InternalContainerUtilMaterial {
   @Nonnull
   protected static MaterialSlotTransaction internal_removeMaterialFromSlot(
      @Nonnull ItemContainer itemContainer, short slot, @Nonnull MaterialQuantity material, boolean allOrNothing, boolean filter
   ) {
      ItemContainer.validateSlotIndex(slot, itemContainer.getCapacity());
      ItemContainer.validateQuantity(material.getQuantity());
      if (material.getItemId() != null) {
         ItemStackSlotTransaction slotTransaction = InternalContainerUtilItemStack.internal_removeItemStackFromSlot(
            itemContainer, slot, material.toItemStack(), material.getQuantity(), allOrNothing, filter, (a, b) -> ItemStack.isEquivalentType(a, b)
         );
         return new MaterialSlotTransaction(
            material, slotTransaction.getRemainder() != null ? slotTransaction.getRemainder().getQuantity() : 0, slotTransaction
         );
      } else if (material.getTagIndex() != Integer.MIN_VALUE) {
         TagSlotTransaction tagTransaction = InternalContainerUtilTag.internal_removeTagFromSlot(
            itemContainer, slot, material.getTagIndex(), material.getQuantity(), allOrNothing, filter
         );
         return new MaterialSlotTransaction(material, tagTransaction.getRemainder(), tagTransaction);
      } else {
         ResourceSlotTransaction resourceTransaction = InternalContainerUtilResource.internal_removeResourceFromSlot(
            itemContainer, slot, material.toResource(), allOrNothing, filter
         );
         return new MaterialSlotTransaction(material, resourceTransaction.getRemainder(), resourceTransaction);
      }
   }

   protected static MaterialTransaction internal_removeMaterial(
      @Nonnull ItemContainer itemContainer, @Nonnull MaterialQuantity material, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return itemContainer.writeAction(
         () -> {
            if (allOrNothing || exactAmount) {
               int testQuantityRemaining = testRemoveMaterialFromItems(itemContainer, material, material.getQuantity(), filter);
               if (testQuantityRemaining > 0) {
                  return new MaterialTransaction(
                     false, ActionType.REMOVE, material, material.getQuantity(), allOrNothing, exactAmount, filter, Collections.emptyList()
                  );
               }

               if (exactAmount && testQuantityRemaining < 0) {
                  return new MaterialTransaction(
                     false, ActionType.REMOVE, material, material.getQuantity(), allOrNothing, exactAmount, filter, Collections.emptyList()
                  );
               }
            }

            List<MaterialSlotTransaction> list = new ObjectArrayList();
            int quantityRemaining = material.getQuantity();

            for (short i = 0; i < itemContainer.getCapacity() && quantityRemaining > 0; i++) {
               MaterialQuantity clone = material.clone(quantityRemaining);
               MaterialSlotTransaction transaction = internal_removeMaterialFromSlot(itemContainer, i, clone, false, filter);
               if (transaction.succeeded()) {
                  list.add(transaction);
                  quantityRemaining = transaction.getRemainder();
               }
            }

            return new MaterialTransaction(
               quantityRemaining != material.getQuantity(), ActionType.REMOVE, material, material.getQuantity(), allOrNothing, exactAmount, filter, list
            );
         }
      );
   }

   protected static ListTransaction<MaterialTransaction> internal_removeMaterials(
      @Nonnull ItemContainer itemContainer, @Nullable List<MaterialQuantity> materials, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return materials != null && !materials.isEmpty()
         ? itemContainer.writeAction(
            () -> {
               if (allOrNothing || exactAmount) {
                  for (MaterialQuantity material : materials) {
                     int testQuantityRemaining = testRemoveMaterialFromItems(itemContainer, material, material.getQuantity(), filter);
                     if (testQuantityRemaining > 0) {
                        return new ListTransaction<>(
                           false,
                           materials.stream()
                              .map(
                                 remainder -> new MaterialTransaction(
                                    false, ActionType.REMOVE, material, material.getQuantity(), allOrNothing, exactAmount, filter, Collections.emptyList()
                                 )
                              )
                              .collect(Collectors.toList())
                        );
                     }

                     if (exactAmount && testQuantityRemaining < 0) {
                        return new ListTransaction<>(
                           false,
                           materials.stream()
                              .map(
                                 remainder -> new MaterialTransaction(
                                    false, ActionType.REMOVE, material, material.getQuantity(), allOrNothing, exactAmount, filter, Collections.emptyList()
                                 )
                              )
                              .collect(Collectors.toList())
                        );
                     }
                  }
               }

               List<MaterialTransaction> transactions = new ObjectArrayList();

               for (MaterialQuantity material : materials) {
                  transactions.add(internal_removeMaterial(itemContainer, material, allOrNothing, exactAmount, filter));
               }

               return new ListTransaction<>(true, transactions);
            }
         )
         : ListTransaction.getEmptyTransaction(true);
   }

   public static int testRemoveMaterialFromItems(
      @Nonnull ItemContainer container, @Nonnull MaterialQuantity material, int testQuantityRemaining, boolean filter
   ) {
      if (material.getItemId() != null) {
         return InternalContainerUtilItemStack.testRemoveItemStackFromItems(container, material.toItemStack(), testQuantityRemaining, filter);
      } else {
         return material.getTagIndex() != Integer.MIN_VALUE
            ? InternalContainerUtilTag.testRemoveTagFromItems(container, material.getTagIndex(), testQuantityRemaining, filter)
            : InternalContainerUtilResource.testRemoveResourceFromItems(container, material.toResource(), testQuantityRemaining, filter);
      }
   }

   public static TestRemoveItemSlotResult getTestRemoveMaterialFromItems(
      @Nonnull ItemContainer container, @Nonnull MaterialQuantity material, int testQuantityRemaining, boolean filter
   ) {
      if (material.getItemId() != null) {
         return InternalContainerUtilItemStack.testRemoveItemStackSlotFromItems(
            container, material.toItemStack(), testQuantityRemaining, filter, (a, b) -> ItemStack.isEquivalentType(a, b)
         );
      } else {
         return material.getTagIndex() != Integer.MIN_VALUE
            ? InternalContainerUtilTag.testRemoveTagSlotFromItems(container, material.getTagIndex(), testQuantityRemaining, filter)
            : InternalContainerUtilResource.testRemoveResourceSlotFromItems(container, material.toResource(), testQuantityRemaining, filter);
      }
   }

   protected static ListTransaction<MaterialSlotTransaction> internal_removeMaterialsOrdered(
      @Nonnull ItemContainer itemContainer, short offset, @Nullable List<MaterialQuantity> materials, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      if (materials != null && !materials.isEmpty()) {
         return offset + materials.size() > itemContainer.getCapacity()
            ? ListTransaction.getEmptyTransaction(false)
            : itemContainer.writeAction(
               () -> {
                  if (allOrNothing || exactAmount) {
                     for (short i = 0; i < materials.size(); i++) {
                        short slot = (short)(offset + i);
                        MaterialQuantity material = materials.get(i);
                        int testQuantityRemaining = testRemoveMaterialFromSlot(itemContainer, slot, material, material.getQuantity(), filter);
                        if (testQuantityRemaining > 0) {
                           List<MaterialSlotTransaction> list = new ObjectArrayList();

                           for (short i1 = 0; i1 < materials.size(); i1++) {
                              short islot = (short)(offset + i1);
                              list.add(
                                 new MaterialSlotTransaction(
                                    material,
                                    material.getQuantity(),
                                    new SlotTransaction(false, ActionType.REMOVE, islot, null, null, null, allOrNothing, exactAmount, filter)
                                 )
                              );
                           }

                           return new ListTransaction<>(false, list);
                        }

                        if (exactAmount && testQuantityRemaining < 0) {
                           List<MaterialSlotTransaction> list = new ObjectArrayList();

                           for (short i1 = 0; i1 < materials.size(); i1++) {
                              short islot = (short)(offset + i1);
                              list.add(
                                 new MaterialSlotTransaction(
                                    material,
                                    material.getQuantity(),
                                    new SlotTransaction(false, ActionType.REMOVE, islot, null, null, null, allOrNothing, exactAmount, filter)
                                 )
                              );
                           }

                           return new ListTransaction<>(false, list);
                        }
                     }
                  }

                  List<MaterialSlotTransaction> transactions = new ObjectArrayList();

                  for (short i = 0; i < materials.size(); i++) {
                     short slotx = (short)(offset + i);
                     MaterialQuantity materialx = materials.get(i);
                     transactions.add(internal_removeMaterialFromSlot(itemContainer, slotx, materialx, allOrNothing, filter));
                  }

                  return new ListTransaction<>(true, transactions);
               }
            );
      } else {
         return ListTransaction.getEmptyTransaction(true);
      }
   }

   public static int testRemoveMaterialFromSlot(
      @Nonnull ItemContainer container, short slot, @Nonnull MaterialQuantity material, int testQuantityRemaining, boolean filter
   ) {
      if (material.getItemId() != null) {
         return InternalContainerUtilItemStack.testRemoveItemStackFromSlot(
            container, slot, material.toItemStack(), testQuantityRemaining, filter, (a, b) -> ItemStack.isEquivalentType(a, b)
         );
      } else {
         return material.getTagIndex() != Integer.MIN_VALUE
            ? InternalContainerUtilTag.testRemoveTagFromSlot(container, slot, material.getTagIndex(), testQuantityRemaining, filter)
            : InternalContainerUtilResource.testRemoveResourceFromSlot(container, slot, material.toResource(), testQuantityRemaining, filter);
      }
   }
}
