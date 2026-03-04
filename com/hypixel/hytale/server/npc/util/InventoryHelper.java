package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
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
         if (item != null && item.hasBlockType()) {
            return true;
         }
      }

      return false;
   }

   public static boolean itemDropListKeyExists(@Nullable String name) {
      if (name != null && !name.isEmpty()) {
         ItemDropList dropList = ItemDropList.getAssetMap().getAsset(name);
         return dropList != null;
      } else {
         return false;
      }
   }

   public static byte findHotbarSlotWithItem(@Nonnull Inventory inventory, String name) {
      ItemContainer hotbar = inventory.getHotbar();

      for (byte i = 0; i < hotbar.getCapacity(); i++) {
         if (matchesItem(name, hotbar.getItemStack(i))) {
            return i;
         }
      }

      return -1;
   }

   public static short findHotbarSlotWithItem(@Nonnull Inventory inventory, List<String> name) {
      ItemContainer hotbar = inventory.getHotbar();

      for (short i = 0; i < hotbar.getCapacity(); i++) {
         if (matchesItem(name, hotbar.getItemStack(i))) {
            return i;
         }
      }

      return -1;
   }

   public static byte findHotbarEmptySlot(@Nonnull Inventory inventory) {
      ItemContainer hotbar = inventory.getHotbar();

      for (byte i = 0; i < hotbar.getCapacity(); i++) {
         if (ItemStack.isEmpty(hotbar.getItemStack(i))) {
            return i;
         }
      }

      return -1;
   }

   public static short findInventorySlotWithItem(@Nonnull Inventory inventory, String name) {
      CombinedItemContainer container = inventory.getCombinedHotbarFirst();

      for (short i = 0; i < container.getCapacity(); i++) {
         if (matchesItem(name, container.getItemStack(i))) {
            return i;
         }
      }

      return -1;
   }

   public static short findInventorySlotWithItem(@Nonnull Inventory inventory, List<String> name) {
      CombinedItemContainer container = inventory.getCombinedHotbarFirst();

      for (short i = 0; i < container.getCapacity(); i++) {
         if (matchesItem(name, container.getItemStack(i))) {
            return i;
         }
      }

      return -1;
   }

   public static int countItems(@Nonnull ItemContainer container, List<String> name) {
      int count = 0;

      for (short i = 0; i < container.getCapacity(); i++) {
         ItemStack item = container.getItemStack(i);
         if (matchesItem(name, item)) {
            count += item.getQuantity();
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

   public static boolean hotbarContainsItem(@Nonnull Inventory inventory, String name) {
      return findHotbarSlotWithItem(inventory, name) != -1;
   }

   public static boolean hotbarContainsItem(@Nonnull Inventory inventory, List<String> name) {
      return findHotbarSlotWithItem(inventory, name) != -1;
   }

   public static boolean holdsItem(@Nonnull Inventory inventory, String name) {
      return matchesItem(name, inventory.getItemInHand());
   }

   public static boolean containsItem(@Nonnull Inventory inventory, String name) {
      return findInventorySlotWithItem(inventory, name) != -1;
   }

   public static boolean containsItem(@Nonnull Inventory inventory, List<String> name) {
      return findInventorySlotWithItem(inventory, name) != -1;
   }

   public static boolean clearItemInHand(@Nonnull Inventory inventory, byte slotHint) {
      if (ItemStack.isEmpty(inventory.getItemInHand())) {
         return true;
      } else {
         byte slot = findHotbarEmptySlot(inventory);
         if (slot >= 0) {
            inventory.setActiveHotbarSlot(slot);
            return true;
         } else {
            slot = slotHint != -1 ? slotHint : 0;
            inventory.getHotbar().removeItemStackFromSlot(slot);
            inventory.setActiveHotbarSlot(slot);
            return true;
         }
      }
   }

   public static void removeItemInHand(@Nonnull Inventory inventory, int count) {
      if (!ItemStack.isEmpty(inventory.getItemInHand())) {
         byte activeHotbarSlot = inventory.getActiveHotbarSlot();
         if (activeHotbarSlot != -1) {
            inventory.getHotbar().removeItemStackFromSlot(activeHotbarSlot, count);
         }
      }
   }

   public static boolean checkHotbarSlot(@Nonnull Inventory inventory, byte slot) {
      ItemContainer hotbar = inventory.getHotbar();
      if (slot < hotbar.getCapacity() && slot >= 0) {
         return true;
      } else {
         NPCPlugin.get().getLogger().at(Level.WARNING).log("Invalid hotbar slot %s. Max is %s", slot, hotbar.getCapacity() - 1);
         return false;
      }
   }

   public static boolean checkOffHandSlot(@Nonnull Inventory inventory, byte slot) {
      ItemContainer utility = inventory.getUtility();
      if (slot < utility.getCapacity() && slot >= -1) {
         return true;
      } else {
         NPCPlugin.get().getLogger().at(Level.WARNING).log("Invalid utility slot %s. Max is %s, Min is %s", slot, utility.getCapacity() - 1, -1);
         return false;
      }
   }

   public static void setHotbarSlot(@Nonnull Inventory inventory, byte slot) {
      if (inventory.getActiveHotbarSlot() != slot) {
         if (checkHotbarSlot(inventory, slot)) {
            inventory.setActiveHotbarSlot(slot);
         }
      }
   }

   public static void setOffHandSlot(@Nonnull Inventory inventory, byte slot) {
      if (inventory.getActiveUtilitySlot() != slot) {
         if (checkOffHandSlot(inventory, slot)) {
            inventory.setActiveUtilitySlot(slot);
         }
      }
   }

   public static boolean setHotbarItem(@Nonnull Inventory inventory, @Nullable String name, byte slot) {
      if (name != null && !name.isEmpty() && itemKeyExists(name)) {
         ItemContainer hotbar = inventory.getHotbar();
         if (!checkHotbarSlot(inventory, slot)) {
            return false;
         } else if (matchesItem(name, hotbar.getItemStack(slot))) {
            return true;
         } else {
            hotbar.setItemStackForSlot(slot, createItem(name));
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean setOffHandItem(@Nonnull Inventory inventory, @Nullable String name, byte slot) {
      if (name != null && !name.isEmpty() && itemKeyExists(name)) {
         ItemContainer utility = inventory.getUtility();
         if (!checkOffHandSlot(inventory, slot)) {
            return false;
         } else if (matchesItem(name, utility.getItemStack(slot))) {
            return true;
         } else {
            utility.setItemStackForSlot(slot, createItem(name));
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean useItem(@Nonnull Inventory inventory, @Nullable String name, byte slotHint) {
      if (name == null || name.isEmpty() || !itemKeyExists(name)) {
         return false;
      } else if (holdsItem(inventory, name)) {
         return true;
      } else {
         byte slot = findHotbarSlotWithItem(inventory, name);
         if (slot >= 0) {
            inventory.setActiveHotbarSlot(slot);
            return true;
         } else {
            if (slotHint == -1) {
               slotHint = findHotbarEmptySlot(inventory);
            }

            if (slotHint == -1) {
               slotHint = 0;
            }

            inventory.getHotbar().setItemStackForSlot(slotHint, createItem(name));
            inventory.setActiveHotbarSlot(slotHint);
            return true;
         }
      }
   }

   @Nullable
   public static ItemStack createItem(@Nullable String name) {
      return !itemKeyExists(name) ? null : new ItemStack(name, 1);
   }

   public static boolean useItem(@Nonnull Inventory inventory, @Nullable String name) {
      return name != null && !name.isEmpty() ? useItem(inventory, name, (byte)-1) : clearItemInHand(inventory, (byte)-1);
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
