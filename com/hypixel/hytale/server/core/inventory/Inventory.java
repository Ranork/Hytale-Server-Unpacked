package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.SmartMoveType;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ItemContainerWindow;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated(forRemoval = true)
public class Inventory {
   public static final int VERSION = 4;
   @Nonnull
   public static final BuilderCodec<Inventory> CODEC = BuilderCodec.builder(Inventory.class, Inventory::new)
      .versioned()
      .codecVersion(4)
      .append(new KeyedCodec<>("Storage", ItemContainer.CODEC), (o, i) -> o.deserializedStorage = i, o -> o.deserializedStorage)
      .add()
      .append(new KeyedCodec<>("Armor", ItemContainer.CODEC), (o, i) -> o.deserializedArmor = i, o -> o.deserializedArmor)
      .add()
      .append(new KeyedCodec<>("HotBar", ItemContainer.CODEC), (o, i) -> o.deserializedHotbar = i, o -> o.deserializedHotbar)
      .add()
      .append(new KeyedCodec<>("Utility", ItemContainer.CODEC), (o, i) -> o.deserializedUtility = i, o -> o.deserializedUtility)
      .add()
      .append(new KeyedCodec<>("Backpack", ItemContainer.CODEC), (o, i) -> o.deserializedBackpack = i, o -> o.deserializedBackpack)
      .add()
      .append(new KeyedCodec<>("ActiveHotbarSlot", Codec.BYTE), (o, i) -> o.deserializedActiveHotbarSlot = i, o -> o.deserializedActiveHotbarSlot)
      .add()
      .append(new KeyedCodec<>("Tool", ItemContainer.CODEC), (o, i) -> o.deserializedTools = i, o -> o.deserializedTools)
      .add()
      .append(new KeyedCodec<>("ActiveToolsSlot", Codec.BYTE), (o, i) -> o.deserializedActiveToolsSlot = i, o -> o.deserializedActiveToolsSlot)
      .add()
      .append(new KeyedCodec<>("ActiveUtilitySlot", Codec.BYTE), (o, i) -> o.deserializedActiveUtilitySlot = i, o -> o.deserializedActiveUtilitySlot)
      .add()
      .build();
   private ItemContainer deserializedStorage;
   private ItemContainer deserializedArmor;
   private ItemContainer deserializedHotbar;
   private ItemContainer deserializedUtility;
   private ItemContainer deserializedTools;
   private ItemContainer deserializedBackpack;
   private byte deserializedActiveHotbarSlot;
   private byte deserializedActiveUtilitySlot = -1;
   private byte deserializedActiveToolsSlot = -1;
   @Nullable
   private InventoryComponent.Storage storage;
   @Nullable
   private InventoryComponent.Armor armor;
   @Nullable
   private InventoryComponent.Hotbar hotbar;
   @Nullable
   private InventoryComponent.Utility utility;
   @Nullable
   private InventoryComponent.Tool tools;
   @Nullable
   private InventoryComponent.Backpack backpack;
   @Nullable
   private LivingEntity entity;

   @Deprecated(forRemoval = true)
   public void unregister() {
      this.entity = null;
   }

