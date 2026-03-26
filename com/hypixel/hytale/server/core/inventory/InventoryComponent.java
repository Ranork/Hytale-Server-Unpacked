package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.FetchedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainerUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class InventoryComponent implements Component<EntityStore> {
   public static final byte INACTIVE_SLOT_INDEX = -1;
   public static final short DEFAULT_HOTBAR_CAPACITY = 9;
   public static final short DEFAULT_UTILITY_CAPACITY = 4;
   public static final short DEFAULT_TOOLS_CAPACITY = 23;
   public static final short DEFAULT_ARMOR_CAPACITY = (short)ItemArmorSlot.VALUES.length;
   public static final short DEFAULT_STORAGE_ROWS = 4;
   public static final short DEFAULT_STORAGE_COLUMNS = 9;
   public static final short DEFAULT_STORAGE_CAPACITY = 36;
   public static final int HOTBAR_SECTION_ID = -1;
   public static final int STORAGE_SECTION_ID = -2;
   public static final int ARMOR_SECTION_ID = -3;
   public static final int UTILITY_SECTION_ID = -5;
   public static final int TOOLS_SECTION_ID = -8;
   public static final int BACKPACK_SECTION_ID = -9;
   public static final BuilderCodec<InventoryComponent> CODEC = BuilderCodec.abstractBuilder(InventoryComponent.class)
      .append(new KeyedCodec<>("Inventory", ItemContainer.CODEC), (o, i) -> o.inventory = i, o -> o.inventory)
      .add()
      .afterDecode(InventoryComponent::postDecode)
      .build();
   protected final AtomicBoolean isDirty = new AtomicBoolean();
   protected final AtomicBoolean needsSaving = new AtomicBoolean();
   protected ItemContainer inventory = EmptyItemContainer.INSTANCE;
   @Nullable
   protected EventRegistration<Void, ItemContainer.ItemContainerChangeEvent> changeEvent = null;
   protected ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> changeEvents = new ConcurrentLinkedQueue<>();
   public static ComponentType<EntityStore, ? extends InventoryComponent>[] HOTBAR_STORAGE_BACKPACK;
   public static ComponentType<EntityStore, ? extends InventoryComponent>[] HOTBAR_FIRST;
   public static ComponentType<EntityStore, ? extends InventoryComponent>[] STORAGE_FIRST;
   public static ComponentType<EntityStore, ? extends InventoryComponent>[] BACKPACK_STORAGE_HOTBAR;
   public static ComponentType<EntityStore, ? extends InventoryComponent>[] BACKPACK_HOTBAR_STORAGE;
   public static ComponentType<EntityStore, ? extends InventoryComponent>[] STORAGE_HOTBAR_BACKPACK;
   public static ComponentType<EntityStore, ? extends InventoryComponent>[] ARMOR_HOTBAR_UTILITY_STORAGE;
   public static ComponentType<EntityStore, ? extends InventoryComponent>[] HOTBAR_UTILITY_CONSUMABLE_STORAGE;
   public static ComponentType<EntityStore, ? extends InventoryComponent>[] EVERYTHING;

   public InventoryComponent() {
   }

   public InventoryComponent(short capacity) {
      this.inventory = (ItemContainer)(capacity == 0 ? EmptyItemContainer.INSTANCE : new SimpleItemContainer(capacity));
      this.registerChangeEvent();
   }

   public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
      if (this.inventory.getCapacity() != capacity) {
         this.unregisterChangeEvent();
         this.inventory = ItemContainer.ensureContainerCapacity(this.inventory, capacity, SimpleItemContainer::new, remainder);
         this.registerChangeEvent();
      }
   }

   protected void registerChangeEvent() {
      if (this.inventory != EmptyItemContainer.INSTANCE) {
         this.changeEvent = this.inventory.registerChangeEvent(itemContainerChangeEvent -> {
            this.markChanged();
            this.changeEvents.add(itemContainerChangeEvent);
         });
      }
   }

   protected void unregisterChangeEvent() {
      if (this.changeEvent != null) {
         this.changeEvent.unregister();
         this.changeEvent = null;
      }
   }

   protected void markChanged() {
      this.isDirty.set(true);
      this.needsSaving.set(true);
   }

   public void markDirty() {
      this.isDirty.set(true);
   }

   public boolean consumeIsDirty() {
      return this.isDirty.getAndSet(false);
   }

   public boolean consumeNeedsSaving() {
      return this.needsSaving.getAndSet(false);
   }

   public ItemContainer getInventory() {
      return this.inventory;
   }

   private void postDecode() {
      this.registerChangeEvent();
   }

   public ConcurrentLinkedQueue<ItemContainer.ItemContainerChangeEvent> getChangeEvents() {
      return this.changeEvents;
   }

   @Nullable
   @Override
   public abstract Component<EntityStore> clone();

   public static void setupCombined(
      ComponentType<EntityStore, InventoryComponent.Storage> storageInventoryComponentType,
      ComponentType<EntityStore, InventoryComponent.Armor> armorInventoryComponentType,
      ComponentType<EntityStore, InventoryComponent.Hotbar> hotbarInventoryComponentType,
      ComponentType<EntityStore, InventoryComponent.Utility> utilityInventoryComponentType,
      ComponentType<EntityStore, InventoryComponent.Backpack> backpackInventoryComponentType,
      ComponentType<EntityStore, InventoryComponent.Tool> toolInventoryComponentType
   ) {
      HOTBAR_STORAGE_BACKPACK = new ComponentType[]{hotbarInventoryComponentType, storageInventoryComponentType, backpackInventoryComponentType};
      HOTBAR_FIRST = new ComponentType[]{hotbarInventoryComponentType, storageInventoryComponentType};
      STORAGE_FIRST = new ComponentType[]{storageInventoryComponentType, hotbarInventoryComponentType};
      BACKPACK_STORAGE_HOTBAR = new ComponentType[]{backpackInventoryComponentType, storageInventoryComponentType, hotbarInventoryComponentType};
      BACKPACK_HOTBAR_STORAGE = new ComponentType[]{backpackInventoryComponentType, hotbarInventoryComponentType, storageInventoryComponentType};
      STORAGE_HOTBAR_BACKPACK = new ComponentType[]{storageInventoryComponentType, hotbarInventoryComponentType, backpackInventoryComponentType};
      ARMOR_HOTBAR_UTILITY_STORAGE = new ComponentType[]{
         armorInventoryComponentType, hotbarInventoryComponentType, utilityInventoryComponentType, storageInventoryComponentType
      };
      HOTBAR_UTILITY_CONSUMABLE_STORAGE = new ComponentType[]{hotbarInventoryComponentType, utilityInventoryComponentType, storageInventoryComponentType};
      EVERYTHING = new ComponentType[]{
         armorInventoryComponentType,
         hotbarInventoryComponentType,
         utilityInventoryComponentType,
         storageInventoryComponentType,
         backpackInventoryComponentType
      };
   }

   @Nullable
   public static ComponentType<EntityStore, ? extends InventoryComponent> getComponentTypeById(int id) {
      if (id >= 0) {
         return null;
      } else {
         return switch (id) {
            case -9 -> InventoryComponent.Backpack.getComponentType();
            case -8 -> InventoryComponent.Tool.getComponentType();
            default -> null;
            case -5 -> InventoryComponent.Utility.getComponentType();
            case -3 -> InventoryComponent.Armor.getComponentType();
            case -2 -> InventoryComponent.Storage.getComponentType();
            case -1 -> InventoryComponent.Hotbar.getComponentType();
         };
      }
   }

   @Nonnull
   @SafeVarargs
   public static CombinedItemContainer getCombined(
      @Nonnull ComponentAccessor<EntityStore> accessor,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentType<EntityStore, ? extends InventoryComponent>... types
   ) {
      InventoryComponent.Combined combined = accessor.getComponent(ref, InventoryComponent.Combined.getComponentType());
      if (combined == null) {
         combined = new InventoryComponent.Combined();
         if (accessor instanceof Store<EntityStore> store) {
            if (store.isProcessing()) {
               store.getExternalData().getWorld().execute(() -> {
                  if (ref.isValid()) {
                     store.putComponent(ref, InventoryComponent.Combined.getComponentType(), combined);
                  }
               });
            } else {
               accessor.putComponent(ref, InventoryComponent.Combined.getComponentType(), combined);
            }
         } else {
            accessor.putComponent(ref, InventoryComponent.Combined.getComponentType(), combined);
         }
      }

      CombinedItemContainer inv = (CombinedItemContainer)combined.inventories.get(types);
      if (inv != null) {
         return inv;
      } else {
         int count = 0;
         Archetype<EntityStore> archetype = accessor.getArchetype(ref);

         for (ComponentType<EntityStore, ? extends InventoryComponent> type : types) {
            if (archetype.contains(type)) {
               count++;
            }
         }

         ItemContainer[] containers = new ItemContainer[count];
         int i = 0;

         for (ComponentType<EntityStore, ? extends InventoryComponent> typex : types) {
            InventoryComponent innerInv = accessor.getComponent(ref, typex);
            if (innerInv != null) {
               containers[i++] = new FetchedItemContainer(innerInv::getInventory);
            }
         }

         inv = new CombinedItemContainer(containers);
         combined.inventories.put(types, inv);
         return inv;
      }
   }

   @Nonnull
   @SafeVarargs
   public static CombinedItemContainer getCombined(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      int index,
      @Nonnull ComponentType<EntityStore, ? extends InventoryComponent>... types
   ) {
      InventoryComponent.Combined combined = archetypeChunk.getComponent(index, InventoryComponent.Combined.getComponentType());
      if (combined == null) {
         combined = new InventoryComponent.Combined();
         commandBuffer.putComponent(archetypeChunk.getReferenceTo(index), InventoryComponent.Combined.getComponentType(), combined);
      }

      CombinedItemContainer inv = (CombinedItemContainer)combined.inventories.get(types);
      if (inv != null) {
         return inv;
      } else {
         int count = 0;
         Archetype<EntityStore> archetype = archetypeChunk.getArchetype();

         for (ComponentType<EntityStore, ? extends InventoryComponent> type : types) {
            if (archetype.contains(type)) {
               count++;
            }
         }

         ItemContainer[] containers = new ItemContainer[count];
         int i = 0;

         for (ComponentType<EntityStore, ? extends InventoryComponent> typex : types) {
            InventoryComponent innerInv = archetypeChunk.getComponent(index, typex);
            if (innerInv != null) {
               containers[i++] = new FetchedItemContainer(innerInv::getInventory);
            }
         }

         inv = new CombinedItemContainer(containers);
         combined.inventories.put(types, inv);
         return inv;
      }
   }

   @Nullable
   public static ItemStack getItemInHand(@Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull Ref<EntityStore> ref) {
      InventoryComponent.Tool toolComponent = accessor.getComponent(ref, InventoryComponent.Tool.getComponentType());
      if (toolComponent != null && toolComponent.isUsingToolsItem()) {
         return toolComponent.getActiveItem();
      } else {
         InventoryComponent.Hotbar hotbarComponent = accessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
         return hotbarComponent != null ? hotbarComponent.getActiveItem() : null;
      }
   }

   public static class Armor extends InventoryComponent {
      public static final BuilderCodec<InventoryComponent.Armor> CODEC = BuilderCodec.builder(
            InventoryComponent.Armor.class, InventoryComponent.Armor::new, InventoryComponent.CODEC
         )
         .afterDecode(InventoryComponent.Armor::afterDecode)
         .build();

      public static ComponentType<EntityStore, InventoryComponent.Armor> getComponentType() {
         return EntityModule.get().getArmorInventoryComponentType();
      }

      public Armor() {
      }

      public Armor(short capacity) {
         super(capacity);
         this.afterDecode();
      }

      public Armor(ItemContainer armor) {
         this.inventory = armor;
         this.registerChangeEvent();
         this.afterDecode();
      }

      private void afterDecode() {
         this.inventory = ItemContainerUtil.trySetArmorFilters(this.inventory);
      }

      @Override
      public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
         super.ensureCapacity(capacity, remainder);
         this.inventory = ItemContainerUtil.trySetArmorFilters(this.inventory);
      }

      @Nullable
      @Override
      public Component<EntityStore> clone() {
         InventoryComponent.Armor armor = new InventoryComponent.Armor();
         armor.inventory = this.inventory.clone();
         return armor;
      }
   }

   public static class Backpack extends InventoryComponent {
      public static final BuilderCodec<InventoryComponent.Backpack> CODEC = BuilderCodec.builder(
            InventoryComponent.Backpack.class, InventoryComponent.Backpack::new, InventoryComponent.CODEC
         )
         .build();

      public static ComponentType<EntityStore, InventoryComponent.Backpack> getComponentType() {
         return EntityModule.get().getBackpackInventoryComponentType();
      }

      public Backpack() {
      }

      public Backpack(short capacity) {
         super(capacity);
      }

      public Backpack(ItemContainer backpack) {
         this.inventory = backpack;
         this.registerChangeEvent();
      }

      public void resize(short capacity, @Nullable List<ItemStack> remainder) {
         this.unregisterChangeEvent();
         if (capacity > 0) {
            this.inventory = ItemContainer.ensureContainerCapacity(this.inventory, capacity, SimpleItemContainer::new, remainder);
         } else {
            this.inventory = ItemContainer.copy(this.inventory, EmptyItemContainer.INSTANCE, remainder);
         }

         this.registerChangeEvent();
         this.markChanged();
      }

      @Nullable
      @Override
      public Component<EntityStore> clone() {
         InventoryComponent.Backpack backpack = new InventoryComponent.Backpack();
         backpack.inventory = this.inventory.clone();
         return backpack;
      }
   }

   public static class Combined implements Component<EntityStore> {
      private final Object2ObjectOpenCustomHashMap<ComponentType[], CombinedItemContainer> inventories = new Object2ObjectOpenCustomHashMap(
         new Strategy<ComponentType[]>() {
            public int hashCode(ComponentType[] o) {
               return Arrays.hashCode((Object[])o);
            }

            public boolean equals(ComponentType[] a, ComponentType[] b) {
               return Arrays.equals((Object[])a, (Object[])b);
            }
         }
      );

      public static ComponentType<EntityStore, InventoryComponent.Combined> getComponentType() {
         return EntityModule.get().getCombinedInventoryComponentType();
      }

      @Nullable
      @Override
      public Component<EntityStore> clone() {
         return new InventoryComponent.Combined();
      }
   }

   public static class Hotbar extends InventoryComponent {
      public static final BuilderCodec<InventoryComponent.Hotbar> CODEC = BuilderCodec.builder(
            InventoryComponent.Hotbar.class, InventoryComponent.Hotbar::new, InventoryComponent.CODEC
         )
         .append(new KeyedCodec<>("ActiveSlot", Codec.BYTE), (o, i) -> o.activeSlot = i, o -> o.activeSlot)
         .add()
         .afterDecode(InventoryComponent.Hotbar::afterDecode)
         .build();
      protected byte activeSlot;

      public static ComponentType<EntityStore, InventoryComponent.Hotbar> getComponentType() {
         return EntityModule.get().getHotbarInventoryComponentType();
      }

      public Hotbar() {
      }

      public Hotbar(short capacity) {
         super(capacity);
      }

      public Hotbar(ItemContainer hotbar, byte activeHotbarSlot) {
         this.inventory = hotbar;
         this.activeSlot = activeHotbarSlot;
         this.registerChangeEvent();
      }

      @Override
      public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
         super.ensureCapacity(capacity, remainder);
         if (this.activeSlot >= this.inventory.getCapacity()) {
            this.activeSlot = (byte)(this.inventory.getCapacity() > 0 ? 0 : -1);
         }
      }

      private void afterDecode() {
         this.activeSlot = (byte)(this.activeSlot < this.inventory.getCapacity() ? this.activeSlot : (this.inventory.getCapacity() > 0 ? 0 : -1));
      }

      public byte getActiveSlot() {
         return this.activeSlot;
      }

      public void setActiveSlot(byte activeSlot) {
         this.activeSlot = activeSlot;
      }

      @Nullable
      public ItemStack getActiveItem() {
         return this.activeSlot != -1 && this.activeSlot < this.inventory.getCapacity() ? this.inventory.getItemStack(this.activeSlot) : null;
      }

      @Nullable
      @Override
      public Component<EntityStore> clone() {
         InventoryComponent.Hotbar hotbar = new InventoryComponent.Hotbar();
         hotbar.inventory = this.inventory.clone();
         hotbar.activeSlot = this.activeSlot;
         return hotbar;
      }
   }

   public static class Storage extends InventoryComponent {
      public static final BuilderCodec<InventoryComponent.Storage> CODEC = BuilderCodec.builder(
            InventoryComponent.Storage.class, InventoryComponent.Storage::new, InventoryComponent.CODEC
         )
         .build();

      public static ComponentType<EntityStore, InventoryComponent.Storage> getComponentType() {
         return EntityModule.get().getStorageInventoryComponentType();
      }

      public Storage() {
      }

      public Storage(short capacity) {
         super(capacity);
      }

      public Storage(ItemContainer storage) {
         this.inventory = storage;
         this.registerChangeEvent();
      }

      @Nullable
      @Override
      public Component<EntityStore> clone() {
         InventoryComponent.Storage storage = new InventoryComponent.Storage();
         storage.inventory = this.inventory.clone();
         return storage;
      }
   }

   public static class Tool extends InventoryComponent {
      public static final BuilderCodec<InventoryComponent.Tool> CODEC = BuilderCodec.builder(
            InventoryComponent.Tool.class, InventoryComponent.Tool::new, InventoryComponent.CODEC
         )
         .append(new KeyedCodec<>("ActiveSlot", Codec.BYTE), (o, i) -> o.activeSlot = i, o -> o.activeSlot)
         .add()
         .afterDecode(InventoryComponent.Tool::afterDecode)
         .build();
      protected byte activeSlot = -1;
      protected boolean usingToolsItem = false;

      public static ComponentType<EntityStore, InventoryComponent.Tool> getComponentType() {
         return EntityModule.get().getToolInventoryComponentType();
      }

      public Tool() {
      }

      public Tool(short capacity) {
         super(capacity);
      }

      public Tool(ItemContainer tools, byte toolsSlot) {
         this.inventory = tools;
         this.activeSlot = toolsSlot;
         this.registerChangeEvent();
      }

      @Override
      public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
         super.ensureCapacity(capacity, remainder);
         if (this.activeSlot >= this.inventory.getCapacity()) {
            this.activeSlot = -1;
         }
      }

      private void afterDecode() {
         this.activeSlot = this.activeSlot < this.inventory.getCapacity() ? this.activeSlot : -1;
      }

      public byte getActiveSlot() {
         return this.activeSlot;
      }

      public void setActiveSlot(byte activeSlot) {
         this.activeSlot = activeSlot;
      }

      @Nullable
      public ItemStack getActiveItem() {
         return this.activeSlot != -1 && this.activeSlot < this.inventory.getCapacity() ? this.inventory.getItemStack(this.activeSlot) : null;
      }

      public boolean isUsingToolsItem() {
         return this.usingToolsItem;
      }

      public void setUsingToolsItem(boolean usingToolsItem) {
         this.usingToolsItem = usingToolsItem;
      }

      @Nullable
      @Override
      public Component<EntityStore> clone() {
         InventoryComponent.Tool tool = new InventoryComponent.Tool();
         tool.inventory = this.inventory.clone();
         tool.activeSlot = this.activeSlot;
         tool.usingToolsItem = this.usingToolsItem;
         return tool;
      }
   }

   public static class Utility extends InventoryComponent {
      public static final BuilderCodec<InventoryComponent.Utility> CODEC = BuilderCodec.builder(
            InventoryComponent.Utility.class, InventoryComponent.Utility::new, InventoryComponent.CODEC
         )
         .append(new KeyedCodec<>("ActiveSlot", Codec.BYTE), (o, i) -> o.activeSlot = i, o -> o.activeSlot)
         .add()
         .afterDecode(InventoryComponent.Utility::afterDecode)
         .build();
      protected byte activeSlot = -1;

      public static ComponentType<EntityStore, InventoryComponent.Utility> getComponentType() {
         return EntityModule.get().getUtilityInventoryComponentType();
      }

      public Utility() {
      }

      public Utility(short capacity) {
         super(capacity);
         this.afterDecode();
      }

      public Utility(ItemContainer utility, byte utilitySlot) {
         this.inventory = utility;
         this.activeSlot = utilitySlot;
         this.registerChangeEvent();
         this.afterDecode();
      }

      @Override
      public void ensureCapacity(short capacity, @Nonnull List<ItemStack> remainder) {
         super.ensureCapacity(capacity, remainder);
         if (this.activeSlot >= this.inventory.getCapacity()) {
            this.activeSlot = -1;
         }

         this.inventory = ItemContainerUtil.trySetSlotFilters(
            this.inventory, (type, container, slot, itemStack) -> itemStack == null || itemStack.getItem().getUtility().isUsable()
         );
      }

      private void afterDecode() {
         this.inventory = ItemContainerUtil.trySetSlotFilters(
            this.inventory, (type, container, slot, itemStack) -> itemStack == null || itemStack.getItem().getUtility().isUsable()
         );
         this.activeSlot = this.activeSlot < this.inventory.getCapacity() ? this.activeSlot : -1;
      }

      public byte getActiveSlot() {
         return this.activeSlot;
      }

      public void setActiveSlot(byte activeSlot) {
         this.activeSlot = activeSlot;
      }

      @Nullable
      public ItemStack getActiveItem() {
         return this.activeSlot != -1 && this.activeSlot < this.inventory.getCapacity() ? this.inventory.getItemStack(this.activeSlot) : null;
      }

      @Nullable
      @Override
      public Component<EntityStore> clone() {
         InventoryComponent.Utility utility = new InventoryComponent.Utility();
         utility.inventory = this.inventory.clone();
         utility.activeSlot = this.activeSlot;
         return utility;
      }
   }
}
