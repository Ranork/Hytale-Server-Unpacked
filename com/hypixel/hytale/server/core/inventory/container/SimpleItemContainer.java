package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Short2ObjectMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap.Entry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SimpleItemContainer extends ItemContainer {
   public static final BuilderCodec<SimpleItemContainer> CODEC = BuilderCodec.builder(SimpleItemContainer.class, SimpleItemContainer::new)
      .append(new KeyedCodec<>("Capacity", Codec.SHORT), (o, i) -> o.capacity = i, o -> o.capacity)
      .addValidator(Validators.greaterThanOrEqual((short)0))
      .add()
      .append(new KeyedCodec<>("Items", new Short2ObjectMapCodec<>(ItemStack.CODEC, Short2ObjectOpenHashMap::new, false)), (o, i) -> {
         o.items = new ItemStack[o.capacity];
         int count = 0;
         Iterator i$ = i.short2ObjectEntrySet().iterator();

         while (i$.hasNext()) {
            Entry<ItemStack> entry = (Entry<ItemStack>)i$.next();
            short slot = entry.getShortKey();
            ItemStack stack = (ItemStack)entry.getValue();
            if (slot >= 0 && slot < o.capacity && !ItemStack.isEmpty(stack)) {
               o.items[slot] = stack;
               count++;
            }
         }

         o.itemsCount = count;
      }, o -> {
         Short2ObjectOpenHashMap<ItemStack> map = new Short2ObjectOpenHashMap();

         for (short slot = 0; slot < o.capacity; slot++) {
            ItemStack stack = o.items[slot];
            if (stack != null && !ItemStack.isEmpty(stack)) {
               map.put(slot, stack);
            }
         }

         return map;
      })
      .add()
      .afterDecode(i -> {
         if (i.items == null) {
            i.items = new ItemStack[i.capacity];
            i.itemsCount = 0;
         }
      })
      .build();
   protected short capacity;
   protected final ReadWriteLock lock = new ReentrantReadWriteLock();
   protected ItemStack[] items;
   protected int itemsCount;
   private final Map<FilterActionType, Int2ObjectConcurrentHashMap<SlotFilter>> slotFilters = new ConcurrentHashMap<>();
   private FilterType globalFilter = FilterType.ALLOW_ALL;

   protected SimpleItemContainer() {
   }

   public SimpleItemContainer(short capacity) {
      if (capacity <= 0) {
         throw new IllegalArgumentException("Capacity is less than or equal zero! " + capacity + " <= 0");
      } else {
         this.capacity = capacity;
         this.items = new ItemStack[capacity];
      }
   }

   public SimpleItemContainer(@Nonnull SimpleItemContainer other) {
      this.capacity = other.capacity;
      other.lock.readLock().lock();

      try {
         this.items = new ItemStack[other.capacity];
         System.arraycopy(other.items, 0, this.items, 0, other.capacity);
         this.itemsCount = other.itemsCount;
      } finally {
         other.lock.readLock().unlock();
      }

      this.slotFilters.putAll(other.slotFilters);
      this.globalFilter = other.globalFilter;
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
   protected void lockForRead() {
      this.lock.readLock().lock();
   }

   @Override
   protected void unlockForRead() {
      this.lock.readLock().unlock();
   }

   @Override
   protected void lockForWrite() {
      this.lock.writeLock().lock();
   }

   @Override
   protected void unlockForWrite() {
      this.lock.writeLock().unlock();
   }

   @Override
   protected ItemStack internal_getSlot(short slot) {
      return this.items[slot];
   }

   @Override
   protected ItemStack internal_setSlot(short slot, ItemStack itemStack) {
      if (ItemStack.isEmpty(itemStack)) {
         return this.internal_removeSlot(slot);
      } else {
         ItemStack previous = this.items[slot];
         this.items[slot] = itemStack;
         if (previous == null) {
            this.itemsCount++;
         }

         return previous;
      }
   }

   @Override
   protected ItemStack internal_removeSlot(short slot) {
      ItemStack previous = this.items[slot];
      this.items[slot] = null;
      if (previous != null) {
         this.itemsCount--;
      }

      return previous;
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
      return false;
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

   @Override
   public short getCapacity() {
      return this.capacity;
   }

   @Nonnull
   @Override
   protected ClearTransaction internal_clear() {
      ItemStack[] itemStacks = new ItemStack[this.getCapacity()];

      for (short i = 0; i < itemStacks.length; i++) {
         itemStacks[i] = this.items[i];
         this.items[i] = null;
      }

      this.itemsCount = 0;
      return new ClearTransaction(true, (short)0, itemStacks);
   }

   @Nonnull
   public SimpleItemContainer clone() {
      return new SimpleItemContainer(this);
   }

   @Override
   public boolean isEmpty() {
      this.lock.readLock().lock();

      try {
         if (this.itemsCount == 0) {
            return true;
         }
      } finally {
         this.lock.readLock().unlock();
      }

      return super.isEmpty();
   }

   @Override
   public void setGlobalFilter(@Nonnull FilterType globalFilter) {
      this.globalFilter = Objects.requireNonNull(globalFilter);
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

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof SimpleItemContainer that)) {
         return false;
      } else if (this.capacity != that.capacity) {
         return false;
      } else {
         this.lock.readLock().lock();

         try {
            if (this.itemsCount != that.itemsCount) {
               return false;
            } else {
               for (int itr = 0; itr < this.capacity; itr++) {
                  if (!Objects.equals(this.items[itr], that.items[itr])) {
                     return false;
                  }
               }

               return true;
            }
         } finally {
            this.lock.readLock().unlock();
         }
      }
   }

   @Override
   public int hashCode() {
      int result = this.capacity;
      this.lock.readLock().lock();

      try {
         for (int i = 0; i < this.capacity; i++) {
            ItemStack item = this.items[i];
            result = 31 * result + (item != null ? item.hashCode() : 0);
         }
      } finally {
         this.lock.readLock().unlock();
      }

      return result;
   }

   public static ItemContainer getNewContainer(short capacity) {
      return ItemContainer.getNewContainer(capacity, SimpleItemContainer::new);
   }

   public static boolean addOrDropItemStack(
      @Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull ItemContainer itemContainer, @Nonnull ItemStack itemStack
   ) {
      ItemStackTransaction transaction = itemContainer.addItemStack(itemStack);
      ItemStack remainder = transaction.getRemainder();
      if (!ItemStack.isEmpty(remainder)) {
         ItemUtils.dropItem(ref, remainder, store);
         return true;
      } else {
         return false;
      }
   }

   public static boolean addOrDropItemStack(
      @Nonnull ComponentAccessor<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ItemContainer itemContainer,
      short slot,
      @Nonnull ItemStack itemStack
   ) {
      ItemStackSlotTransaction transaction = itemContainer.addItemStackToSlot(slot, itemStack);
      ItemStack remainder = transaction.getRemainder();
      return !ItemStack.isEmpty(remainder) ? addOrDropItemStack(store, ref, itemContainer, itemStack) : false;
   }

   public static boolean addOrDropItemStacks(
      @Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull ItemContainer itemContainer, List<ItemStack> itemStacks
   ) {
      ListTransaction<ItemStackTransaction> transaction = itemContainer.addItemStacks(itemStacks);
      boolean droppedItem = false;

      for (ItemStackTransaction stackTransaction : transaction.getList()) {
         ItemStack remainder = stackTransaction.getRemainder();
         if (!ItemStack.isEmpty(remainder)) {
            ItemUtils.dropItem(ref, remainder, store);
            droppedItem = true;
         }
      }

      return droppedItem;
   }

   public static boolean tryAddOrderedOrDropItemStacks(
      @Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull ItemContainer itemContainer, List<ItemStack> itemStacks
   ) {
      ListTransaction<ItemStackSlotTransaction> transaction = itemContainer.addItemStacksOrdered(itemStacks);
      List<ItemStack> remainderItemStacks = null;

      for (ItemStackSlotTransaction stackTransaction : transaction.getList()) {
         ItemStack remainder = stackTransaction.getRemainder();
         if (!ItemStack.isEmpty(remainder)) {
            if (remainderItemStacks == null) {
               remainderItemStacks = new ObjectArrayList();
            }

            remainderItemStacks.add(remainder);
         }
      }

      return addOrDropItemStacks(store, ref, itemContainer, remainderItemStacks);
   }
}
