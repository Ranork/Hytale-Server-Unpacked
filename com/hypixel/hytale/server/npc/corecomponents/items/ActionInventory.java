package com.hypixel.hytale.server.npc.corecomponents.items;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.items.builders.BuilderActionInventory;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import java.util.EnumSet;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionInventory extends ActionBase {
   @Nonnull
   private static final EnumSet<ActionInventory.Operation> ITEM_FREE_OPERATIONS = EnumSet.of(
      ActionInventory.Operation.ClearHeldItem,
      ActionInventory.Operation.RemoveHeldItem,
      ActionInventory.Operation.EquipHotbar,
      ActionInventory.Operation.EquipOffHand
   );
   protected final ActionInventory.Operation operation;
   protected final String item;
   protected final int count;
   protected final boolean useTarget;
   protected final byte slot;

   public ActionInventory(@Nonnull BuilderActionInventory builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.operation = builder.getOperation(support);
      this.count = builder.getCount(support);
      this.item = builder.getItem(support);
      this.useTarget = builder.getUseTarget(support);
      this.slot = (byte)builder.getSlot(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return !super.canExecute(ref, role, sensorInfo, dt, store)
         ? false
         : (!this.useTarget || sensorInfo != null && sensorInfo.hasPosition())
            && (ITEM_FREE_OPERATIONS.contains(this.operation) || this.item != null && !this.item.isEmpty());
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      IPositionProvider positionProvider = sensorInfo != null ? sensorInfo.getPositionProvider() : null;
      Ref<EntityStore> targetRef = this.useTarget ? (positionProvider != null ? positionProvider.getTarget() : null) : ref;
      if (targetRef == null) {
         return false;
      } else if (this.operation == ActionInventory.Operation.ClearHeldItem) {
         InventoryHelper.clearItemInHand(targetRef, (byte)-1, store);
         return true;
      } else if (this.operation == ActionInventory.Operation.RemoveHeldItem) {
         InventoryHelper.removeItemInHand(targetRef, store, this.count);
         return true;
      } else if (this.operation != ActionInventory.Operation.EquipHotbar || this.item != null && !this.item.isEmpty()) {
         if (this.operation != ActionInventory.Operation.EquipOffHand || this.item != null && !this.item.isEmpty()) {
            String itemStackKey = this.item;
            if (itemStackKey != null && !"Empty".equals(itemStackKey) && !"Unknown".equals(itemStackKey) && ItemModule.exists(itemStackKey)) {
               CombinedItemContainer combinedStorage = InventoryComponent.getCombined(store, targetRef, InventoryComponent.HOTBAR_FIRST);
               ItemStack itemStack = new ItemStack(itemStackKey, this.count);
               switch (this.operation) {
                  case Add:
                     if (this.count > 0) {
                        combinedStorage.addItemStack(itemStack);
                     }
                     break;
                  case Remove:
                     if (this.count > 0) {
                        combinedStorage.removeItemStack(itemStack);
                     }
                     break;
                  case Equip:
                     Item item = itemStack.getItem();
                     if (item.getArmor() != null) {
                        InventoryComponent.Armor armorComponent = store.getComponent(targetRef, InventoryComponent.Armor.getComponentType());
                        if (armorComponent != null) {
                           InventoryHelper.useArmor(armorComponent.getInventory(), itemStack);
                        }
                     } else {
                        InventoryHelper.useItem(targetRef, item.getId(), store);
                     }
                  case ClearHeldItem:
                  case RemoveHeldItem:
                  default:
                     break;
                  case SetHotbar:
                     if (InventoryHelper.checkHotbarSlot(targetRef, this.slot, store)) {
                        InventoryComponent.Hotbar hotbarComponent = store.getComponent(targetRef, InventoryComponent.Hotbar.getComponentType());
                        if (hotbarComponent != null) {
                           hotbarComponent.getInventory().setItemStackForSlot(this.slot, itemStack);
                        }
                     }
                     break;
                  case EquipHotbar:
                     if (InventoryHelper.checkHotbarSlot(targetRef, this.slot, store)) {
                        InventoryComponent.Hotbar hotbarComponent = store.getComponent(targetRef, InventoryComponent.Hotbar.getComponentType());
                        if (hotbarComponent != null) {
                           hotbarComponent.getInventory().setItemStackForSlot(this.slot, itemStack);
                           if (hotbarComponent.getActiveSlot() != this.slot) {
                              hotbarComponent.setActiveSlot(this.slot, targetRef, store);
                           }
                        }
                     }
                     break;
                  case SetOffHand:
                     if (InventoryHelper.checkOffHandSlot(targetRef, this.slot, store)) {
                        InventoryComponent.Utility utilityComponent = store.getComponent(targetRef, InventoryComponent.Utility.getComponentType());
                        if (utilityComponent != null) {
                           utilityComponent.getInventory().setItemStackForSlot(this.slot, itemStack);
                        }
                     }
                     break;
                  case EquipOffHand:
                     if (InventoryHelper.checkOffHandSlot(targetRef, this.slot, store)) {
                        InventoryComponent.Utility utilityComponent = store.getComponent(targetRef, InventoryComponent.Utility.getComponentType());
                        if (utilityComponent != null) {
                           utilityComponent.getInventory().setItemStackForSlot(this.slot, itemStack);
                        }
                     }

                     InventoryHelper.setOffHandSlot(targetRef, this.slot, store);
               }

               return true;
            } else {
               NPCPlugin.get().getLogger().at(Level.WARNING).log("Unknown item %s in Inventory action", this.item);
               return true;
            }
         } else {
            InventoryHelper.setOffHandSlot(targetRef, this.slot, store);
            return true;
         }
      } else if (InventoryUtils.getActiveSlot(targetRef, -1, store) == this.slot) {
         return true;
      } else {
         if (InventoryHelper.checkHotbarSlot(targetRef, this.slot, store)) {
            InventoryUtils.setActiveSlot(targetRef, -1, this.slot, store);
         }

         return true;
      }
   }

   public static enum Operation implements Supplier<String> {
      Add("Add items to inventory"),
      Remove("Remove items from inventory"),
      Equip("Equip item as weapon or armour"),
      ClearHeldItem("Clear the held item"),
      RemoveHeldItem("Destroy the held item"),
      SetHotbar("Sets the hotbar item in a specific slot"),
      EquipHotbar("Equips the item from a specific hotbar slot"),
      SetOffHand("Sets the off-hand item in a specific slot"),
      EquipOffHand("Equips the item from a specific off-hand slot");

      private final String description;

      private Operation(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
