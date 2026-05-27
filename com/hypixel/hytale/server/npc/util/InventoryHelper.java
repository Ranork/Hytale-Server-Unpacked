package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InventoryHelper {
   public static final short DEFAULT_NPC_HOTBAR_SLOTS = 3;
   public static final short MAX_NPC_HOTBAR_SLOTS = 8;
   public static final short DEFAULT_NPC_INVENTORY_SLOTS = 0;
   public static final short DEFAULT_NPC_UTILITY_SLOTS = 0;
   public static final short MAX_NPC_UTILITY_SLOTS = 4;
   public static final short DEFAULT_NPC_TOOL_SLOTS = 0;
   public static final short MAX_NPC_INVENTORY_SLOTS = 36;

   private InventoryHelper() {
   }

   public static boolean matchesItem(@Nullable String pattern, @Nonnull ItemStack itemStack) {
      return pattern != null && !pattern.isEmpty() && !ItemStack.isEmpty(itemStack) ? StringUtil.isGlobMatching(pattern, itemStack.getItem().getId()) : false;
   }

   public static boolean matchesItem(@Nullable List<String> patterns, @Nonnull ItemStack itemStack) {
      return patterns != null && !patterns.isEmpty() && !ItemStack.isEmpty(itemStack) ? matchesPatterns(patterns, itemStack.getItem().getId()) : false;
   }

   protected static boolean matchesPatterns(@Nonnull List<String> patterns, @Nullable String name) {
      if (name != null && !name.isEmpty()) {
         for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            if (pattern != null && !pattern.isEmpty() && StringUtil.isGlobMatching(pattern, name)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean itemKeyExists(@Nullable String name) {
      return name != null && !name.isEmpty() ? ItemModule.exists(name) : false;
   }

   public static boolean itemKeyIsBlockType(@Nullable String name) {
      if (name != null && !name.isEmpty()) {
         Item item = Item.getAssetMap().getAsset(name);
         return item != null && item.hasBlockType();
      } else {
         return false;
      }
   }

   public static boolean itemDropListKeyExists(@Nullable String name) {
      if (name != null && !name.isEmpty()) {
         ItemDropList dropList = ItemDropList.getAssetMap().getAsset(name);
         return dropList != null;
      } else {
         return false;
      }
   }

   public static byte findHotbarSlotWithItem(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull String name) {
      InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
      if (hotbarComponent == null) {
         return -1;
      } else {
         ItemContainer hotbarContainer = hotbarComponent.getInventory();

         for (byte i = 0; i < hotbarContainer.getCapacity(); i++) {
            ItemStack itemStack = hotbarContainer.getItemStack(i);
            if (itemStack != null && matchesItem(name, itemStack)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static short findHotbarSlotWithItem(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull List<String> name
   ) {
      InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
      if (hotbarComponent == null) {
         return -1;
      } else {
         ItemContainer hotbarContainer = hotbarComponent.getInventory();

         for (short i = 0; i < hotbarContainer.getCapacity(); i++) {
            ItemStack itemStack = hotbarContainer.getItemStack(i);
            if (itemStack != null && matchesItem(name, itemStack)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static byte findHotbarEmptySlot(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
      if (hotbarComponent == null) {
         return -1;
      } else {
         ItemContainer hotbarContainer = hotbarComponent.getInventory();

         for (byte i = 0; i < hotbarContainer.getCapacity(); i++) {
            if (ItemStack.isEmpty(hotbarContainer.getItemStack(i))) {
               return i;
            }
         }

         return -1;
      }
   }

   public static short findInventorySlotWithItem(@Nonnull Ref<EntityStore> ref, @Nonnull String name, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      CombinedItemContainer combinedContainer = InventoryComponent.getCombined(componentAccessor, ref, InventoryComponent.HOTBAR_FIRST);

      for (short i = 0; i < combinedContainer.getCapacity(); i++) {
         ItemStack itemStack = combinedContainer.getItemStack(i);
         if (itemStack != null && matchesItem(name, itemStack)) {
            return i;
         }
      }

      return -1;
   }

   public static short findInventorySlotWithItem(
      @Nonnull Ref<EntityStore> ref, @Nonnull List<String> name, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      CombinedItemContainer combinedContainer = InventoryComponent.getCombined(componentAccessor, ref, InventoryComponent.HOTBAR_FIRST);

      for (short i = 0; i < combinedContainer.getCapacity(); i++) {
         ItemStack itemStack = combinedContainer.getItemStack(i);
         if (itemStack != null && matchesItem(name, itemStack)) {
            return i;
         }
      }

      return -1;
   }

   public static int countItems(@Nonnull ItemContainer container, @Nonnull List<String> name) {
      int count = 0;

      for (short i = 0; i < container.getCapacity(); i++) {
         ItemStack itemStack = container.getItemStack(i);
         if (itemStack != null && matchesItem(name, itemStack)) {
            count += itemStack.getQuantity();
         }
      }

      return count;
   }

   public static int countFreeSlots(@Nonnull ItemContainer container) {
      int count = 0;

      for (short i = 0; i < container.getCapacity(); i++) {
         ItemStack item = container.getItemStack(i);
         if (item == null || item.isEmpty()) {
            count++;
         }
      }

      return count;
   }

   public static boolean hotbarContainsItem(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull String name) {
      return findHotbarSlotWithItem(ref, componentAccessor, name) != -1;
   }

   public static boolean hotbarContainsItem(
      @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull List<String> name
   ) {
      return findHotbarSlotWithItem(ref, componentAccessor, name) != -1;
   }

   public static boolean holdsItem(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull String name) {
      return matchesItem(name, InventoryComponent.getItemInHand(componentAccessor, ref));
   }

   public static boolean containsItem(@Nonnull Ref<EntityStore> ref, @Nonnull String name, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return findInventorySlotWithItem(ref, name, componentAccessor) != -1;
   }

   public static boolean containsItem(@Nonnull Ref<EntityStore> ref, @Nonnull List<String> name, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return findInventorySlotWithItem(ref, name, componentAccessor) != -1;
   }

   public static boolean clearItemInHand(@Nonnull Ref<EntityStore> ref, byte slotHint, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (ItemStack.isEmpty(InventoryComponent.getItemInHand(componentAccessor, ref))) {
         return true;
      } else {
         byte slot = findHotbarEmptySlot(ref, componentAccessor);
         if (slot >= 0) {
            InventoryUtils.setActiveSlot(ref, -1, slot, componentAccessor);
            return true;
         } else {
            slot = slotHint != -1 ? slotHint : 0;
            InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
            if (hotbarComponent != null) {
               hotbarComponent.getInventory().removeItemStackFromSlot(slot);
            }

            InventoryUtils.setActiveSlot(ref, -1, slot, componentAccessor);
            return true;
         }
      }
   }

   public static void removeItemInHand(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, int count) {
      if (!ItemStack.isEmpty(InventoryComponent.getItemInHand(componentAccessor, ref))) {
         InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
         if (hotbarComponent != null) {
            byte activeHotbarSlot = hotbarComponent.getActiveSlot();
            if (activeHotbarSlot != -1) {
               hotbarComponent.getInventory().removeItemStackFromSlot(activeHotbarSlot, count);
            }
         }
      }
   }

   public static boolean checkHotbarSlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
      if (hotbarComponent == null) {
         return false;
      } else if (slot < hotbarComponent.getInventory().getCapacity() && slot >= 0) {
         return true;
      } else {
         NPCPlugin.get().getLogger().at(Level.WARNING).log("Invalid hotbar slot %s. Max is %s", slot, hotbarComponent.getInventory().getCapacity() - 1);
         return false;
      }
   }

   public static boolean checkOffHandSlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      InventoryComponent.Utility utilityComponent = componentAccessor.getComponent(ref, InventoryComponent.Utility.getComponentType());
      if (utilityComponent == null) {
         return false;
      } else if (slot < utilityComponent.getInventory().getCapacity() && slot >= -1) {
         return true;
      } else {
         NPCPlugin.get()
            .getLogger()
            .at(Level.WARNING)
            .log("Invalid utility slot %s. Max is %s, Min is %s", slot, utilityComponent.getInventory().getCapacity() - 1, -1);
         return false;
      }
   }

   public static void setHotbarSlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (InventoryUtils.getActiveSlot(ref, -1, componentAccessor) != slot) {
         if (checkHotbarSlot(ref, slot, componentAccessor)) {
            InventoryUtils.setActiveSlot(ref, -1, slot, componentAccessor);
         }
      }
   }

   public static void setOffHandSlot(@Nonnull Ref<EntityStore> ref, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (InventoryUtils.getActiveSlot(ref, -5, componentAccessor) != slot) {
         if (checkOffHandSlot(ref, slot, componentAccessor)) {
            InventoryComponent.Utility utilityComponent = componentAccessor.getComponent(ref, InventoryComponent.Utility.getComponentType());
            if (utilityComponent != null) {
               utilityComponent.setActiveSlot(slot, ref, componentAccessor);
            }
         }
      }
   }

   public static boolean setHotbarItem(
      @Nonnull Ref<EntityStore> ref, @Nullable String name, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (name != null && !name.isEmpty() && itemKeyExists(name)) {
         InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
         if (hotbarComponent != null && checkHotbarSlot(ref, slot, componentAccessor)) {
            ItemContainer hotbarContainer = hotbarComponent.getInventory();
            if (matchesItem(name, hotbarContainer.getItemStack(slot))) {
               return true;
            } else {
               hotbarContainer.setItemStackForSlot(slot, createItem(name));
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean setOffHandItem(
      @Nonnull Ref<EntityStore> ref, @Nullable String name, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (name != null && !name.isEmpty() && itemKeyExists(name)) {
         InventoryComponent.Utility utilityComponent = componentAccessor.getComponent(ref, InventoryComponent.Utility.getComponentType());
         if (utilityComponent != null && checkOffHandSlot(ref, slot, componentAccessor)) {
            ItemContainer utilityContainer = utilityComponent.getInventory();
            if (matchesItem(name, utilityContainer.getItemStack(slot))) {
               return true;
            } else {
               utilityContainer.setItemStackForSlot(slot, createItem(name));
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean useItem(@Nonnull Ref<EntityStore> ref, @Nullable String name, byte slotHint, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (name == null || name.isEmpty() || !itemKeyExists(name)) {
         return false;
      } else if (holdsItem(ref, componentAccessor, name)) {
         return true;
      } else {
         byte slot = findHotbarSlotWithItem(ref, componentAccessor, name);
         if (slot >= 0) {
            InventoryUtils.setActiveSlot(ref, -1, slot, componentAccessor);
            return true;
         } else {
            if (slotHint == -1) {
               slotHint = findHotbarEmptySlot(ref, componentAccessor);
            }

            if (slotHint == -1) {
               slotHint = 0;
            }

            InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
            if (hotbarComponent != null) {
               hotbarComponent.getInventory().setItemStackForSlot(slotHint, createItem(name));
               hotbarComponent.setActiveSlot(slotHint, ref, componentAccessor);
            }

            return true;
         }
      }
   }

   @Nullable
   public static ItemStack createItem(@Nullable String name) {
      return !itemKeyExists(name) ? null : new ItemStack(name, 1);
   }

   public static boolean useItem(@Nonnull Ref<EntityStore> ref, @Nullable String name, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return name != null && !name.isEmpty() ? useItem(ref, name, (byte)-1, componentAccessor) : clearItemInHand(ref, (byte)-1, componentAccessor);
   }

   public static boolean useArmor(@Nonnull ItemContainer armorInventory, @Nullable String armorItem) {
      ItemStack itemStack = createItem(armorItem);
      return useArmor(armorInventory, itemStack);
   }

   public static boolean useArmor(@Nonnull ItemContainer armorInventory, @Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else {
         Item item = itemStack.getItem();
         if (item == null) {
            return false;
         } else {
            ItemArmor armor = item.getArmor();
            if (armor == null) {
               return false;
            } else {
               short slot = (short)armor.getArmorSlot().ordinal();
               return slot >= 0 && slot <= armorInventory.getCapacity() ? armorInventory.setItemStackForSlot(slot, itemStack).succeeded() : false;
            }
         }
      }
   }
}
