package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.fastutil.shorts.Short2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.SyncEventBusRegistry;
import com.hypixel.hytale.function.consumer.ShortObjectConsumer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InventorySection;
import com.hypixel.hytale.protocol.ItemResourceType;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ActionType;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveType;
import com.hypixel.hytale.server.core.inventory.transaction.ResourceSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ResourceTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.TagSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.TagTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ItemContainer {
   public static final CodecMapCodec<ItemContainer> CODEC = new CodecMapCodec<>(true);
   public static final boolean DEFAULT_ADD_ALL_OR_NOTHING = false;
   public static final boolean DEFAULT_REMOVE_ALL_OR_NOTHING = true;
   public static final boolean DEFAULT_FULL_STACKS = false;
   public static final boolean DEFAULT_EXACT_AMOUNT = true;
   public static final boolean DEFAULT_FILTER = true;
   protected static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   protected final SyncEventBusRegistry<Void, ItemContainer.ItemContainerChangeEvent> externalChangeEventRegistry = new SyncEventBusRegistry<>(
      LOGGER, ItemContainer.ItemContainerChangeEvent.class
   );
   protected final SyncEventBusRegistry<Void, ItemContainer.ItemContainerChangeEvent> internalChangeEventRegistry = new SyncEventBusRegistry<>(
      LOGGER, ItemContainer.ItemContainerChangeEvent.class
   );

   public abstract short getCapacity();

   public abstract void setGlobalFilter(FilterType var1);

   public abstract void setSlotFilter(FilterActionType var1, short var2, SlotFilter var3);

   public abstract ItemContainer clone();

   protected abstract <V> V readAction(Supplier<V> var1);

   protected abstract <X, V> V readAction(Function<X, V> var1, X var2);

   protected abstract <V> V writeAction(Supplier<V> var1);

   protected abstract <X, V> V writeAction(Function<X, V> var1, X var2);

   protected abstract ClearTransaction internal_clear();

   @Nullable
   protected abstract ItemStack internal_getSlot(short var1);

   @Nullable
   protected abstract ItemStack internal_setSlot(short var1, ItemStack var2);

   @Nullable
   protected abstract ItemStack internal_removeSlot(short var1);

   protected abstract boolean cantAddToSlot(short var1, ItemStack var2, ItemStack var3);

   protected abstract boolean cantRemoveFromSlot(short var1);

   protected abstract boolean cantDropFromSlot(short var1);

   protected abstract boolean cantMoveToSlot(ItemContainer var1, short var2);

   @Nonnull
   public InventorySection toPacket() {
      InventorySection packet = new InventorySection();
      packet.capacity = this.getCapacity();
      packet.items = this.toProtocolMap();
      return packet;
   }

   @Nonnull
   public Map<Integer, ItemWithAllMetadata> toProtocolMap() {
      Map<Integer, ItemWithAllMetadata> map = new Int2ObjectOpenHashMap();
      this.forEachWithMeta((slot, itemStack, _map) -> {
         if (!ItemStack.isEmpty(itemStack) && itemStack.isValid()) {
            _map.put(Integer.valueOf(slot), itemStack.toPacket());
         }
      }, map);
      return map;
   }

   public EventRegistration registerChangeEvent(@Nonnull Consumer<ItemContainer.ItemContainerChangeEvent> consumer) {
      return this.registerChangeEvent((short)0, consumer);
   }

   public EventRegistration registerChangeEvent(@Nonnull EventPriority priority, @Nonnull Consumer<ItemContainer.ItemContainerChangeEvent> consumer) {
      return this.registerChangeEvent(priority.getValue(), consumer);
   }

   public EventRegistration registerChangeEvent(short priority, @Nonnull Consumer<ItemContainer.ItemContainerChangeEvent> consumer) {
      return this.externalChangeEventRegistry.register(priority, null, consumer);
   }

   public ClearTransaction clear() {
      ClearTransaction transaction = this.writeAction(this::internal_clear);
      this.sendUpdate(transaction);
      return transaction;
   }

   public boolean canAddItemStackToSlot(short slot, @Nonnull ItemStack itemStack, boolean allOrNothing, boolean filter) {
      validateSlotIndex(slot, this.getCapacity());
      return this.writeAction(() -> {
         int quantityRemaining = itemStack.getQuantity();
         ItemStack slotItemStack = this.internal_getSlot(slot);
         if (filter && this.cantAddToSlot(slot, itemStack, slotItemStack)) {
            return false;
         } else if (slotItemStack == null) {
            return true;
         } else if (!itemStack.isStackableWith(slotItemStack)) {
            return false;
         } else {
            int quantity = slotItemStack.getQuantity();
            int quantityAdjustment = Math.min(slotItemStack.getItem().getMaxStack() - quantity, quantityRemaining);
            int newQuantityRemaining = quantityRemaining - quantityAdjustment;
            return allOrNothing ? quantityRemaining <= 0 : quantityRemaining != newQuantityRemaining;
         }
      });
   }

   @Nonnull
   public ItemStackSlotTransaction addItemStackToSlot(short slot, @Nonnull ItemStack itemStack) {
      return this.addItemStackToSlot(slot, itemStack, false, true);
   }

   @Nonnull
   public ItemStackSlotTransaction addItemStackToSlot(short slot, @Nonnull ItemStack itemStack, boolean allOrNothing, boolean filter) {
      ItemStackSlotTransaction transaction = InternalContainerUtilItemStack.internal_addItemStackToSlot(this, slot, itemStack, allOrNothing, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   @Nonnull
   public ItemStackSlotTransaction setItemStackForSlot(short slot, ItemStack itemStack) {
      return this.setItemStackForSlot(slot, itemStack, true);
   }

   @Nonnull
   public ItemStackSlotTransaction setItemStackForSlot(short slot, ItemStack itemStack, boolean filter) {
      ItemStackSlotTransaction transaction = InternalContainerUtilItemStack.internal_setItemStackForSlot(this, slot, itemStack, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   @Nullable
   public ItemStack getItemStack(short slot) {
      validateSlotIndex(slot, this.getCapacity());
      return this.readAction(() -> this.internal_getSlot(slot));
   }

   @Nonnull
   public ItemStackSlotTransaction replaceItemStackInSlot(short slot, ItemStack itemStackToRemove, ItemStack itemStack) {
      ItemStackSlotTransaction transaction = this.internal_replaceItemStack(slot, itemStackToRemove, itemStack);
      this.sendUpdate(transaction);
      return transaction;
   }

   public ListTransaction<ItemStackSlotTransaction> replaceAll(SlotReplacementFunction func) {
      return this.replaceAll(func, true);
   }

   private ListTransaction<ItemStackSlotTransaction> replaceAll(SlotReplacementFunction func, boolean ignoreEmpty) {
      ListTransaction<ItemStackSlotTransaction> transaction = this.writeAction(
         () -> {
            short capacity = this.getCapacity();
            ObjectArrayList<ItemStackSlotTransaction> transactionsList = new ObjectArrayList(capacity);

            for (short slot = 0; slot < capacity; slot++) {
               ItemStack existing = this.internal_getSlot(slot);
               if (!ignoreEmpty || !ItemStack.isEmpty(existing)) {
                  ItemStack replacement = func.replace(slot, existing);
                  this.internal_setSlot(slot, replacement);
                  transactionsList.add(
                     new ItemStackSlotTransaction(
                        true, ActionType.REPLACE, slot, existing, replacement, existing, true, false, false, false, replacement, replacement
                     )
                  );
               }
            }

            return new ListTransaction<>(true, transactionsList);
         }
      );
      this.sendUpdate(transaction);
      return transaction;
   }

   protected ItemStackSlotTransaction internal_replaceItemStack(short slot, @Nullable ItemStack itemStackToRemove, ItemStack itemStack) {
      validateSlotIndex(slot, this.getCapacity());
      return this.writeAction(
         () -> {
            ItemStack slotItemStack = this.internal_getSlot(slot);
            if ((slotItemStack != null || itemStackToRemove == null)
               && (slotItemStack == null || itemStackToRemove != null)
               && (slotItemStack == null || itemStackToRemove.isStackableWith(slotItemStack))) {
               this.internal_setSlot(slot, itemStack);
               return new ItemStackSlotTransaction(
                  true, ActionType.REPLACE, slot, slotItemStack, itemStack, slotItemStack, true, false, false, false, itemStack, null
               );
            } else {
               return new ItemStackSlotTransaction(
                  false, ActionType.REPLACE, slot, slotItemStack, slotItemStack, null, true, false, false, false, itemStack, itemStack
               );
            }
         }
      );
   }

   @Nonnull
   public SlotTransaction removeItemStackFromSlot(short slot) {
      return this.removeItemStackFromSlot(slot, true);
   }

   @Nonnull
   public SlotTransaction removeItemStackFromSlot(short slot, boolean filter) {
      SlotTransaction transaction = InternalContainerUtilItemStack.internal_removeItemStackFromSlot(this, slot, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   @Nonnull
   public ItemStackSlotTransaction removeItemStackFromSlot(short slot, int quantityToRemove) {
      return this.removeItemStackFromSlot(slot, quantityToRemove, true, true);
   }

   @Nonnull
   public ItemStackSlotTransaction removeItemStackFromSlot(short slot, int quantityToRemove, boolean allOrNothing, boolean filter) {
      ItemStackSlotTransaction transaction = InternalContainerUtilItemStack.internal_removeItemStackFromSlot(this, slot, quantityToRemove, allOrNothing, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   @Deprecated
   public ItemStackSlotTransaction internal_removeItemStack(short slot, int quantityToRemove) {
      return InternalContainerUtilItemStack.internal_removeItemStackFromSlot(this, slot, quantityToRemove, true, true);
   }

   @Nonnull
   public ItemStackSlotTransaction removeItemStackFromSlot(short slot, ItemStack itemStackToRemove, int quantityToRemove) {
      return this.removeItemStackFromSlot(slot, itemStackToRemove, quantityToRemove, true, true);
   }

   @Nonnull
   public ItemStackSlotTransaction removeItemStackFromSlot(short slot, ItemStack itemStackToRemove, int quantityToRemove, boolean allOrNothing, boolean filter) {
      ItemStackSlotTransaction transaction = InternalContainerUtilItemStack.internal_removeItemStackFromSlot(
         this, slot, itemStackToRemove, quantityToRemove, allOrNothing, filter
      );
      this.sendUpdate(transaction);
      return transaction;
   }

   @Nonnull
   public MaterialSlotTransaction removeMaterialFromSlot(short slot, @Nonnull MaterialQuantity material) {
      return this.removeMaterialFromSlot(slot, material, true, true, true);
   }

   @Nonnull
   public MaterialSlotTransaction removeMaterialFromSlot(
      short slot, @Nonnull MaterialQuantity material, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      MaterialSlotTransaction transaction = InternalContainerUtilMaterial.internal_removeMaterialFromSlot(this, slot, material, allOrNothing, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   @Nonnull
   public ResourceSlotTransaction removeResourceFromSlot(short slot, @Nonnull ResourceQuantity resource) {
      return this.removeResourceFromSlot(slot, resource, true, true, true);
   }

   @Nonnull
   public ResourceSlotTransaction removeResourceFromSlot(
      short slot, @Nonnull ResourceQuantity resource, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      ResourceSlotTransaction transaction = InternalContainerUtilResource.internal_removeResourceFromSlot(this, slot, resource, allOrNothing, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   @Nonnull
   public TagSlotTransaction removeTagFromSlot(short slot, int tagIndex, int quantity) {
      return this.removeTagFromSlot(slot, tagIndex, quantity, true, true);
   }

   @Nonnull
   public TagSlotTransaction removeTagFromSlot(short slot, int tagIndex, int quantity, boolean allOrNothing, boolean filter) {
      TagSlotTransaction transaction = InternalContainerUtilTag.internal_removeTagFromSlot(this, slot, tagIndex, quantity, allOrNothing, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   @Nonnull
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(short slot, @Nonnull ItemContainer containerTo) {
      return this.moveItemStackFromSlot(slot, containerTo, true);
   }

   @Nonnull
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(short slot, @Nonnull ItemContainer containerTo, boolean filter) {
      return this.moveItemStackFromSlot(slot, containerTo, false, filter);
   }

   @Nonnull
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(short slot, @Nonnull ItemContainer containerTo, boolean allOrNothing, boolean filter) {
      MoveTransaction<ItemStackTransaction> transaction = this.internal_moveItemStackFromSlot(slot, containerTo, allOrNothing, filter);
      this.sendUpdate(transaction);
      containerTo.sendUpdate(transaction.toInverted(this));
      return transaction;
   }

   protected MoveTransaction<ItemStackTransaction> internal_moveItemStackFromSlot(
      short slot, @Nonnull ItemContainer containerTo, boolean allOrNothing, boolean filter
   ) {
      validateSlotIndex(slot, this.getCapacity());
      return this.writeAction(() -> containerTo.writeAction(() -> {
         if (filter && this.cantRemoveFromSlot(slot)) {
            return null;
         } else {
            ItemStack itemFrom = this.internal_removeSlot(slot);
            if (ItemStack.isEmpty(itemFrom)) {
               SlotTransaction slotTransaction = new SlotTransaction(false, ActionType.REMOVE, slot, null, null, null, false, false, filter);
               return new MoveTransaction<>(false, slotTransaction, MoveType.MOVE_FROM_SELF, containerTo, ItemStackTransaction.FAILED_ADD);
            } else {
               SlotTransaction fromTransaction = new SlotTransaction(true, ActionType.REMOVE, slot, itemFrom, null, null, false, false, filter);
               ItemStackTransaction addTransaction = InternalContainerUtilItemStack.internal_addItemStack(containerTo, itemFrom, allOrNothing, false, filter);
               ItemStack remainder = addTransaction.getRemainder();
               if (!ItemStack.isEmpty(remainder)) {
                  InternalContainerUtilItemStack.internal_addItemStackToSlot(this, slot, remainder, allOrNothing, false);
               }

               return new MoveTransaction<>(addTransaction.succeeded(), fromTransaction, MoveType.MOVE_FROM_SELF, containerTo, addTransaction);
            }
         }
      }));
   }

   @Nonnull
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(short slot, int quantity, @Nonnull ItemContainer containerTo) {
      return this.moveItemStackFromSlot(slot, quantity, containerTo, false, true);
   }

   @Nonnull
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(
      short slot, int quantity, @Nonnull ItemContainer containerTo, boolean allOrNothing, boolean filter
   ) {
      MoveTransaction<ItemStackTransaction> transaction = this.internal_moveItemStackFromSlot(slot, quantity, containerTo, allOrNothing, filter);
      this.sendUpdate(transaction);
      containerTo.sendUpdate(transaction.toInverted(this));
      return transaction;
   }

   protected MoveTransaction<ItemStackTransaction> internal_moveItemStackFromSlot(
      short slot, int quantity, @Nonnull ItemContainer containerTo, boolean allOrNothing, boolean filter
   ) {
      validateSlotIndex(slot, this.getCapacity());
      validateQuantity(quantity);
      return this.writeAction(
         () -> containerTo.writeAction(
            () -> {
               if (filter && this.cantRemoveFromSlot(slot)) {
                  return null;
               } else if (filter && containerTo.cantMoveToSlot(this, slot)) {
                  ItemStack itemStack = this.internal_getSlot(slot);
                  SlotTransaction slotTransaction = new SlotTransaction(false, ActionType.REMOVE, slot, itemStack, itemStack, null, false, false, filter);
                  return new MoveTransaction<>(false, slotTransaction, MoveType.MOVE_FROM_SELF, containerTo, ItemStackTransaction.FAILED_ADD);
               } else {
                  ItemStackSlotTransaction fromTransaction = this.internal_removeItemStack(slot, quantity);
                  if (!fromTransaction.succeeded()) {
                     SlotTransaction slotTransaction = new SlotTransaction(false, ActionType.REMOVE, slot, null, null, null, false, false, filter);
                     return new MoveTransaction<>(false, slotTransaction, MoveType.MOVE_FROM_SELF, containerTo, ItemStackTransaction.FAILED_ADD);
                  } else {
                     ItemStack itemFrom = fromTransaction.getOutput();
                     if (ItemStack.isEmpty(itemFrom)) {
                        SlotTransaction slotTransaction = new SlotTransaction(false, ActionType.REMOVE, slot, null, null, null, false, false, filter);
                        return new MoveTransaction<>(false, slotTransaction, MoveType.MOVE_FROM_SELF, containerTo, ItemStackTransaction.FAILED_ADD);
                     } else {
                        ItemStackTransaction addTransaction = InternalContainerUtilItemStack.internal_addItemStack(
                           containerTo, itemFrom, allOrNothing, false, filter
                        );
                        ItemStack remainder = addTransaction.getRemainder();
                        if (!ItemStack.isEmpty(remainder)) {
                           InternalContainerUtilItemStack.internal_addItemStackToSlot(this, slot, remainder, allOrNothing, false);
                        }

                        return new MoveTransaction<>(addTransaction.succeeded(), fromTransaction, MoveType.MOVE_FROM_SELF, containerTo, addTransaction);
                     }
                  }
               }
            }
         )
      );
   }

   @Nonnull
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveItemStackFromSlot(short slot, ItemContainer... containerTo) {
      return this.moveItemStackFromSlot(slot, false, true, containerTo);
   }

   @Nonnull
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveItemStackFromSlot(
      short slot, boolean allOrNothing, boolean filter, @Nonnull ItemContainer... containerTo
   ) {
      ListTransaction<MoveTransaction<ItemStackTransaction>> transaction = this.internal_moveItemStackFromSlot(slot, allOrNothing, filter, containerTo);
      this.sendUpdate(transaction);

      for (MoveTransaction<ItemStackTransaction> moveItemStackTransaction : transaction.getList()) {
         moveItemStackTransaction.getOtherContainer().sendUpdate(moveItemStackTransaction.toInverted(this));
      }

      return transaction;
   }

   @Nonnull
   private ListTransaction<MoveTransaction<ItemStackTransaction>> internal_moveItemStackFromSlot(
      short slot, boolean allOrNothing, boolean filter, @Nonnull ItemContainer[] containerTo
   ) {
      List<MoveTransaction<ItemStackTransaction>> transactions = new ObjectArrayList();

      for (ItemContainer itemContainer : containerTo) {
         MoveTransaction<ItemStackTransaction> transaction = this.internal_moveItemStackFromSlot(slot, itemContainer, allOrNothing, filter);
         transactions.add(transaction);
         if (transaction.succeeded()) {
            ItemStackTransaction addTransaction = transaction.getAddTransaction();
            if (ItemStack.isEmpty(addTransaction.getRemainder())) {
               break;
            }
         }
      }

      return new ListTransaction<>(!transactions.isEmpty(), transactions);
   }

   @Nonnull
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveItemStackFromSlot(short slot, int quantity, ItemContainer... containerTo) {
      return this.moveItemStackFromSlot(slot, quantity, false, true, containerTo);
   }

   @Nonnull
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveItemStackFromSlot(
      short slot, int quantity, boolean allOrNothing, boolean filter, @Nonnull ItemContainer... containerTo
   ) {
      ListTransaction<MoveTransaction<ItemStackTransaction>> transaction = this.internal_moveItemStackFromSlot(
         slot, quantity, allOrNothing, filter, containerTo
      );
      this.sendUpdate(transaction);

      for (MoveTransaction<ItemStackTransaction> moveItemStackTransaction : transaction.getList()) {
         moveItemStackTransaction.getOtherContainer().sendUpdate(moveItemStackTransaction.toInverted(this));
      }

      return transaction;
   }

   @Nonnull
   private ListTransaction<MoveTransaction<ItemStackTransaction>> internal_moveItemStackFromSlot(
      short slot, int quantity, boolean allOrNothing, boolean filter, @Nonnull ItemContainer[] containerTo
   ) {
      List<MoveTransaction<ItemStackTransaction>> transactions = new ObjectArrayList();

      for (ItemContainer itemContainer : containerTo) {
         MoveTransaction<ItemStackTransaction> transaction = this.internal_moveItemStackFromSlot(slot, quantity, itemContainer, allOrNothing, filter);
         transactions.add(transaction);
         if (transaction.succeeded()) {
            ItemStackTransaction addTransaction = transaction.getAddTransaction();
            if (ItemStack.isEmpty(addTransaction.getRemainder())) {
               break;
            }
         }
      }

      return new ListTransaction<>(!transactions.isEmpty(), transactions);
   }

   @Nonnull
   public MoveTransaction<SlotTransaction> moveItemStackFromSlotToSlot(short slot, int quantity, @Nonnull ItemContainer containerTo, short slotTo) {
      return this.moveItemStackFromSlotToSlot(slot, quantity, containerTo, slotTo, true);
   }

   @Nonnull
   public MoveTransaction<SlotTransaction> moveItemStackFromSlotToSlot(
      short slot, int quantity, @Nonnull ItemContainer containerTo, short slotTo, boolean filter
   ) {
      MoveTransaction<SlotTransaction> transaction = this.internal_moveItemStackFromSlot(slot, quantity, containerTo, slotTo, filter);
      this.sendUpdate(transaction);
      containerTo.sendUpdate(transaction.toInverted(this));
      return transaction;
   }

   protected MoveTransaction<SlotTransaction> internal_moveItemStackFromSlot(
      short slot, int quantity, @Nonnull ItemContainer containerTo, short slotTo, boolean filter
   ) {
      validateSlotIndex(slot, this.getCapacity());
      validateSlotIndex(slotTo, containerTo.getCapacity());
      validateQuantity(quantity);
      return this.writeAction(
         () -> containerTo.writeAction(
            () -> {
               if (filter && this.cantRemoveFromSlot(slot)) {
                  ItemStack itemStack = this.internal_getSlot(slot);
                  SlotTransaction slotTransaction = new SlotTransaction(false, ActionType.REMOVE, slot, itemStack, itemStack, null, false, false, filter);
                  return new MoveTransaction<>(false, slotTransaction, MoveType.MOVE_FROM_SELF, containerTo, SlotTransaction.FAILED_ADD);
               } else if (filter && containerTo.cantMoveToSlot(this, slot)) {
                  ItemStack itemStack = this.internal_getSlot(slot);
                  SlotTransaction slotTransaction = new SlotTransaction(false, ActionType.REMOVE, slot, itemStack, itemStack, null, false, false, filter);
                  return new MoveTransaction<>(false, slotTransaction, MoveType.MOVE_FROM_SELF, containerTo, SlotTransaction.FAILED_ADD);
               } else {
                  ItemStackSlotTransaction fromTransaction = this.internal_removeItemStack(slot, quantity);
                  if (!fromTransaction.succeeded()) {
                     return new MoveTransaction<>(false, fromTransaction, MoveType.MOVE_FROM_SELF, containerTo, SlotTransaction.FAILED_ADD);
                  } else {
                     ItemStack itemFrom = fromTransaction.getOutput();
                     if (ItemStack.isEmpty(itemFrom)) {
                        return new MoveTransaction<>(true, fromTransaction, MoveType.MOVE_FROM_SELF, containerTo, SlotTransaction.FAILED_ADD);
                     } else {
                        ItemStack itemTo = containerTo.getItemStack(slotTo);
                        if (filter && containerTo.cantAddToSlot(slotTo, itemFrom, itemTo)) {
                           this.internal_setSlot(slot, fromTransaction.getSlotBefore());
                           SlotTransaction slotTransaction = new SlotTransaction(
                              true, ActionType.REMOVE, slot, fromTransaction.getSlotBefore(), fromTransaction.getSlotAfter(), null, false, false, filter
                           );
                           SlotTransaction addTransaction = new SlotTransaction(false, ActionType.ADD, slotTo, itemTo, itemTo, null, false, false, filter);
                           return new MoveTransaction<>(false, slotTransaction, MoveType.MOVE_FROM_SELF, containerTo, addTransaction);
                        } else if (ItemStack.isEmpty(itemTo)) {
                           ItemStackSlotTransaction addTransaction = InternalContainerUtilItemStack.internal_setItemStackForSlot(
                              containerTo, slotTo, itemFrom, filter
                           );
                           return new MoveTransaction<>(true, fromTransaction, MoveType.MOVE_FROM_SELF, containerTo, addTransaction);
                        } else if (!itemFrom.isStackableWith(itemTo)) {
                           if (ItemStack.isEmpty(fromTransaction.getSlotAfter())) {
                              if (filter && this.cantAddToSlot(slot, itemTo, itemFrom)) {
                                 this.internal_setSlot(slot, fromTransaction.getSlotBefore());
                                 SlotTransaction slotTransaction = new SlotTransaction(
                                    true, ActionType.REMOVE, slot, fromTransaction.getSlotBefore(), fromTransaction.getSlotAfter(), null, false, false, filter
                                 );
                                 SlotTransaction addTransaction = new SlotTransaction(false, ActionType.ADD, slotTo, itemTo, itemTo, null, false, false, filter);
                                 return new MoveTransaction<>(false, slotTransaction, MoveType.MOVE_FROM_SELF, containerTo, addTransaction);
                              } else {
                                 this.internal_setSlot(slot, itemTo);
                                 containerTo.internal_setSlot(slotTo, itemFrom);
                                 SlotTransaction from = new SlotTransaction(true, ActionType.REPLACE, slot, itemFrom, itemTo, null, false, false, filter);
                                 SlotTransaction to = new SlotTransaction(true, ActionType.REPLACE, slotTo, itemTo, itemFrom, null, false, false, filter);
                                 return new MoveTransaction<>(true, from, MoveType.MOVE_FROM_SELF, containerTo, to);
                              }
                           } else {
                              this.internal_setSlot(slot, fromTransaction.getSlotBefore());
                              SlotTransaction slotTransaction = new SlotTransaction(
                                 true, ActionType.REMOVE, slot, fromTransaction.getSlotBefore(), fromTransaction.getSlotAfter(), null, false, false, filter
                              );
                              SlotTransaction addTransaction = new SlotTransaction(false, ActionType.ADD, slotTo, itemTo, itemTo, null, false, false, filter);
                              return new MoveTransaction<>(false, slotTransaction, MoveType.MOVE_FROM_SELF, containerTo, addTransaction);
                           }
                        } else {
                           int maxStack = itemFrom.getItem().getMaxStack();
                           int newQuantity = itemFrom.getQuantity() + itemTo.getQuantity();
                           if (newQuantity <= maxStack) {
                              ItemStackSlotTransaction addTransaction = InternalContainerUtilItemStack.internal_setItemStackForSlot(
                                 containerTo, slotTo, itemTo.withQuantity(newQuantity), filter
                              );
                              return new MoveTransaction<>(true, fromTransaction, MoveType.MOVE_FROM_SELF, containerTo, addTransaction);
                           } else {
                              ItemStackSlotTransaction addTransaction = InternalContainerUtilItemStack.internal_setItemStackForSlot(
                                 containerTo, slotTo, itemTo.withQuantity(maxStack), filter
                              );
                              int remainder = newQuantity - maxStack;
                              int quantityLeft = !ItemStack.isEmpty(fromTransaction.getSlotAfter()) ? fromTransaction.getSlotAfter().getQuantity() : 0;
                              this.internal_setSlot(slot, itemFrom.withQuantity(remainder + quantityLeft));
                              return new MoveTransaction<>(true, fromTransaction, MoveType.MOVE_FROM_SELF, containerTo, addTransaction);
                           }
                        }
                     }
                  }
               }
            }
         )
      );
   }

   @Nonnull
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveAllItemStacksTo(ItemContainer... containerTo) {
      return this.moveAllItemStacksTo(null, containerTo);
   }

   @Nonnull
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveAllItemStacksTo(Predicate<ItemStack> itemPredicate, ItemContainer... containerTo) {
      ListTransaction<MoveTransaction<ItemStackTransaction>> transaction = this.internal_moveAllItemStacksTo(itemPredicate, containerTo);
      this.sendUpdate(transaction);

      for (MoveTransaction<ItemStackTransaction> moveItemStackTransaction : transaction.getList()) {
         moveItemStackTransaction.getOtherContainer().sendUpdate(moveItemStackTransaction.toInverted(this));
      }

      return transaction;
   }

   @Nonnull
   protected ListTransaction<MoveTransaction<ItemStackTransaction>> internal_moveAllItemStacksTo(
      @Nullable Predicate<ItemStack> itemPredicate, ItemContainer[] containerTo
   ) {
      return this.writeAction(() -> {
         List<MoveTransaction<ItemStackTransaction>> transactions = new ObjectArrayList();

         for (short i = 0; i < this.getCapacity(); i++) {
            if (!this.cantRemoveFromSlot(i)) {
               ItemStack checkedItem = this.internal_getSlot(i);
               if (!ItemStack.isEmpty(checkedItem) && (itemPredicate == null || itemPredicate.test(checkedItem))) {
                  transactions.addAll(this.moveItemStackFromSlot(i, containerTo).getList());
               }
            }
         }

         return new ListTransaction<>(true, transactions);
      });
   }

   @Nonnull
   public ListTransaction<MoveTransaction<ItemStackTransaction>> quickStackTo(@Nonnull ItemContainer... containerTo) {
      return this.moveAllItemStacksTo(itemStack -> {
         for (ItemContainer itemContainer : containerTo) {
            if (itemContainer.containsItemStacksStackableWith(itemStack)) {
               return true;
            }
         }

         return false;
      }, containerTo);
   }

   @Nonnull
   public ListTransaction<MoveTransaction<SlotTransaction>> combineItemStacksIntoSlot(@Nonnull ItemContainer containerTo, short slotTo) {
      ListTransaction<MoveTransaction<SlotTransaction>> transaction = this.internal_combineItemStacksIntoSlot(containerTo, slotTo);
      this.sendUpdate(transaction);

      for (MoveTransaction<SlotTransaction> moveSlotTransaction : transaction.getList()) {
         moveSlotTransaction.getOtherContainer().sendUpdate(moveSlotTransaction.toInverted(this));
      }

      return transaction;
   }

   @Nonnull
   protected ListTransaction<MoveTransaction<SlotTransaction>> internal_combineItemStacksIntoSlot(@Nonnull ItemContainer containerTo, short slotTo) {
      validateSlotIndex(slotTo, containerTo.getCapacity());
      return this.writeAction(() -> {
         ItemStack itemStack = containerTo.internal_getSlot(slotTo);
         Item item = itemStack.getItem();
         int maxStack = item.getMaxStack();
         if (!ItemStack.isEmpty(itemStack) && itemStack.getQuantity() < maxStack) {
            int count = 0;
            int[] quantities = new int[this.getCapacity()];
            int[] indexes = new int[this.getCapacity()];

            for (short i = 0; i < this.getCapacity(); i++) {
               if (!this.cantRemoveFromSlot(i)) {
                  ItemStack itemFrom = this.internal_getSlot(i);
                  if (itemStack != itemFrom && !ItemStack.isEmpty(itemFrom) && itemFrom.isStackableWith(itemStack)) {
                     indexes[count] = i;
                     quantities[count] = itemFrom.getQuantity();
                     count++;
                  }
               }
            }

            IntArrays.quickSort(quantities, indexes, 0, count);
            int quantity = itemStack.getQuantity();
            List<MoveTransaction<SlotTransaction>> list = new ObjectArrayList();

            for (int ai = 0; ai < count && quantity < maxStack; ai++) {
               short ix = (short)indexes[ai];
               ItemStack itemFrom = this.internal_getSlot(ix);
               MoveTransaction<SlotTransaction> transaction = this.internal_moveItemStackFromSlot(ix, itemFrom.getQuantity(), containerTo, slotTo, true);
               list.add(transaction);
               quantity = !ItemStack.isEmpty(transaction.getAddTransaction().getSlotAfter()) ? transaction.getAddTransaction().getSlotAfter().getQuantity() : 0;
            }

            return new ListTransaction<>(true, list);
         } else {
            return new ListTransaction<>(false, Collections.emptyList());
         }
      });
   }

   @Nonnull
   public ListTransaction<MoveTransaction<SlotTransaction>> swapItems(short srcPos, @Nonnull ItemContainer containerTo, short destPos, short length) {
      ListTransaction<MoveTransaction<SlotTransaction>> transaction = this.internal_swapItems(srcPos, containerTo, destPos, length);
      this.sendUpdate(transaction);

      for (MoveTransaction<SlotTransaction> moveItemStackTransaction : transaction.getList()) {
         moveItemStackTransaction.getOtherContainer().sendUpdate(moveItemStackTransaction.toInverted(this));
      }

      return transaction;
   }

   @Nonnull
   protected ListTransaction<MoveTransaction<SlotTransaction>> internal_swapItems(short srcPos, @Nonnull ItemContainer containerTo, short destPos, short length) {
      if (srcPos < 0) {
         throw new IndexOutOfBoundsException("srcPos < 0");
      } else if (srcPos + length > this.getCapacity()) {
         throw new IndexOutOfBoundsException("srcPos + length > capacity");
      } else if (destPos < 0) {
         throw new IndexOutOfBoundsException("destPos < 0");
      } else if (destPos + length > containerTo.getCapacity()) {
         throw new IndexOutOfBoundsException("destPos + length > dest.capacity");
      } else {
         return this.writeAction(() -> containerTo.writeAction(() -> {
            List<MoveTransaction<SlotTransaction>> list = new ObjectArrayList(length);

            for (short slot = 0; slot < length; slot++) {
               list.add(this.internal_swapItems(containerTo, (short)(srcPos + slot), (short)(destPos + slot)));
            }

            return new ListTransaction<>(true, list);
         }));
      }
   }

   @Nonnull
   protected MoveTransaction<SlotTransaction> internal_swapItems(@Nonnull ItemContainer containerTo, short slotFrom, short slotTo) {
      ItemStack itemFrom = this.internal_removeSlot(slotFrom);
      ItemStack itemTo = containerTo.internal_removeSlot(slotTo);
      if (itemTo != null && !itemTo.isEmpty()) {
         this.internal_setSlot(slotFrom, itemTo);
      }

      if (itemFrom != null && !itemFrom.isEmpty()) {
         containerTo.internal_setSlot(slotTo, itemFrom);
      }

      SlotTransaction from = new SlotTransaction(true, ActionType.REPLACE, slotFrom, itemFrom, itemTo, null, false, false, false);
      SlotTransaction to = new SlotTransaction(true, ActionType.REPLACE, slotTo, itemTo, itemFrom, null, false, false, false);
      return new MoveTransaction<>(true, from, MoveType.MOVE_FROM_SELF, containerTo, to);
   }

   public boolean canAddItemStack(@Nonnull ItemStack itemStack) {
      return this.canAddItemStack(itemStack, false, true);
   }

   public boolean canAddItemStack(@Nonnull ItemStack itemStack, boolean fullStacks, boolean filter) {
      Item item = itemStack.getItem();
      if (item == null) {
         throw new IllegalArgumentException(itemStack.getItemId() + " is an invalid item!");
      } else {
         int itemMaxStack = item.getMaxStack();
         return this.readAction(() -> {
            int testQuantityRemaining = itemStack.getQuantity();
            if (!fullStacks) {
               testQuantityRemaining = InternalContainerUtilItemStack.testAddToExistingItemStacks(this, itemStack, itemMaxStack, testQuantityRemaining, filter);
            }

            testQuantityRemaining = InternalContainerUtilItemStack.testAddToEmptySlots(this, itemStack, itemMaxStack, testQuantityRemaining, filter);
            return testQuantityRemaining <= 0;
         });
      }
   }

   @Nonnull
   public ItemStackTransaction addItemStack(@Nonnull ItemStack itemStack) {
      return this.addItemStack(itemStack, false, false, true);
   }

   @Nonnull
   public ItemStackTransaction addItemStack(@Nonnull ItemStack itemStack, boolean allOrNothing, boolean fullStacks, boolean filter) {
      ItemStackTransaction transaction = InternalContainerUtilItemStack.internal_addItemStack(this, itemStack, allOrNothing, fullStacks, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   public boolean canAddItemStacks(List<ItemStack> itemStacks) {
      return this.canAddItemStacks(itemStacks, false, true);
   }

   public boolean canAddItemStacks(@Nullable List<ItemStack> itemStacks, boolean fullStacks, boolean filter) {
      if (itemStacks != null && !itemStacks.isEmpty()) {
         List<ItemContainer.TempItemData> tempItemDataList = new ObjectArrayList(itemStacks.size());

         for (ItemStack itemStack : itemStacks) {
            Item item = itemStack.getItem();
            if (item == null) {
               throw new IllegalArgumentException(itemStack.getItemId() + " is an invalid item!");
            }

            tempItemDataList.add(new ItemContainer.TempItemData(itemStack, item));
         }

         return this.readAction(
            () -> {
               for (ItemContainer.TempItemData tempItemData : tempItemDataList) {
                  int itemMaxStack = tempItemData.item().getMaxStack();
                  ItemStack itemStackx = tempItemData.itemStack();
                  int testQuantityRemaining = itemStackx.getQuantity();
                  if (!fullStacks) {
                     testQuantityRemaining = InternalContainerUtilItemStack.testAddToExistingItemStacks(
                        this, itemStackx, itemMaxStack, testQuantityRemaining, filter
                     );
                  }

                  testQuantityRemaining = InternalContainerUtilItemStack.testAddToEmptySlots(this, itemStackx, itemMaxStack, testQuantityRemaining, filter);
                  if (testQuantityRemaining > 0) {
                     return false;
                  }
               }

               return true;
            }
         );
      } else {
         return true;
      }
   }

   public ListTransaction<ItemStackTransaction> addItemStacks(List<ItemStack> itemStacks) {
      return this.addItemStacks(itemStacks, false, false, true);
   }

   public ListTransaction<ItemStackTransaction> addItemStacks(@Nullable List<ItemStack> itemStacks, boolean allOrNothing, boolean fullStacks, boolean filter) {
      if (itemStacks != null && !itemStacks.isEmpty()) {
         ListTransaction<ItemStackTransaction> transaction = InternalContainerUtilItemStack.internal_addItemStacks(
            this, itemStacks, allOrNothing, fullStacks, filter
         );
         this.sendUpdate(transaction);
         return transaction;
      } else {
         return ListTransaction.getEmptyTransaction(true);
      }
   }

   public ListTransaction<ItemStackSlotTransaction> addItemStacksOrdered(List<ItemStack> itemStacks) {
      return this.addItemStacksOrdered(itemStacks, false, true);
   }

   public ListTransaction<ItemStackSlotTransaction> addItemStacksOrdered(short offset, List<ItemStack> itemStacks) {
      return this.addItemStacksOrdered(offset, itemStacks, false, true);
   }

   public ListTransaction<ItemStackSlotTransaction> addItemStacksOrdered(List<ItemStack> itemStacks, boolean allOrNothing, boolean filter) {
      return this.addItemStacksOrdered((short)0, itemStacks, allOrNothing, filter);
   }

   public ListTransaction<ItemStackSlotTransaction> addItemStacksOrdered(
      short offset, @Nullable List<ItemStack> itemStacks, boolean allOrNothing, boolean filter
   ) {
      if (itemStacks != null && !itemStacks.isEmpty()) {
         ListTransaction<ItemStackSlotTransaction> transaction = InternalContainerUtilItemStack.internal_addItemStacksOrdered(
            this, offset, itemStacks, allOrNothing, filter
         );
         this.sendUpdate(transaction);
         return transaction;
      } else {
         return ListTransaction.getEmptyTransaction(true);
      }
   }

   public boolean canRemoveItemStack(ItemStack itemStack) {
      return this.canRemoveItemStack(itemStack, true, true);
   }

   public boolean canRemoveItemStack(@Nullable ItemStack itemStack, boolean exactAmount, boolean filter) {
      return itemStack == null ? true : this.readAction(() -> {
         int testQuantityRemaining = InternalContainerUtilItemStack.testRemoveItemStackFromItems(this, itemStack, itemStack.getQuantity(), filter);
         return testQuantityRemaining > 0 ? false : !exactAmount || testQuantityRemaining >= 0;
      });
   }

   @Nonnull
   public ItemStackTransaction removeItemStack(@Nonnull ItemStack itemStack) {
      return this.removeItemStack(itemStack, true, true);
   }

   @Nonnull
   public ItemStackTransaction removeItemStack(@Nonnull ItemStack itemStack, boolean allOrNothing, boolean filter) {
      ItemStackTransaction transaction = InternalContainerUtilItemStack.internal_removeItemStack(this, itemStack, allOrNothing, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   public boolean canRemoveItemStacks(List<ItemStack> itemStacks) {
      return this.canRemoveItemStacks(itemStacks, true, true);
   }

   public boolean canRemoveItemStacks(@Nullable List<ItemStack> itemStacks, boolean exactAmount, boolean filter) {
      return itemStacks != null && !itemStacks.isEmpty() ? this.readAction(() -> {
         for (ItemStack itemStack : itemStacks) {
            int testQuantityRemaining = InternalContainerUtilItemStack.testRemoveItemStackFromItems(this, itemStack, itemStack.getQuantity(), filter);
            if (testQuantityRemaining > 0) {
               return false;
            }

            if (exactAmount && testQuantityRemaining < 0) {
               return false;
            }
         }

         return true;
      }) : true;
   }

   public ListTransaction<ItemStackTransaction> removeItemStacks(List<ItemStack> itemStacks) {
      return this.removeItemStacks(itemStacks, true, true);
   }

   public ListTransaction<ItemStackTransaction> removeItemStacks(@Nullable List<ItemStack> itemStacks, boolean allOrNothing, boolean filter) {
      if (itemStacks != null && !itemStacks.isEmpty()) {
         ListTransaction<ItemStackTransaction> transaction = InternalContainerUtilItemStack.internal_removeItemStacks(this, itemStacks, allOrNothing, filter);
         this.sendUpdate(transaction);
         return transaction;
      } else {
         return ListTransaction.getEmptyTransaction(true);
      }
   }

   public boolean canRemoveTag(int tagIndex, int quantity) {
      return this.canRemoveTag(tagIndex, quantity, true, true);
   }

   public boolean canRemoveTag(int tagIndex, int quantity, boolean exactAmount, boolean filter) {
      return this.readAction(() -> {
         int testQuantityRemaining = InternalContainerUtilTag.testRemoveTagFromItems(this, tagIndex, quantity, filter);
         return testQuantityRemaining > 0 ? false : !exactAmount || testQuantityRemaining >= 0;
      });
   }

   @Nonnull
   public TagTransaction removeTag(int tagIndex, int quantity) {
      return this.removeTag(tagIndex, quantity, true, true, true);
   }

   @Nonnull
   public TagTransaction removeTag(int tagIndex, int quantity, boolean allOrNothing, boolean exactAmount, boolean filter) {
      TagTransaction transaction = InternalContainerUtilTag.internal_removeTag(this, tagIndex, quantity, allOrNothing, exactAmount, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   public boolean canRemoveResource(ResourceQuantity resource) {
      return this.canRemoveResource(resource, true, true);
   }

   public boolean canRemoveResource(@Nullable ResourceQuantity resource, boolean exactAmount, boolean filter) {
      return resource == null ? true : this.readAction(() -> {
         int testQuantityRemaining = InternalContainerUtilResource.testRemoveResourceFromItems(this, resource, resource.getQuantity(), filter);
         return testQuantityRemaining > 0 ? false : !exactAmount || testQuantityRemaining >= 0;
      });
   }

   @Nonnull
   public ResourceTransaction removeResource(@Nonnull ResourceQuantity resource) {
      return this.removeResource(resource, true, true, true);
   }

   @Nonnull
   public ResourceTransaction removeResource(@Nonnull ResourceQuantity resource, boolean allOrNothing, boolean exactAmount, boolean filter) {
      ResourceTransaction transaction = InternalContainerUtilResource.internal_removeResource(this, resource, allOrNothing, exactAmount, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   public boolean canRemoveResources(List<ResourceQuantity> resources) {
      return this.canRemoveResources(resources, true, true);
   }

   public boolean canRemoveResources(@Nullable List<ResourceQuantity> resources, boolean exactAmount, boolean filter) {
      return resources != null && !resources.isEmpty() ? this.readAction(() -> {
         for (ResourceQuantity resource : resources) {
            int testQuantityRemaining = InternalContainerUtilResource.testRemoveResourceFromItems(this, resource, resource.getQuantity(), filter);
            if (testQuantityRemaining > 0) {
               return false;
            }

            if (exactAmount && testQuantityRemaining < 0) {
               return false;
            }
         }

         return true;
      }) : true;
   }

   public ListTransaction<ResourceTransaction> removeResources(List<ResourceQuantity> resources) {
      return this.removeResources(resources, true, true, true);
   }

   public ListTransaction<ResourceTransaction> removeResources(
      @Nullable List<ResourceQuantity> resources, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      if (resources != null && !resources.isEmpty()) {
         ListTransaction<ResourceTransaction> transaction = InternalContainerUtilResource.internal_removeResources(
            this, resources, allOrNothing, exactAmount, filter
         );
         this.sendUpdate(transaction);
         return transaction;
      } else {
         return ListTransaction.getEmptyTransaction(true);
      }
   }

   public boolean canRemoveMaterial(MaterialQuantity material) {
      return this.canRemoveMaterial(material, true, true);
   }

   public boolean canRemoveMaterial(@Nullable MaterialQuantity material, boolean exactAmount, boolean filter) {
      return material == null ? true : this.readAction(() -> {
         int testQuantityRemaining = InternalContainerUtilMaterial.testRemoveMaterialFromItems(this, material, material.getQuantity(), filter);
         return testQuantityRemaining > 0 ? false : !exactAmount || testQuantityRemaining >= 0;
      });
   }

   @Nonnull
   public MaterialTransaction removeMaterial(@Nonnull MaterialQuantity material) {
      return this.removeMaterial(material, true, true, true);
   }

   @Nonnull
   public MaterialTransaction removeMaterial(@Nonnull MaterialQuantity material, boolean allOrNothing, boolean exactAmount, boolean filter) {
      MaterialTransaction transaction = InternalContainerUtilMaterial.internal_removeMaterial(this, material, allOrNothing, exactAmount, filter);
      this.sendUpdate(transaction);
      return transaction;
   }

   public boolean canRemoveMaterials(List<MaterialQuantity> materials) {
      return this.canRemoveMaterials(materials, true, true);
   }

   public boolean canRemoveMaterials(@Nullable List<MaterialQuantity> materials, boolean exactAmount, boolean filter) {
      return materials != null && !materials.isEmpty() ? this.readAction(() -> {
         for (MaterialQuantity material : materials) {
            int testQuantityRemaining = InternalContainerUtilMaterial.testRemoveMaterialFromItems(this, material, material.getQuantity(), filter);
            if (testQuantityRemaining > 0) {
               return false;
            }

            if (exactAmount && testQuantityRemaining < 0) {
               return false;
            }
         }

         return true;
      }) : true;
   }

   public List<TestRemoveItemSlotResult> getSlotMaterialsToRemove(@Nullable List<MaterialQuantity> materials, boolean exactAmount, boolean filter) {
      List<TestRemoveItemSlotResult> slotMaterials = new ObjectArrayList();
      return materials != null && !materials.isEmpty() ? this.readAction(() -> {
         for (MaterialQuantity material : materials) {
            TestRemoveItemSlotResult testResult = InternalContainerUtilMaterial.getTestRemoveMaterialFromItems(this, material, material.getQuantity(), filter);
            if (testResult.quantityRemaining > 0) {
               slotMaterials.clear();
               return slotMaterials;
            }

            if (exactAmount && testResult.quantityRemaining < 0) {
               slotMaterials.clear();
               return slotMaterials;
            }

            slotMaterials.add(testResult);
         }

         return slotMaterials;
      }) : slotMaterials;
   }

   public ListTransaction<MaterialTransaction> removeMaterials(List<MaterialQuantity> materials) {
      return this.removeMaterials(materials, true, true, true);
   }

   public ListTransaction<MaterialTransaction> removeMaterials(
      @Nullable List<MaterialQuantity> materials, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      if (materials != null && !materials.isEmpty()) {
         ListTransaction<MaterialTransaction> transaction = InternalContainerUtilMaterial.internal_removeMaterials(
            this, materials, allOrNothing, exactAmount, filter
         );
         this.sendUpdate(transaction);
         return transaction;
      } else {
         return ListTransaction.getEmptyTransaction(true);
      }
   }

   public ListTransaction<MaterialSlotTransaction> removeMaterialsOrdered(short offset, List<MaterialQuantity> materials) {
      return this.removeMaterialsOrdered(offset, materials, true, true, true);
   }

   public ListTransaction<MaterialSlotTransaction> removeMaterialsOrdered(
      List<MaterialQuantity> materials, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return this.removeMaterialsOrdered((short)0, materials, allOrNothing, exactAmount, filter);
   }

   public ListTransaction<MaterialSlotTransaction> removeMaterialsOrdered(
      short offset, @Nullable List<MaterialQuantity> materials, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      if (materials != null && !materials.isEmpty()) {
         if (offset + materials.size() > this.getCapacity()) {
            return ListTransaction.getEmptyTransaction(false);
         } else {
            ListTransaction<MaterialSlotTransaction> transaction = InternalContainerUtilMaterial.internal_removeMaterialsOrdered(
               this, offset, materials, allOrNothing, exactAmount, filter
            );
            this.sendUpdate(transaction);
            return transaction;
         }
      } else {
         return ListTransaction.getEmptyTransaction(true);
      }
   }

   public boolean isEmpty() {
      return this.readAction(() -> {
         for (short i = 0; i < this.getCapacity(); i++) {
            ItemStack itemStack = this.internal_getSlot(i);
            if (itemStack != null && !itemStack.isEmpty()) {
               return false;
            }
         }

         return true;
      });
   }

   public int countItemStacks(@Nonnull Predicate<ItemStack> itemPredicate) {
      return this.readAction(() -> {
         int count = 0;

         for (short i = 0; i < this.getCapacity(); i++) {
            ItemStack itemStack = this.internal_getSlot(i);
            if (!ItemStack.isEmpty(itemStack) && itemPredicate.test(itemStack)) {
               count += itemStack.getQuantity();
            }
         }

         return count;
      });
   }

   public boolean containsItemStacksStackableWith(@Nonnull ItemStack itemStack) {
      return this.readAction(() -> {
         for (short i = 0; i < this.getCapacity(); i++) {
            ItemStack checked = this.internal_getSlot(i);
            if (!ItemStack.isEmpty(checked) && itemStack.isStackableWith(checked)) {
               return true;
            }
         }

         return false;
      });
   }

   public void forEach(@Nonnull ShortObjectConsumer<ItemStack> action) {
      for (short i = 0; i < this.getCapacity(); i++) {
         ItemStack itemStack = this.getItemStack(i);
         if (!ItemStack.isEmpty(itemStack)) {
            action.accept(i, itemStack);
         }
      }
   }

   public <T> void forEachWithMeta(@Nonnull Short2ObjectConcurrentHashMap.ShortBiObjConsumer<ItemStack, T> consumer, T meta) {
      for (short i = 0; i < this.getCapacity(); i++) {
         ItemStack itemStack = this.getItemStack(i);
         if (!ItemStack.isEmpty(itemStack)) {
            consumer.accept(i, itemStack, meta);
         }
      }
   }

   @Nonnull
   public List<ItemStack> removeAllItemStacks() {
      List<ItemStack> items = new ObjectArrayList();
      ListTransaction<SlotTransaction> transaction = this.writeAction(() -> {
         List<SlotTransaction> transactions = new ObjectArrayList();

         for (short i = 0; i < this.getCapacity(); i++) {
            if (!this.cantRemoveFromSlot(i)) {
               ItemStack itemStack = this.internal_removeSlot(i);
               if (!ItemStack.isEmpty(itemStack)) {
                  items.add(itemStack);
                  transactions.add(new SlotTransaction(true, ActionType.REMOVE, i, itemStack, null, itemStack, false, false, true));
               }
            }
         }

         return new ListTransaction<>(true, transactions);
      });
      this.sendUpdate(transaction);
      return items;
   }

   @Nonnull
   public List<ItemStack> dropAllItemStacks() {
      return this.dropAllItemStacks(true);
   }

   @Nonnull
   public List<ItemStack> dropAllItemStacks(boolean filter) {
      List<ItemStack> items = new ObjectArrayList();
      ListTransaction<SlotTransaction> transaction = this.writeAction(() -> {
         List<SlotTransaction> transactions = new ObjectArrayList();

         for (short i = 0; i < this.getCapacity(); i++) {
            if (!filter || !this.cantDropFromSlot(i)) {
               ItemStack itemStack = this.internal_removeSlot(i);
               if (!ItemStack.isEmpty(itemStack)) {
                  items.add(itemStack);
                  transactions.add(new SlotTransaction(true, ActionType.REMOVE, i, itemStack, null, itemStack, false, false, true));
               }
            }
         }

         return new ListTransaction<>(true, transactions);
      });
      this.sendUpdate(transaction);
      return items;
   }

   @Nonnull
   public ListTransaction<SlotTransaction> sortItems(@Nonnull SortType sort) {
      ListTransaction<SlotTransaction> transaction = this.internal_sortItems(sort);
      this.sendUpdate(transaction);
      return transaction;
   }

   protected ListTransaction<SlotTransaction> internal_sortItems(@Nonnull SortType sort) {
      return this.writeAction(() -> {
         ItemStack[] stacks = new ItemStack[this.getCapacity()];
         int stackOffset = 0;

         for (short i = 0; i < stacks.length; i++) {
            if (!this.cantRemoveFromSlot(i)) {
               ItemStack slot = this.internal_getSlot(i);
               if (slot != null) {
                  Item item = slot.getItem();
                  int maxStack = item.getMaxStack();
                  int slotQuantity = slot.getQuantity();
                  if (maxStack > 1) {
                     for (int j = 0; j < stackOffset && slotQuantity > 0; j++) {
                        ItemStack stack = stacks[j];
                        if (slot.isStackableWith(stack)) {
                           int stackQuantity = stack.getQuantity();
                           if (stackQuantity < maxStack) {
                              int adjust = Math.min(slotQuantity, maxStack - stackQuantity);
                              slotQuantity -= adjust;
                              stacks[j] = stack.withQuantity(stackQuantity + adjust);
                           }
                        }
                     }
                  }

                  if (slotQuantity > 0) {
                     stacks[stackOffset++] = slotQuantity != slot.getQuantity() ? slot.withQuantity(slotQuantity) : slot;
                  }
               }
            }
         }

         Arrays.sort(stacks, sort.getComparator());
         List<SlotTransaction> transactions = new ObjectArrayList(stacks.length);
         stackOffset = 0;

         for (short ix = 0; ix < stacks.length; ix++) {
            if (!this.cantRemoveFromSlot(ix)) {
               ItemStack existing = this.internal_getSlot(ix);
               ItemStack replacement = stacks[stackOffset];
               if (!this.cantAddToSlot(ix, replacement, existing)) {
                  stackOffset++;
                  if (existing != replacement) {
                     this.internal_setSlot(ix, replacement);
                     transactions.add(new SlotTransaction(true, ActionType.REMOVE, ix, existing, null, replacement, false, false, true));
                  }
               }
            }
         }

         for (int ixx = stackOffset; ixx < stacks.length; ixx++) {
            if (stacks[ixx] != null) {
               throw new IllegalStateException("Had leftover stacks that didn't get sorted!");
            }
         }

         return new ListTransaction<>(true, transactions);
      });
   }

   protected void sendUpdate(@Nonnull Transaction transaction) {
      if (transaction.succeeded()) {
         ItemContainer.ItemContainerChangeEvent event = new ItemContainer.ItemContainerChangeEvent(this, transaction);
         this.externalChangeEventRegistry.dispatchFor(null).dispatch(event);
         this.internalChangeEventRegistry.dispatchFor(null).dispatch(event);
      }
   }

   public boolean containsContainer(ItemContainer itemContainer) {
      return itemContainer == this;
   }

   public void doMigration(Function<String, String> blockMigration) {
      this.writeAction(_blockMigration -> {
         for (short i = 0; i < this.getCapacity(); i++) {
            ItemStack slot = this.internal_getSlot(i);
            if (!ItemStack.isEmpty(slot)) {
               String oldItemId = slot.getItemId();
               String newItemId = (String)_blockMigration.apply(slot.getItemId());
               if (!oldItemId.equals(newItemId)) {
                  this.internal_setSlot(i, new ItemStack(newItemId, slot.getQuantity(), slot.getMetadata()));
               }
            }
         }

         return null;
      }, blockMigration);
   }

   @Nullable
   public static ItemResourceType getMatchingResourceType(@Nonnull Item item, @Nonnull String resourceId) {
      ItemResourceType[] resourceTypes = item.getResourceTypes();
      if (resourceTypes == null) {
         return null;
      } else {
         for (ItemResourceType resourceType : resourceTypes) {
            if (resourceId.equals(resourceType.id)) {
               return resourceType;
            }
         }

         return null;
      }
   }

   public static void validateQuantity(int quantity) {
      if (quantity < 0) {
         throw new IllegalArgumentException("Quantity is less than zero! " + quantity + " < 0");
      }
   }

   public static void validateSlotIndex(short slot, int capacity) {
      if (slot < 0) {
         throw new IllegalArgumentException("Slot is less than zero! " + slot + " < 0");
      } else if (slot >= capacity) {
         throw new IllegalArgumentException("Slot is outside capacity! " + slot + " >= " + capacity);
      }
   }

   @Nonnull
   public static <T extends ItemContainer> T copy(@Nonnull ItemContainer from, @Nonnull T to, @Nullable List<ItemStack> remainder) {
      from.forEach((slot, itemStack) -> {
         if (slot >= to.getCapacity()) {
            if (remainder != null) {
               remainder.add(itemStack);
            }
         } else if (!ItemStack.isEmpty(itemStack)) {
            to.setItemStackForSlot(slot, itemStack);
         }
      });
      return to;
   }

   public static <T extends ItemContainer> T ensureContainerCapacity(
      @Nullable T inputContainer, short capacity, @Nonnull Short2ObjectConcurrentHashMap.ShortFunction<T> newContainerSupplier, List<ItemStack> remainder
   ) {
      if (inputContainer == null) {
         return newContainerSupplier.apply(capacity);
      } else {
         return inputContainer.getCapacity() == capacity ? inputContainer : copy(inputContainer, newContainerSupplier.apply(capacity), remainder);
      }
   }

   public static ItemContainer getNewContainer(short capacity, @Nonnull Short2ObjectConcurrentHashMap.ShortFunction<ItemContainer> supplier) {
      return (ItemContainer)(capacity > 0 ? supplier.apply(capacity) : EmptyItemContainer.INSTANCE);
   }

   public record ItemContainerChangeEvent(ItemContainer container, Transaction transaction) implements IEvent<Void> {
      @Nonnull
      @Override
      public String toString() {
         return "ItemContainerChangeEvent{container=" + this.container + ", transaction=" + this.transaction + "}";
      }
   }

   public record TempItemData(ItemStack itemStack, Item item) {
   }
}