   @Deprecated(forRemoval = true)
   public static void moveItem(
      @Nonnull Ref<EntityStore> ref,
      int fromSectionId,
      int fromSlotId,
      int quantity,
      int toSectionId,
      int toSlotId,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      InventoryUtils.moveItem(ref, fromSectionId, fromSlotId, quantity, toSectionId, toSlotId, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   public static void smartMoveItem(
      @Nonnull Ref<EntityStore> ref,
      int fromSectionId,
      int fromSlotId,
      int quantity,
      @Nonnull SmartMoveType moveType,
      PlayerSettings settings,
      @Nonnull ComponentAccessor<EntityStore> accessor
   ) {
      InventoryUtils.smartMoveItem(ref, fromSectionId, fromSlotId, quantity, moveType, settings, accessor);
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ListTransaction<MoveTransaction<ItemStackTransaction>> takeAll(
      @Nonnull Ref<EntityStore> ref, int inventorySectionId, PlayerSettings settings, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return InventoryUtils.takeAll(ref, inventorySectionId, settings, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   public static ListTransaction<MoveTransaction<ItemStackTransaction>> takeAllWithPriority(
      @Nonnull Ref<EntityStore> ref, ItemContainer fromContainer, PlayerSettings settings, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return InventoryUtils.takeAllWithPriority(ref, fromContainer, settings, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ListTransaction<MoveTransaction<ItemStackTransaction>> putAll(int inventorySectionId) {
      ItemContainer sectionById = this.getSectionById(inventorySectionId);
      return sectionById != null ? this.storage.getInventory().moveAllItemStacksTo(sectionById) : null;
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ListTransaction<MoveTransaction<ItemStackTransaction>> quickStack(
      @Nonnull Ref<EntityStore> ref, int inventorySectionId, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return InventoryUtils.quickStack(ref, inventorySectionId, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   @Nonnull
   public List<ItemStack> dropAllItemStacks() {
      if (this.entity == null) {
         return List.of();
      } else {
         Ref<EntityStore> ref = this.entity.getReference();
         return ref != null && ref.isValid() ? InventoryUtils.dropAllItemStacks(ref, ref.getStore()) : List.of();
      }
   }

   @Deprecated(forRemoval = true)
   public void clear() {
      if (this.entity != null) {
         Ref<EntityStore> ref = this.entity.getReference();
         if (ref != null && ref.isValid()) {
            InventoryUtils.clear(ref, ref.getStore());
         }
      }
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemContainer getStorage() {
      return this.storage != null ? this.storage.getInventory() : null;
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemContainer getArmor() {
      return this.armor != null ? this.armor.getInventory() : null;
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemContainer getHotbar() {
      return this.hotbar != null ? this.hotbar.getInventory() : null;
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemContainer getUtility() {
      return this.utility != null ? this.utility.getInventory() : null;
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemContainer getTools() {
      return this.tools != null ? this.tools.getInventory() : null;
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemContainer getBackpack() {
      return this.backpack != null ? this.backpack.getInventory() : null;
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public CombinedItemContainer getCombinedHotbarFirst() {
      if (this.entity == null) {
         return null;
      } else {
         Ref<EntityStore> ref = this.entity.getReference();
         return ref != null && ref.isValid() ? InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.HOTBAR_FIRST) : null;
      }
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public CombinedItemContainer getCombinedStorageFirst() {
      if (this.entity == null) {
         return null;
      } else {
         Ref<EntityStore> ref = this.entity.getReference();
         return ref != null && ref.isValid() ? InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.STORAGE_FIRST) : null;
      }
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public CombinedItemContainer getCombinedBackpackStorageHotbar() {
      if (this.entity == null) {
         return null;
      } else {
         Ref<EntityStore> ref = this.entity.getReference();
         return ref != null && ref.isValid() ? InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.BACKPACK_STORAGE_HOTBAR) : null;
      }
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public CombinedItemContainer getCombinedBackpackStorageHotbarFirst() {
      if (this.entity == null) {
         return null;
      } else {
         Ref<EntityStore> ref = this.entity.getReference();
         return ref != null && ref.isValid() ? InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.HOTBAR_STORAGE_BACKPACK) : null;
      }
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public CombinedItemContainer getCombinedArmorHotbarUtilityStorage() {
      if (this.entity == null) {
         return null;
      } else {
         Ref<EntityStore> ref = this.entity.getReference();
         return ref != null && ref.isValid() ? InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.ARMOR_HOTBAR_UTILITY_STORAGE) : null;
      }
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public CombinedItemContainer getCombinedHotbarUtilityConsumableStorage() {
      if (this.entity == null) {
         return null;
      } else {
         Ref<EntityStore> ref = this.entity.getReference();
         return ref != null && ref.isValid() ? InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.HOTBAR_UTILITY_CONSUMABLE_STORAGE) : null;
      }
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public CombinedItemContainer getCombinedStorageHotbarBackpack() {
      if (this.entity == null) {
         return null;
      } else {
         Ref<EntityStore> ref = this.entity.getReference();
         return ref != null && ref.isValid() ? InventoryComponent.getCombined(ref.getStore(), ref, InventoryComponent.STORAGE_HOTBAR_BACKPACK) : null;
      }
   }

   @Deprecated(forRemoval = true)
   public static void setActiveSlot(@Nonnull Ref<EntityStore> ref, int inventorySectionId, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      InventoryUtils.setActiveSlot(ref, inventorySectionId, slot, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   public static byte getActiveSlot(@Nonnull Ref<EntityStore> ref, int inventorySectionId, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return InventoryUtils.getActiveSlot(ref, inventorySectionId, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   public byte getActiveSlot(int inventorySectionId) {
      return switch (inventorySectionId) {
         case -8 -> this.tools.getActiveSlot();
         case -5 -> this.utility.getActiveSlot();
         case -1 -> this.hotbar.getActiveSlot();
         default -> throw new IllegalArgumentException("Inventory section with id " + inventorySectionId + " cannot select an active slot");
      };
   }

   @Deprecated(forRemoval = true)
   public byte getActiveHotbarSlot() {
      return this.hotbar.getActiveSlot();
   }

   @Deprecated(forRemoval = true)
   public void setActiveHotbarSlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      InventoryUtils.setActiveSlot(ref, -1, slot, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemStack getActiveHotbarItem() {
      return this.hotbar.getActiveItem();
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemStack getActiveToolItem() {
      return this.tools.getActiveItem();
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemStack getItemInHand() {
      return this.tools != null && this.tools.usingToolsItem ? this.getActiveToolItem() : this.getActiveHotbarItem();
   }

   @Deprecated(forRemoval = true)
   public byte getActiveUtilitySlot() {
      return this.utility.getActiveSlot();
   }

   @Deprecated(forRemoval = true)
   public void setActiveUtilitySlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      InventoryUtils.setActiveSlot(ref, -5, slot, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemStack getUtilityItem() {
      return this.utility.getActiveItem();
   }

   @Deprecated(forRemoval = true)
   public byte getActiveToolsSlot() {
      return this.tools.getActiveSlot();
   }

   @Deprecated(forRemoval = true)
   public void setActiveToolsSlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      InventoryUtils.setActiveSlot(ref, -8, slot, componentAccessor);
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemStack getToolsItem() {
      return this.tools != null ? this.tools.getActiveItem() : null;
   }

   @Deprecated(forRemoval = true)
   @Nullable
   public ItemContainer getSectionById(int id) {
      if (id >= 0) {
         return this.entity instanceof Player player && player.getWindowManager().getWindow(id) instanceof ItemContainerWindow itemContainerWindow
            ? itemContainerWindow.getItemContainer()
            : null;
      } else {
         return switch (id) {
            case -9 -> this.backpack.getInventory();
            case -8 -> this.tools.getInventory();
            default -> null;
            case -5 -> this.utility.getInventory();
            case -3 -> this.armor.getInventory();
            case -2 -> this.storage.getInventory();
            case -1 -> this.hotbar.getInventory();
         };
      }
   }

   @Deprecated(forRemoval = true)
   public void setEntity(@Nonnull LivingEntity entity) {
      this.entity = entity;
   }

   @Deprecated(forRemoval = true)
   public void sortStorage() {
      if (this.entity != null) {
         Ref<EntityStore> ref = this.entity.getReference();
         if (ref != null && ref.isValid()) {
            InventoryUtils.sortStorage(ref, ref.getStore());
         }
      }
   }

   @Deprecated(forRemoval = true)
   public static boolean containsBrokenItem(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
      return InventoryUtils.containsBrokenItem(ref, accessor);
   }

   public void migrateToComponents(Holder<EntityStore> holder) {
      if (this.deserializedStorage != null) {
         holder.putComponent(InventoryComponent.Storage.getComponentType(), new InventoryComponent.Storage(this.deserializedStorage));
         this.deserializedStorage = null;
      }

      if (this.deserializedArmor != null) {
         holder.putComponent(InventoryComponent.Armor.getComponentType(), new InventoryComponent.Armor(this.deserializedArmor));
         this.deserializedArmor = null;
      }

      if (this.deserializedHotbar != null) {
         holder.putComponent(
            InventoryComponent.Hotbar.getComponentType(), new InventoryComponent.Hotbar(this.deserializedHotbar, this.deserializedActiveHotbarSlot)
         );
         this.deserializedHotbar = null;
      }

      if (this.deserializedUtility != null) {
         holder.putComponent(
            InventoryComponent.Utility.getComponentType(), new InventoryComponent.Utility(this.deserializedUtility, this.deserializedActiveUtilitySlot)
         );
         this.deserializedUtility = null;
      }

      if (this.deserializedTools != null) {
         holder.putComponent(InventoryComponent.Tool.getComponentType(), new InventoryComponent.Tool(this.deserializedTools, this.deserializedActiveToolsSlot));
         this.deserializedTools = null;
      }

      if (this.deserializedBackpack != null) {
         holder.putComponent(InventoryComponent.Backpack.getComponentType(), new InventoryComponent.Backpack(this.deserializedBackpack));
         this.deserializedBackpack = null;
      }
   }

   public void backwardsCompatHook(Holder<EntityStore> holder) {
      this.storage = holder.getComponent(InventoryComponent.Storage.getComponentType());
      this.armor = holder.getComponent(InventoryComponent.Armor.getComponentType());
      this.hotbar = holder.getComponent(InventoryComponent.Hotbar.getComponentType());
      this.utility = holder.getComponent(InventoryComponent.Utility.getComponentType());
      this.tools = holder.getComponent(InventoryComponent.Tool.getComponentType());
      this.backpack = holder.getComponent(InventoryComponent.Backpack.getComponentType());
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Inventory inventory = (Inventory)o;
         if (!Objects.equals(this.storage, inventory.storage)) {
            return false;
         } else if (!Objects.equals(this.armor, inventory.armor)) {
            return false;
         } else if (!Objects.equals(this.utility, inventory.utility)) {
            return false;
         } else if (!Objects.equals(this.tools, inventory.tools)) {
            return false;
         } else {
            return !Objects.equals(this.backpack, inventory.backpack) ? false : Objects.equals(this.hotbar, inventory.hotbar);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.storage != null ? this.storage.hashCode() : 0;
      result = 31 * result + (this.armor != null ? this.armor.hashCode() : 0);
      result = 31 * result + (this.hotbar != null ? this.hotbar.hashCode() : 0);
      result = 31 * result + (this.utility != null ? this.utility.hashCode() : 0);
      result = 31 * result + (this.tools != null ? this.tools.hashCode() : 0);
      return 31 * result + (this.backpack != null ? this.backpack.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "Inventory{, storage=" + this.storage + ", armor=" + this.armor + ", hotbar=" + this.hotbar + ", utility=" + this.utility + "}";
   }

   @Deprecated(forRemoval = true)
   public void setUsingToolsItem(boolean value) {
      if (this.tools != null) {
         this.tools.setUsingToolsItem(value);
      }
   }

   @Deprecated(forRemoval = true)
   public boolean usingToolsItem() {
      return this.tools != null && this.tools.isUsingToolsItem();
   }
}
