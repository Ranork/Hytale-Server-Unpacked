package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.function.consumer.ShortObjectConsumer;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class EmptyItemContainer extends ItemContainer {
   public static final EmptyItemContainer INSTANCE = new EmptyItemContainer();
   public static final BuilderCodec<EmptyItemContainer> CODEC = BuilderCodec.builder(EmptyItemContainer.class, () -> INSTANCE).build();
   private static final EventRegistration<Void, ItemContainer.ItemContainerChangeEvent> EVENT_REGISTRATION = new EventRegistration<>(
      ItemContainer.ItemContainerChangeEvent.class, () -> false, () -> {}
   );

   protected EmptyItemContainer() {
   }

   @Override
   public short getCapacity() {
      return 0;
   }

   @Nonnull
   @Override
   public ClearTransaction clear() {
      return ClearTransaction.EMPTY;
   }

   @Override
   public void forEach(ShortObjectConsumer<ItemStack> action) {
   }

   @Override
   protected <V> V readAction(@Nonnull Supplier<V> action) {
      return action.get();
   }

   @Override
   protected <X, V> V readAction(@Nonnull Function<X, V> action, X x) {
      return action.apply(x);
   }

   @Override
   protected <V> V writeAction(@Nonnull Supplier<V> action) {
      return action.get();
   }

   @Override
   protected <X, V> V writeAction(@Nonnull Function<X, V> action, X x) {
      return action.apply(x);
   }

   @Nonnull
   @Override
   protected ClearTransaction internal_clear() {
      return ClearTransaction.EMPTY;
   }

   @Override
   protected ItemStack internal_getSlot(short slot) {
      throw new UnsupportedOperationException("getSlot(int) is not supported in EmptyItemContainer");
   }

   @Override
   protected ItemStack internal_setSlot(short slot, ItemStack itemStack) {
      throw new UnsupportedOperationException("setSlot(int, ItemStack) is not supported in EmptyItemContainer");
   }

   @Override
   protected ItemStack internal_removeSlot(short slot) {
      throw new UnsupportedOperationException("removeSlot(int) is not supported in EmptyItemContainer");
   }

   @Override
   protected boolean cantAddToSlot(short slot, ItemStack itemStack, ItemStack slotItemStack) {
      return false;
   }

   @Override
   protected boolean cantRemoveFromSlot(short slot) {
      return false;
   }

   @Override
   protected boolean cantDropFromSlot(short slot) {
      return false;
   }

   @Override
   protected boolean cantMoveToSlot(ItemContainer fromContainer, short slotFrom) {
      return false;
   }

   @Nonnull
   @Override
   public List<ItemStack> removeAllItemStacks() {
      return Collections.emptyList();
   }

   @Nonnull
   @Override
   public Map<Integer, ItemWithAllMetadata> toProtocolMap() {
      return Collections.emptyMap();
   }

   public EmptyItemContainer clone() {
      return INSTANCE;
   }

   @Override
   public EventRegistration registerChangeEvent(short priority, Consumer<ItemContainer.ItemContainerChangeEvent> consumer) {
      return EVENT_REGISTRATION;
   }

   @Override
   public void setGlobalFilter(FilterType globalFilter) {
   }

   @Override
   public void setSlotFilter(FilterActionType actionType, short slot, SlotFilter filter) {
      validateSlotIndex(slot, 0);
   }
}
