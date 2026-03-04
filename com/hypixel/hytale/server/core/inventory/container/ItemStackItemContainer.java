package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemStackContainerConfig;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.container.filter.TagFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class ItemStackItemContainer extends ItemContainer {
   @Nonnull
   public static KeyedCodec<BsonDocument> CONTAINER_CODEC = new KeyedCodec<>("Container", Codec.BSON_DOCUMENT);
   @Nonnull
   public static KeyedCodec<Short> CAPACITY_CODEC = new KeyedCodec<>("Capacity", Codec.SHORT);
   @Nonnull
   public static KeyedCodec<ItemStack[]> ITEMS_CODEC = new KeyedCodec<>("Items", new ArrayCodec<>(ItemStack.CODEC, ItemStack[]::new));
   protected final ReadWriteLock lock = new ReentrantReadWriteLock();
   protected final ItemContainer parentContainer;
   protected final short itemStackSlot;
   protected final ItemStack originalItemStack;
   protected final short capacity;
   protected ItemStack[] items;
   private final Map<FilterActionType, Int2ObjectConcurrentHashMap<SlotFilter>> slotFilters = new ConcurrentHashMap<>();
   @Nonnull
   private FilterType globalFilter = FilterType.ALLOW_ALL;

   private ItemStackItemContainer(ItemContainer parentContainer, short itemStackSlot, ItemStack originalItemStack, short capacity, ItemStack[] items) {
      this.parentContainer = parentContainer;
      this.itemStackSlot = itemStackSlot;
      this.originalItemStack = originalItemStack;
      this.capacity = capacity;
      this.items = items;
   }

   public ItemContainer getParentContainer() {
      return this.parentContainer;
   }

   public short getItemStackSlot() {
      return this.itemStackSlot;
   }

   public ItemStack getOriginalItemStack() {
      return this.originalItemStack;
   }

   public boolean isItemStackValid() {
      ItemStack itemStack = this.parentContainer.getItemStack(this.itemStackSlot);
      return ItemStack.isEmpty(itemStack) ? false : ItemStack.isSameItemType(itemStack, this.originalItemStack);
   }

   @Override
   public short getCapacity() {
      return this.capacity;
   }

   @Override
   public void setGlobalFilter(@Nonnull FilterType globalFilter) {
      this.globalFilter = globalFilter;
   }

   @Override
   public void setSlotFilter(FilterActionType actionType, short slot, @Nullable SlotFilter filter) {
      validateSlotIndex(slot, this.getCapacity());
      if (filter != null) {
         this.slotFilters.computeIfAbsent(actionType, k -> new Int2ObjectConcurrentHashMap<>()).put(slot, filter);
      } else {
         this.slotFilters.computeIfPresent(actionType, (k, map) -> {
            map.remove(slot);
            return map.isEmpty() ? null : map;
         });
      }
   }

   @Override
   public ItemContainer clone() {
      throw new UnsupportedOperationException("Item stack containers don't support clone");
   }

   @Override
   protected <V> V readAction(@Nonnull Supplier<V> action) {
      this.lock.readLock().lock();

      Object var2;
      try {
         var2 = action.get();
      } finally {
         this.lock.readLock().unlock();
      }

      return (V)var2;
   }

   @Override
   protected <X, V> V readAction(@Nonnull Function<X, V> action, X x) {
      this.lock.readLock().lock();

      Object var3;
      try {
         var3 = action.apply(x);
      } finally {
         this.lock.readLock().unlock();
      }

      return (V)var3;
   }

   @Override
   protected <V> V writeAction(@Nonnull Supplier<V> action) {
      this.lock.writeLock().lock();

      Object var2;
      try {
         var2 = action.get();
      } finally {
         this.lock.writeLock().unlock();
      }

      return (V)var2;
   }

   @Override
   protected <X, V> V writeAction(@Nonnull Function<X, V> action, X x) {
      this.lock.writeLock().lock();

      Object var3;
      try {
         var3 = action.apply(x);
      } finally {
         this.lock.writeLock().unlock();
      }

      return (V)var3;
   }

   @Override
   public boolean isEmpty() {
      this.lock.readLock().lock();

      try {
         if (this.items == null) {
            return true;
         } else {
            for (short i = 0; i < this.items.length; i++) {
               if (!ItemStack.isEmpty(this.items[i])) {
                  return false;
               }
            }

            return false;
         }
      } finally {
         this.lock.readLock().unlock();
      }
   }

   @Nonnull
   @Override
   protected ClearTransaction internal_clear() {
      if (this.items == null) {
         return new ClearTransaction(true, (short)0, ItemStack.EMPTY_ARRAY);
      } else {
         ItemStack[] oldItems = this.items;
         this.items = new ItemStack[oldItems.length];
         writeToItemStack(this.parentContainer, this.itemStackSlot, this.originalItemStack, this.items);
         return new ClearTransaction(true, (short)0, oldItems);
      }
   }

   @Nullable
   @Override
   protected ItemStack internal_getSlot(short slot) {
      return this.items != null ? this.items[slot] : null;
   }

   @Nullable
   @Override
   protected ItemStack internal_setSlot(short slot, ItemStack itemStack) {
      if (this.items == null) {
         return null;
      } else if (ItemStack.isEmpty(itemStack)) {
         return this.internal_removeSlot(slot);
      } else {
         ItemStack old = this.items[slot];
         this.items[slot] = itemStack;
         writeToItemStack(this.parentContainer, this.itemStackSlot, this.originalItemStack, this.items);
         return old;
      }
   }

   @Nullable
   @Override
   protected ItemStack internal_removeSlot(short slot) {
      if (this.items == null) {
         return null;
      } else {
         ItemStack old = this.items[slot];
         this.items[slot] = null;
         writeToItemStack(this.parentContainer, this.itemStackSlot, this.originalItemStack, this.items);
         return old;
      }
   }

   @Override
   protected boolean cantAddToSlot(short slot, ItemStack itemStack, ItemStack slotItemStack) {
      return !this.globalFilter.allowInput() ? true : this.testFilter(FilterActionType.ADD, slot, itemStack);
   }

   @Override
   protected boolean cantRemoveFromSlot(short slot) {
      return !this.globalFilter.allowOutput() ? true : this.testFilter(FilterActionType.REMOVE, slot, null);
   }

   @Override
   protected boolean cantDropFromSlot(short slot) {
      return this.testFilter(FilterActionType.DROP, slot, null);
   }

   @Override
   protected boolean cantMoveToSlot(ItemContainer fromContainer, short slotFrom) {
      return fromContainer == this.parentContainer && slotFrom == this.itemStackSlot;
   }

   private boolean testFilter(FilterActionType actionType, short slot, ItemStack itemStack) {
      Int2ObjectConcurrentHashMap<SlotFilter> map = this.slotFilters.get(actionType);
      if (map == null) {
         return false;
      } else {
         SlotFilter filter = map.get(slot);
         return filter == null ? false : !filter.test(actionType, this, slot, itemStack);
      }
   }

   @Nullable
   @Override
   public ItemStack getItemStack(short slot) {
      validateSlotIndex(slot, this.getCapacity());
      this.lock.readLock().lock();

      ItemStack var2;
      try {
         var2 = this.internal_getSlot(slot);
      } finally {
         this.lock.readLock().unlock();
      }

      return var2;
   }

   public static void writeToItemStack(@Nonnull ItemContainer itemContainer, short slot, ItemStack originalItemStack, ItemStack[] items) {
      if (ItemStack.isEmpty(originalItemStack)) {
         throw new IllegalStateException("Item stack container is empty");
      } else {
         ItemStack itemStack = itemContainer.getItemStack(slot);
         if (!ItemStack.isSameItemType(itemStack, originalItemStack)) {
            throw new IllegalStateException("Item stack in parent container changed!");
         } else {
            BsonDocument newMetadata = itemStack.getMetadata();
            BsonDocument containerDocument = CONTAINER_CODEC.getOrNull(newMetadata, EmptyExtraInfo.EMPTY);
            if (containerDocument == null) {
               throw new IllegalStateException("Item stack container is empty!");
            } else {
               ITEMS_CODEC.put(containerDocument, items, EmptyExtraInfo.EMPTY);
               itemContainer.setItemStackForSlot(slot, itemStack.withMetadata(newMetadata));
            }
         }
      }
   }

   @Nullable
   public static ItemStackItemContainer getContainer(@Nonnull ItemContainer itemContainer, short slot) {
      ItemStack itemStack = itemContainer.getItemStack(slot);
      if (ItemStack.isEmpty(itemStack)) {
         return null;
      } else {
         BsonDocument containerDocument = itemStack.getFromMetadataOrNull(CONTAINER_CODEC);
         if (containerDocument == null) {
            return null;
         } else {
            Short capacity = CAPACITY_CODEC.getOrNull(containerDocument, EmptyExtraInfo.EMPTY);
            if (capacity != null && capacity > 0) {
               ItemStack[] items = ITEMS_CODEC.getOrNull(containerDocument, EmptyExtraInfo.EMPTY);
               if (items == null) {
                  items = new ItemStack[capacity];
               }

               return new ItemStackItemContainer(itemContainer, slot, itemStack, capacity, items);
            } else {
               return null;
            }
         }
      }
   }

   @Nonnull
   public static ItemStackItemContainer makeContainerWithCapacity(@Nonnull ItemContainer itemContainer, short slot, short capacity) {
      if (capacity <= 0) {
         throw new IllegalArgumentException("Capacity must be > 0");
      } else {
         ItemStack itemStack = itemContainer.getItemStack(slot);
         if (ItemStack.isEmpty(itemStack)) {
            throw new IllegalArgumentException("Item stack is empty!");
         } else {
            ItemStackItemContainer itemStackItemContainer = getContainer(itemContainer, slot);
            if (itemStackItemContainer != null && itemStackItemContainer.getCapacity() != 0) {
               throw new IllegalStateException("Item stack already has a container!");
            } else {
               BsonDocument newMetadata = itemStack.getMetadata();
               if (newMetadata == null) {
                  newMetadata = new BsonDocument();
               }

               BsonDocument containerDocument = CONTAINER_CODEC.getOrNull(newMetadata, EmptyExtraInfo.EMPTY);
               if (containerDocument == null) {
                  containerDocument = new BsonDocument();
                  CONTAINER_CODEC.put(newMetadata, containerDocument, EmptyExtraInfo.EMPTY);
               }

               CAPACITY_CODEC.put(containerDocument, capacity, EmptyExtraInfo.EMPTY);
               itemContainer.setItemStackForSlot(slot, itemStack.withMetadata(newMetadata));
               return new ItemStackItemContainer(itemContainer, slot, itemStack, capacity, new ItemStack[capacity]);
            }
         }
      }
   }

   @Nullable
   public static ItemStackItemContainer ensureContainer(@Nonnull ItemContainer itemContainer, short slot, short capacity) {
      if (capacity <= 0) {
         throw new IllegalArgumentException("Capacity must be > 0");
      } else {
         ItemStack itemStack = itemContainer.getItemStack(slot);
         if (ItemStack.isEmpty(itemStack)) {
            return null;
         } else {
            ItemStackItemContainer itemStackItemContainer = getContainer(itemContainer, slot);
            if (itemStackItemContainer != null && itemStackItemContainer.getCapacity() != 0) {
               return itemStackItemContainer;
            } else {
               BsonDocument newMetadata = itemStack.getMetadata();
               if (newMetadata == null) {
                  newMetadata = new BsonDocument();
               }

               BsonDocument containerDocument = CONTAINER_CODEC.getOrNull(newMetadata, EmptyExtraInfo.EMPTY);
               if (containerDocument == null) {
                  containerDocument = new BsonDocument();
                  CONTAINER_CODEC.put(newMetadata, containerDocument, EmptyExtraInfo.EMPTY);
               }

               CAPACITY_CODEC.put(containerDocument, capacity, EmptyExtraInfo.EMPTY);
               itemContainer.setItemStackForSlot(slot, itemStack.withMetadata(newMetadata));
               return new ItemStackItemContainer(itemContainer, slot, itemStack, capacity, new ItemStack[capacity]);
            }
         }
      }
   }

   @Nullable
   public static ItemStackItemContainer ensureConfiguredContainer(@Nonnull ItemContainer itemContainer, short slot, @Nonnull ItemStackContainerConfig config) {
      ItemStackItemContainer itemStackItemContainer = ensureContainer(itemContainer, slot, config.getCapacity());
      if (itemStackItemContainer == null) {
         return null;
      } else {
         itemStackItemContainer.setGlobalFilter(config.getGlobalFilter());
         int tagIndex = config.getTagIndex();
         if (tagIndex != Integer.MIN_VALUE) {
            for (short i = 0; i < itemStackItemContainer.getCapacity(); i++) {
               itemStackItemContainer.setSlotFilter(FilterActionType.ADD, i, new TagFilter(tagIndex));
            }
         }

         return itemStackItemContainer;
      }
   }
}
