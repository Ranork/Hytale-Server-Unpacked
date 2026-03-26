package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.fastutil.shorts.Short2ObjectConcurrentHashMap;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.function.consumer.ShortObjectConsumer;
import com.hypixel.hytale.protocol.InventorySection;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.ResourceQuantity;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MaterialTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ResourceSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ResourceTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.TagSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.TagTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FetchedItemContainer extends ItemContainer {
   private final Supplier<ItemContainer> fetcher;

   public FetchedItemContainer(Supplier<ItemContainer> fetcher) {
      this.fetcher = fetcher;
   }

   @Override
   public short getCapacity() {
      return this.fetcher.get().getCapacity();
   }

   @Override
   public void setGlobalFilter(FilterType globalFilter) {
      this.fetcher.get().setGlobalFilter(globalFilter);
   }

   @Override
   public void setSlotFilter(FilterActionType actionType, short slot, SlotFilter filter) {
      this.fetcher.get().setSlotFilter(actionType, slot, filter);
   }

   @Override
   public ItemContainer clone() {
      return new FetchedItemContainer(this.fetcher);
   }

   @Override
   protected <V> V readAction(Supplier<V> action) {
      return this.fetcher.get().readAction(action);
   }

   @Override
   protected <X, V> V readAction(Function<X, V> action, X x) {
      return this.fetcher.get().readAction(action, x);
   }

   @Override
   protected <V> V writeAction(Supplier<V> action) {
      return this.fetcher.get().writeAction(action);
   }

   @Override
   protected <X, V> V writeAction(Function<X, V> action, X x) {
      return this.fetcher.get().writeAction(action, x);
   }

   @Override
   protected void lockForRead() {
      this.fetcher.get().lockForRead();
   }

   @Override
   protected void unlockForRead() {
      this.fetcher.get().unlockForRead();
   }

   @Override
   protected void lockForWrite() {
      this.fetcher.get().lockForWrite();
   }

   @Override
   protected void unlockForWrite() {
      this.fetcher.get().unlockForWrite();
   }

   @Override
   protected ClearTransaction internal_clear() {
      return this.fetcher.get().internal_clear();
   }

   @Nullable
   @Override
   protected ItemStack internal_getSlot(short slot) {
      return this.fetcher.get().internal_getSlot(slot);
   }

   @Nullable
   @Override
   protected ItemStack internal_setSlot(short slot, ItemStack itemStack) {
      return this.fetcher.get().internal_setSlot(slot, itemStack);
   }

   @Nullable
   @Override
   protected ItemStack internal_removeSlot(short slot) {
      return this.fetcher.get().internal_removeSlot(slot);
   }

   @Override
   protected boolean cantAddToSlot(short slot, ItemStack itemStack, ItemStack slotItemStack) {
      return this.fetcher.get().cantAddToSlot(slot, itemStack, slotItemStack);
   }

   @Override
   protected boolean cantRemoveFromSlot(short slot) {
      return this.fetcher.get().cantRemoveFromSlot(slot);
   }

   @Override
   protected boolean cantDropFromSlot(short slot) {
      return this.fetcher.get().cantDropFromSlot(slot);
   }

   @Override
   protected boolean cantMoveToSlot(ItemContainer fromContainer, short slotFrom) {
      return this.fetcher.get().cantMoveToSlot(fromContainer, slotFrom);
   }

   @Nonnull
   @Override
   public InventorySection toPacket() {
      return this.fetcher.get().toPacket();
   }

   @Nonnull
   @Override
   public Map<Integer, ItemWithAllMetadata> toProtocolMap() {
      return this.fetcher.get().toProtocolMap();
   }

   @Override
   public EventRegistration<Void, ItemContainer.ItemContainerChangeEvent> registerChangeEvent(
      @Nonnull Consumer<ItemContainer.ItemContainerChangeEvent> consumer
   ) {
      return this.fetcher.get().registerChangeEvent(consumer);
   }

   @Override
   public EventRegistration<Void, ItemContainer.ItemContainerChangeEvent> registerChangeEvent(
      @Nonnull EventPriority priority, @Nonnull Consumer<ItemContainer.ItemContainerChangeEvent> consumer
   ) {
      return this.fetcher.get().registerChangeEvent(priority, consumer);
   }

   @Override
   public EventRegistration<Void, ItemContainer.ItemContainerChangeEvent> registerChangeEvent(
      short priority, @Nonnull Consumer<ItemContainer.ItemContainerChangeEvent> consumer
   ) {
      return this.fetcher.get().registerChangeEvent(priority, consumer);
   }

   @Override
   public ClearTransaction clear() {
      return this.fetcher.get().clear();
   }

   @Override
   public boolean canAddItemStackToSlot(short slot, @Nonnull ItemStack itemStack, boolean allOrNothing, boolean filter) {
      return this.fetcher.get().canAddItemStackToSlot(slot, itemStack, allOrNothing, filter);
   }

   @Nonnull
   @Override
   public ItemStackSlotTransaction addItemStackToSlot(short slot, @Nonnull ItemStack itemStack) {
      return this.fetcher.get().addItemStackToSlot(slot, itemStack);
   }

   @Nonnull
   @Override
   public ItemStackSlotTransaction addItemStackToSlot(short slot, @Nonnull ItemStack itemStack, boolean allOrNothing, boolean filter) {
      return this.fetcher.get().addItemStackToSlot(slot, itemStack, allOrNothing, filter);
   }

   @Nonnull
   @Override
   public ItemStackSlotTransaction setItemStackForSlot(short slot, ItemStack itemStack) {
      return this.fetcher.get().setItemStackForSlot(slot, itemStack);
   }

   @Nonnull
   @Override
   public ItemStackSlotTransaction setItemStackForSlot(short slot, ItemStack itemStack, boolean filter) {
      return this.fetcher.get().setItemStackForSlot(slot, itemStack, filter);
   }

   @Nullable
   @Override
   public ItemStack getItemStack(short slot) {
      return this.fetcher.get().getItemStack(slot);
   }

   @Nonnull
   @Override
   public ItemStackSlotTransaction replaceItemStackInSlot(short slot, ItemStack itemStackToRemove, ItemStack itemStack) {
      return this.fetcher.get().replaceItemStackInSlot(slot, itemStackToRemove, itemStack);
   }

   @Override
   public ListTransaction<ItemStackSlotTransaction> replaceAll(SlotReplacementFunction func) {
      return this.fetcher.get().replaceAll(func);
   }

   @Override
   protected ItemStackSlotTransaction internal_replaceItemStack(short slot, @Nullable ItemStack itemStackToRemove, ItemStack itemStack) {
      return this.fetcher.get().internal_replaceItemStack(slot, itemStackToRemove, itemStack);
   }

   @Nonnull
   @Override
   public SlotTransaction removeItemStackFromSlot(short slot) {
      return this.fetcher.get().removeItemStackFromSlot(slot);
   }

   @Nonnull
   @Override
   public SlotTransaction removeItemStackFromSlot(short slot, boolean filter) {
      return this.fetcher.get().removeItemStackFromSlot(slot, filter);
   }

   @Nonnull
   @Override
   public ItemStackSlotTransaction removeItemStackFromSlot(short slot, int quantityToRemove) {
      return this.fetcher.get().removeItemStackFromSlot(slot, quantityToRemove);
   }

   @Nonnull
   @Override
   public ItemStackSlotTransaction removeItemStackFromSlot(short slot, int quantityToRemove, boolean allOrNothing, boolean filter) {
      return this.fetcher.get().removeItemStackFromSlot(slot, quantityToRemove, allOrNothing, filter);
   }

   @Deprecated
   @Override
   public ItemStackSlotTransaction internal_removeItemStack(short slot, int quantityToRemove) {
      return this.fetcher.get().internal_removeItemStack(slot, quantityToRemove);
   }

   @Nonnull
   @Override
   public ItemStackSlotTransaction removeItemStackFromSlot(short slot, ItemStack itemStackToRemove, int quantityToRemove) {
      return this.fetcher.get().removeItemStackFromSlot(slot, itemStackToRemove, quantityToRemove);
   }

   @Nonnull
   @Override
   public ItemStackSlotTransaction removeItemStackFromSlot(short slot, ItemStack itemStackToRemove, int quantityToRemove, boolean allOrNothing, boolean filter) {
      return this.fetcher.get().removeItemStackFromSlot(slot, itemStackToRemove, quantityToRemove, allOrNothing, filter);
   }

   @Nonnull
   @Override
   public MaterialSlotTransaction removeMaterialFromSlot(short slot, @Nonnull MaterialQuantity material) {
      return this.fetcher.get().removeMaterialFromSlot(slot, material);
   }

   @Nonnull
   @Override
   public MaterialSlotTransaction removeMaterialFromSlot(
      short slot, @Nonnull MaterialQuantity material, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return this.fetcher.get().removeMaterialFromSlot(slot, material, allOrNothing, exactAmount, filter);
   }

   @Nonnull
   @Override
   public ResourceSlotTransaction removeResourceFromSlot(short slot, @Nonnull ResourceQuantity resource) {
      return this.fetcher.get().removeResourceFromSlot(slot, resource);
   }

   @Nonnull
   @Override
   public ResourceSlotTransaction removeResourceFromSlot(
      short slot, @Nonnull ResourceQuantity resource, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return this.fetcher.get().removeResourceFromSlot(slot, resource, allOrNothing, exactAmount, filter);
   }

   @Nonnull
   @Override
   public TagSlotTransaction removeTagFromSlot(short slot, int tagIndex, int quantity) {
      return this.fetcher.get().removeTagFromSlot(slot, tagIndex, quantity);
   }

   @Nonnull
   @Override
   public TagSlotTransaction removeTagFromSlot(short slot, int tagIndex, int quantity, boolean allOrNothing, boolean filter) {
      return this.fetcher.get().removeTagFromSlot(slot, tagIndex, quantity, allOrNothing, filter);
   }

   @Nonnull
   @Override
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(short slot, @Nonnull ItemContainer containerTo) {
      return this.fetcher.get().moveItemStackFromSlot(slot, containerTo);
   }

   @Nonnull
   @Override
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(short slot, @Nonnull ItemContainer containerTo, boolean filter) {
      return this.fetcher.get().moveItemStackFromSlot(slot, containerTo, filter);
   }

   @Nonnull
   @Override
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(short slot, @Nonnull ItemContainer containerTo, boolean allOrNothing, boolean filter) {
      return this.fetcher.get().moveItemStackFromSlot(slot, containerTo, allOrNothing, filter);
   }

   @Override
   protected MoveTransaction<ItemStackTransaction> internal_moveItemStackFromSlot(
      short slot, @Nonnull ItemContainer containerTo, boolean allOrNothing, boolean filter
   ) {
      return this.fetcher.get().internal_moveItemStackFromSlot(slot, containerTo, allOrNothing, filter);
   }

   @Nonnull
   @Override
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(short slot, int quantity, @Nonnull ItemContainer containerTo) {
      return this.fetcher.get().moveItemStackFromSlot(slot, quantity, containerTo);
   }

   @Nonnull
   @Override
   public MoveTransaction<ItemStackTransaction> moveItemStackFromSlot(
      short slot, int quantity, @Nonnull ItemContainer containerTo, boolean allOrNothing, boolean filter
   ) {
      return this.fetcher.get().moveItemStackFromSlot(slot, quantity, containerTo, allOrNothing, filter);
   }

   @Override
   protected MoveTransaction<ItemStackTransaction> internal_moveItemStackFromSlot(
      short slot, int quantity, @Nonnull ItemContainer containerTo, boolean allOrNothing, boolean filter
   ) {
      return this.fetcher.get().internal_moveItemStackFromSlot(slot, quantity, containerTo, allOrNothing, filter);
   }

   @Nonnull
   @Override
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveItemStackFromSlot(short slot, ItemContainer... containerTo) {
      return this.fetcher.get().moveItemStackFromSlot(slot, containerTo);
   }

   @Nonnull
   @Override
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveItemStackFromSlot(
      short slot, boolean allOrNothing, boolean filter, @Nonnull ItemContainer... containerTo
   ) {
      return this.fetcher.get().moveItemStackFromSlot(slot, allOrNothing, filter, containerTo);
   }

   @Nonnull
   @Override
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveItemStackFromSlot(short slot, int quantity, ItemContainer... containerTo) {
      return this.fetcher.get().moveItemStackFromSlot(slot, quantity, containerTo);
   }

   @Nonnull
   @Override
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveItemStackFromSlot(
      short slot, int quantity, boolean allOrNothing, boolean filter, @Nonnull ItemContainer... containerTo
   ) {
      return this.fetcher.get().moveItemStackFromSlot(slot, quantity, allOrNothing, filter, containerTo);
   }

   @Nonnull
   @Override
   public MoveTransaction<SlotTransaction> moveItemStackFromSlotToSlot(short slot, int quantity, @Nonnull ItemContainer containerTo, short slotTo) {
      return this.fetcher.get().moveItemStackFromSlotToSlot(slot, quantity, containerTo, slotTo);
   }

   @Nonnull
   @Override
   public MoveTransaction<SlotTransaction> moveItemStackFromSlotToSlot(
      short slot, int quantity, @Nonnull ItemContainer containerTo, short slotTo, boolean filter
   ) {
      return this.fetcher.get().moveItemStackFromSlotToSlot(slot, quantity, containerTo, slotTo, filter);
   }

   @Override
   protected MoveTransaction<SlotTransaction> internal_moveItemStackFromSlot(
      short slot, int quantity, @Nonnull ItemContainer containerTo, short slotTo, boolean filter
   ) {
      return this.fetcher.get().internal_moveItemStackFromSlot(slot, quantity, containerTo, slotTo, filter);
   }

   @Nonnull
   @Override
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveAllItemStacksTo(ItemContainer... containerTo) {
      return this.fetcher.get().moveAllItemStacksTo(containerTo);
   }

   @Nonnull
   @Override
   public ListTransaction<MoveTransaction<ItemStackTransaction>> moveAllItemStacksTo(Predicate<ItemStack> itemPredicate, ItemContainer... containerTo) {
      return this.fetcher.get().moveAllItemStacksTo(itemPredicate, containerTo);
   }

   @Nonnull
   @Override
   protected ListTransaction<MoveTransaction<ItemStackTransaction>> internal_moveAllItemStacksTo(
      @Nullable Predicate<ItemStack> itemPredicate, ItemContainer[] containerTo
   ) {
      return this.fetcher.get().internal_moveAllItemStacksTo(itemPredicate, containerTo);
   }

   @Nonnull
   @Override
   public ListTransaction<MoveTransaction<ItemStackTransaction>> quickStackTo(@Nonnull ItemContainer... containerTo) {
      return this.fetcher.get().quickStackTo(containerTo);
   }

   @Nonnull
   @Override
   public ListTransaction<MoveTransaction<SlotTransaction>> combineItemStacksIntoSlot(@Nonnull ItemContainer containerTo, short slotTo) {
      return this.fetcher.get().combineItemStacksIntoSlot(containerTo, slotTo);
   }

   @Nonnull
   @Override
   protected ListTransaction<MoveTransaction<SlotTransaction>> internal_combineItemStacksIntoSlot(@Nonnull ItemContainer containerTo, short slotTo) {
      return this.fetcher.get().internal_combineItemStacksIntoSlot(containerTo, slotTo);
   }

   @Nonnull
   @Override
   public ListTransaction<MoveTransaction<SlotTransaction>> swapItems(short srcPos, @Nonnull ItemContainer containerTo, short destPos, short length) {
      return this.fetcher.get().swapItems(srcPos, containerTo, destPos, length);
   }

   @Nonnull
   @Override
   protected ListTransaction<MoveTransaction<SlotTransaction>> internal_swapItems(short srcPos, @Nonnull ItemContainer containerTo, short destPos, short length) {
      return this.fetcher.get().internal_swapItems(srcPos, containerTo, destPos, length);
   }

   @Nonnull
   @Override
   protected MoveTransaction<SlotTransaction> internal_swapItems(@Nonnull ItemContainer containerTo, short slotFrom, short slotTo) {
      return this.fetcher.get().internal_swapItems(containerTo, slotFrom, slotTo);
   }

   @Override
   public boolean canAddItemStack(@Nonnull ItemStack itemStack) {
      return this.fetcher.get().canAddItemStack(itemStack);
   }

   @Override
   public boolean canAddItemStack(@Nonnull ItemStack itemStack, boolean fullStacks, boolean filter) {
      return this.fetcher.get().canAddItemStack(itemStack, fullStacks, filter);
   }

   @Nonnull
   @Override
   public ItemStackTransaction addItemStack(@Nonnull ItemStack itemStack) {
      return this.fetcher.get().addItemStack(itemStack);
   }

   @Nonnull
   @Override
   public ItemStackTransaction addItemStack(@Nonnull ItemStack itemStack, boolean allOrNothing, boolean fullStacks, boolean filter) {
      return this.fetcher.get().addItemStack(itemStack, allOrNothing, fullStacks, filter);
   }

   @Override
   public boolean canAddItemStacks(List<ItemStack> itemStacks) {
      return this.fetcher.get().canAddItemStacks(itemStacks);
   }

   @Override
   public boolean canAddItemStacks(@Nullable List<ItemStack> itemStacks, boolean fullStacks, boolean filter) {
      return this.fetcher.get().canAddItemStacks(itemStacks, fullStacks, filter);
   }

   @Override
   public ListTransaction<ItemStackTransaction> addItemStacks(List<ItemStack> itemStacks) {
      return this.fetcher.get().addItemStacks(itemStacks);
   }

   @Override
   public ListTransaction<ItemStackTransaction> addItemStacks(@Nullable List<ItemStack> itemStacks, boolean allOrNothing, boolean fullStacks, boolean filter) {
      return this.fetcher.get().addItemStacks(itemStacks, allOrNothing, fullStacks, filter);
   }

   @Override
   public ListTransaction<ItemStackSlotTransaction> addItemStacksOrdered(List<ItemStack> itemStacks) {
      return this.fetcher.get().addItemStacksOrdered(itemStacks);
   }

   @Override
   public ListTransaction<ItemStackSlotTransaction> addItemStacksOrdered(short offset, List<ItemStack> itemStacks) {
      return this.fetcher.get().addItemStacksOrdered(offset, itemStacks);
   }

   @Override
   public ListTransaction<ItemStackSlotTransaction> addItemStacksOrdered(List<ItemStack> itemStacks, boolean allOrNothing, boolean filter) {
      return this.fetcher.get().addItemStacksOrdered(itemStacks, allOrNothing, filter);
   }

   @Override
   public ListTransaction<ItemStackSlotTransaction> addItemStacksOrdered(
      short offset, @Nullable List<ItemStack> itemStacks, boolean allOrNothing, boolean filter
   ) {
      return this.fetcher.get().addItemStacksOrdered(offset, itemStacks, allOrNothing, filter);
   }

   @Override
   public boolean canRemoveItemStack(ItemStack itemStack) {
      return this.fetcher.get().canRemoveItemStack(itemStack);
   }

   @Override
   public boolean canRemoveItemStack(@Nullable ItemStack itemStack, boolean exactAmount, boolean filter) {
      return this.fetcher.get().canRemoveItemStack(itemStack, exactAmount, filter);
   }

   @Nonnull
   @Override
   public ItemStackTransaction removeItemStack(@Nonnull ItemStack itemStack) {
      return this.fetcher.get().removeItemStack(itemStack);
   }

   @Nonnull
   @Override
   public ItemStackTransaction removeItemStack(@Nonnull ItemStack itemStack, boolean allOrNothing, boolean filter) {
      return this.fetcher.get().removeItemStack(itemStack, allOrNothing, filter);
   }

   @Override
   public boolean canRemoveItemStacks(List<ItemStack> itemStacks) {
      return this.fetcher.get().canRemoveItemStacks(itemStacks);
   }

   @Override
   public boolean canRemoveItemStacks(@Nullable List<ItemStack> itemStacks, boolean exactAmount, boolean filter) {
      return this.fetcher.get().canRemoveItemStacks(itemStacks, exactAmount, filter);
   }

   @Override
   public ListTransaction<ItemStackTransaction> removeItemStacks(List<ItemStack> itemStacks) {
      return this.fetcher.get().removeItemStacks(itemStacks);
   }

   @Override
   public ListTransaction<ItemStackTransaction> removeItemStacks(@Nullable List<ItemStack> itemStacks, boolean allOrNothing, boolean filter) {
      return this.fetcher.get().removeItemStacks(itemStacks, allOrNothing, filter);
   }

   @Override
   public boolean canRemoveTag(int tagIndex, int quantity) {
      return this.fetcher.get().canRemoveTag(tagIndex, quantity);
   }

   @Override
   public boolean canRemoveTag(int tagIndex, int quantity, boolean exactAmount, boolean filter) {
      return this.fetcher.get().canRemoveTag(tagIndex, quantity, exactAmount, filter);
   }

   @Nonnull
   @Override
   public TagTransaction removeTag(int tagIndex, int quantity) {
      return this.fetcher.get().removeTag(tagIndex, quantity);
   }

   @Nonnull
   @Override
   public TagTransaction removeTag(int tagIndex, int quantity, boolean allOrNothing, boolean exactAmount, boolean filter) {
      return this.fetcher.get().removeTag(tagIndex, quantity, allOrNothing, exactAmount, filter);
   }

   @Override
   public boolean canRemoveResource(ResourceQuantity resource) {
      return this.fetcher.get().canRemoveResource(resource);
   }

   @Override
   public boolean canRemoveResource(@Nullable ResourceQuantity resource, boolean exactAmount, boolean filter) {
      return this.fetcher.get().canRemoveResource(resource, exactAmount, filter);
   }

   @Nonnull
   @Override
   public ResourceTransaction removeResource(@Nonnull ResourceQuantity resource) {
      return this.fetcher.get().removeResource(resource);
   }

   @Nonnull
   @Override
   public ResourceTransaction removeResource(@Nonnull ResourceQuantity resource, boolean allOrNothing, boolean exactAmount, boolean filter) {
      return this.fetcher.get().removeResource(resource, allOrNothing, exactAmount, filter);
   }

   @Override
   public boolean canRemoveResources(List<ResourceQuantity> resources) {
      return this.fetcher.get().canRemoveResources(resources);
   }

   @Override
   public boolean canRemoveResources(@Nullable List<ResourceQuantity> resources, boolean exactAmount, boolean filter) {
      return this.fetcher.get().canRemoveResources(resources, exactAmount, filter);
   }

   @Override
   public ListTransaction<ResourceTransaction> removeResources(List<ResourceQuantity> resources) {
      return this.fetcher.get().removeResources(resources);
   }

   @Override
   public ListTransaction<ResourceTransaction> removeResources(
      @Nullable List<ResourceQuantity> resources, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return this.fetcher.get().removeResources(resources, allOrNothing, exactAmount, filter);
   }

   @Override
   public boolean canRemoveMaterial(MaterialQuantity material) {
      return this.fetcher.get().canRemoveMaterial(material);
   }

   @Override
   public boolean canRemoveMaterial(@Nullable MaterialQuantity material, boolean exactAmount, boolean filter) {
      return this.fetcher.get().canRemoveMaterial(material, exactAmount, filter);
   }

   @Nonnull
   @Override
   public MaterialTransaction removeMaterial(@Nonnull MaterialQuantity material) {
      return this.fetcher.get().removeMaterial(material);
   }

   @Nonnull
   @Override
   public MaterialTransaction removeMaterial(@Nonnull MaterialQuantity material, boolean allOrNothing, boolean exactAmount, boolean filter) {
      return this.fetcher.get().removeMaterial(material, allOrNothing, exactAmount, filter);
   }

   @Override
   public boolean canRemoveMaterials(List<MaterialQuantity> materials) {
      return this.fetcher.get().canRemoveMaterials(materials);
   }

   @Override
   public boolean canRemoveMaterials(@Nullable List<MaterialQuantity> materials, boolean exactAmount, boolean filter) {
      return this.fetcher.get().canRemoveMaterials(materials, exactAmount, filter);
   }

   @Override
   public List<TestRemoveItemSlotResult> getSlotMaterialsToRemove(@Nullable List<MaterialQuantity> materials, boolean exactAmount, boolean filter) {
      return this.fetcher.get().getSlotMaterialsToRemove(materials, exactAmount, filter);
   }

   @Override
   public ListTransaction<MaterialTransaction> removeMaterials(List<MaterialQuantity> materials) {
      return this.fetcher.get().removeMaterials(materials);
   }

   @Override
   public ListTransaction<MaterialTransaction> removeMaterials(
      @Nullable List<MaterialQuantity> materials, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return this.fetcher.get().removeMaterials(materials, allOrNothing, exactAmount, filter);
   }

   @Override
   public ListTransaction<MaterialSlotTransaction> removeMaterialsOrdered(short offset, List<MaterialQuantity> materials) {
      return this.fetcher.get().removeMaterialsOrdered(offset, materials);
   }

   @Override
   public ListTransaction<MaterialSlotTransaction> removeMaterialsOrdered(
      List<MaterialQuantity> materials, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return this.fetcher.get().removeMaterialsOrdered(materials, allOrNothing, exactAmount, filter);
   }

   @Override
   public ListTransaction<MaterialSlotTransaction> removeMaterialsOrdered(
      short offset, @Nullable List<MaterialQuantity> materials, boolean allOrNothing, boolean exactAmount, boolean filter
   ) {
      return this.fetcher.get().removeMaterialsOrdered(offset, materials, allOrNothing, exactAmount, filter);
   }

   @Override
   public boolean isEmpty() {
      return this.fetcher.get().isEmpty();
   }

   @Override
   public int countItemStacks(@Nonnull Predicate<ItemStack> itemPredicate) {
      return this.fetcher.get().countItemStacks(itemPredicate);
   }

   @Override
   public boolean containsItemStacksStackableWith(@Nonnull ItemStack itemStack) {
      return this.fetcher.get().containsItemStacksStackableWith(itemStack);
   }

   @Override
   public void forEach(@Nonnull ShortObjectConsumer<ItemStack> action) {
      this.fetcher.get().forEach(action);
   }

   @Override
   public <T> void forEachWithMeta(@Nonnull Short2ObjectConcurrentHashMap.ShortBiObjConsumer<ItemStack, T> consumer, T meta) {
      this.fetcher.get().forEachWithMeta(consumer, meta);
   }

   @Nonnull
   @Override
   public List<ItemStack> removeAllItemStacks() {
      return this.fetcher.get().removeAllItemStacks();
   }

   @Nonnull
   @Override
   public List<ItemStack> dropAllItemStacks() {
      return this.fetcher.get().dropAllItemStacks();
   }

   @Nonnull
   @Override
   public List<ItemStack> dropAllItemStacks(boolean filter) {
      return this.fetcher.get().dropAllItemStacks(filter);
   }

   @Nonnull
   @Override
   public ListTransaction<SlotTransaction> sortItems(@Nonnull SortType sort) {
      return this.fetcher.get().sortItems(sort);
   }

   @Override
   protected ListTransaction<SlotTransaction> internal_sortItems(@Nonnull SortType sort) {
      return this.fetcher.get().internal_sortItems(sort);
   }

   @Override
   protected void sendUpdate(@Nonnull Transaction transaction) {
      this.fetcher.get().sendUpdate(transaction);
   }

   @Override
   public boolean containsContainer(ItemContainer itemContainer) {
      return this.fetcher.get().containsContainer(itemContainer);
   }

   @Override
   public void doMigration(Function<String, String> blockMigration) {
      this.fetcher.get().doMigration(blockMigration);
   }
}
