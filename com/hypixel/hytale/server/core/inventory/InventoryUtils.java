package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.EquipmentUpdate;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ItemArmorSlot;
import com.hypixel.hytale.protocol.PickupLocation;
import com.hypixel.hytale.protocol.SmartMoveType;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.gameplay.PlayerConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ItemContainerWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SortType;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ChangeActiveSlotInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InventoryUtils {
   @Nonnull
   private static ItemContainer getItemContainerForPickupLocation(
      @Nonnull PickupLocation pickupLocation, @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return switch (pickupLocation) {
         case Hotbar -> InventoryComponent.getCombined(componentAccessor, ref, InventoryComponent.HOTBAR_STORAGE_BACKPACK);
         case Storage -> InventoryComponent.getCombined(componentAccessor, ref, InventoryComponent.STORAGE_HOTBAR_BACKPACK);
         case Backpack -> InventoryComponent.getCombined(componentAccessor, ref, InventoryComponent.BACKPACK_STORAGE_HOTBAR);
      };
   }

   @Nonnull
   public static ItemContainer getContainerForItemPickup(
      @Nonnull Ref<EntityStore> ref, @Nonnull Item item, PlayerSettings playerSettings, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return getContainerForItemPickup(ref, item, playerSettings, null, componentAccessor);
   }

   @Nonnull
   public static ItemContainer getContainerForItemPickup(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Item item,
      PlayerSettings playerSettings,
      @Nullable PickupLocation overridePickupLocation,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (overridePickupLocation != null) {
         return getItemContainerForPickupLocation(overridePickupLocation, ref, componentAccessor);
      } else if (item.getArmor() != null) {
         return getItemContainerForPickupLocation(playerSettings.armorItemsPreferredPickupLocation(), ref, componentAccessor);
      } else if (item.getWeapon() != null || item.getTool() != null) {
         return getItemContainerForPickupLocation(playerSettings.weaponAndToolItemsPreferredPickupLocation(), ref, componentAccessor);
      } else if (item.getUtility().isUsable()) {
         return getItemContainerForPickupLocation(playerSettings.usableItemsItemsPreferredPickupLocation(), ref, componentAccessor);
      } else {
         BlockType blockType = item.hasBlockType() ? BlockType.getAssetMap().getAsset(item.getBlockId()) : BlockType.EMPTY;
         if (blockType == null) {
            blockType = BlockType.EMPTY;
         }

         return blockType.getMaterial() == BlockMaterial.Solid
            ? getItemContainerForPickupLocation(playerSettings.solidBlockItemsPreferredPickupLocation(), ref, componentAccessor)
            : getItemContainerForPickupLocation(playerSettings.miscItemsPreferredPickupLocation(), ref, componentAccessor);
      }
   }

   public static void smartMoveItem(
      @Nonnull Ref<EntityStore> ref,
      int fromSectionId,
      int fromSlotId,
      int quantity,
      @Nonnull SmartMoveType moveType,
      PlayerSettings settings,
      @Nonnull ComponentAccessor<EntityStore> accessor
   ) {
      ItemContainer targetContainer = getSectionById(ref, fromSectionId, accessor);
      if (targetContainer != null) {
         ItemStack itemStack = targetContainer.getItemStack((short)fromSlotId);
         if (!ItemStack.isEmpty(itemStack)) {
            switch (moveType) {
               case EquipOrMergeStack:
                  if (tryEquipArmorPart(ref, itemStack, fromSectionId, (short)fromSlotId, quantity, targetContainer, true, accessor)) {
                     return;
                  }

                  Player playerComponent = accessor.getComponent(ref, Player.getComponentType());
                  if (playerComponent != null) {
                     for (Window window : playerComponent.getWindowManager().getWindows()) {
                        if (window instanceof ItemContainerWindow itemContainerWindow) {
                           itemContainerWindow.getItemContainer().combineItemStacksIntoSlot(targetContainer, (short)fromSlotId);
                        }
                     }
                  }

                  CombinedItemContainer everythingInventoryComponent = InventoryComponent.getCombined(accessor, ref, InventoryComponent.EVERYTHING);
                  everythingInventoryComponent.combineItemStacksIntoSlot(targetContainer, (short)fromSlotId);
                  break;
               case PutInHotbarOrWindow:
                  if (fromSectionId >= 0) {
                     moveItemFromCheckToInventory(ref, itemStack, targetContainer, (short)fromSlotId, quantity, settings, accessor);
                     return;
                  }

                  Player playerComponent = accessor.getComponent(ref, Player.getComponentType());
                  if (playerComponent != null) {
                     for (Window windowx : playerComponent.getWindowManager().getWindows()) {
                        if (windowx instanceof ItemContainerWindow itemContainerWindow) {
                           ItemContainer itemContainer = itemContainerWindow.getItemContainer();
                           MoveTransaction<ItemStackTransaction> transaction = targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, itemContainer);
                           ItemStack remainder = transaction.getAddTransaction().getRemainder();
                           if (ItemStack.isEmpty(remainder) || remainder.getQuantity() != quantity) {
                              return;
                           }
                        }
                     }
                  }

                  if (tryEquipArmorPart(ref, itemStack, fromSectionId, (short)fromSlotId, quantity, targetContainer, false, accessor)) {
                     return;
                  }

                  switch (fromSectionId) {
                     case -2:
                        InventoryComponent.Hotbar hotbarComponent = accessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
                        if (hotbarComponent != null) {
                           targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, hotbarComponent.getInventory());
                        }

                        return;
                     case -1:
                        InventoryComponent.Storage storageComponent = accessor.getComponent(ref, InventoryComponent.Storage.getComponentType());
                        if (storageComponent != null) {
                           targetContainer.moveItemStackFromSlot((short)fromSlotId, quantity, storageComponent.getInventory());
                        }

                        return;
                     default:
                        moveItemFromCheckToInventory(ref, itemStack, targetContainer, (short)fromSlotId, quantity, settings, accessor);
                        return;
                  }
               case PutInHotbarOrBackpack:
                  if (fromSectionId == -9) {
                     moveItemFromCheckToInventory(ref, itemStack, targetContainer, (short)fromSlotId, quantity, settings, accessor);
                  } else {
                     targetContainer.moveItemStackFromSlot(
                        (short)fromSlotId, quantity, getContainerForItemPickup(ref, itemStack.getItem(), settings, PickupLocation.Backpack, accessor)
                     );
                  }
            }
         }
      }
   }

   private static boolean tryEquipArmorPart(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ItemStack itemStack,
      int fromSectionId,
      short fromSlotId,
      int quantity,
      @Nonnull ItemContainer targetContainer,
      boolean forceEquip,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      InventoryComponent.Armor armorComponent = componentAccessor.getComponent(ref, InventoryComponent.Armor.getComponentType());
      if (armorComponent == null) {
         return false;
      } else {
         Item item = itemStack.getItem();
         ItemArmor itemArmor = item.getArmor();
         ItemContainer armorInventory = armorComponent.getInventory();
         if (itemArmor == null || fromSectionId == -3 || !forceEquip && armorInventory.getItemStack((short)itemArmor.getArmorSlot().ordinal()) != null) {
            return false;
         } else {
            targetContainer.moveItemStackFromSlotToSlot(fromSlotId, quantity, armorInventory, (short)itemArmor.getArmorSlot().ordinal());
            return true;
         }
      }
   }

   public static void moveItem(
      @Nonnull Ref<EntityStore> ref,
      int fromSectionId,
      int fromSlotId,
      int quantity,
      int toSectionId,
      int toSlotId,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      ItemContainer fromContainer = getSectionById(ref, fromSectionId, componentAccessor);
      if (fromContainer != null) {
         ItemContainer toContainer = getSectionById(ref, toSectionId, componentAccessor);
         if (toContainer != null) {
            InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
            Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
            if (playerComponent != null
               && hotbarComponent != null
               && (toSectionId == -1 && hotbarComponent.getActiveSlot() == toSlotId || fromSectionId == -1 && hotbarComponent.getActiveSlot() == fromSlotId)) {
               ItemStack fromItem = fromContainer.getItemStack((short)fromSlotId);
               ItemStack currentItem = toContainer.getItemStack((short)toSlotId);
               if (currentItem == null || ItemStack.isStackableWith(fromItem, currentItem) || ItemStack.isSameItemType(fromItem, currentItem)) {
                  fromContainer.moveItemStackFromSlotToSlot((short)fromSlotId, quantity, toContainer, (short)toSlotId);
                  return;
               }

               int interactionSlot = toSectionId == -1 && hotbarComponent.getActiveSlot() == toSlotId ? toSlotId : hotbarComponent.getActiveSlot();
               InteractionManager interactionManagerComponent = componentAccessor.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
               if (interactionManagerComponent != null) {
                  PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, InteractionType.SwapFrom, componentAccessor);
                  context.getMetaStore().putMetaObject(Interaction.TARGET_SLOT, interactionSlot);
                  context.getMetaStore()
                     .putMetaObject(
                        ChangeActiveSlotInteraction.PLACE_MOVED_ITEM,
                        () -> {
                           fromContainer.moveItemStackFromSlotToSlot((short)fromSlotId, quantity, toContainer, (short)toSlotId);
                           if (ref.isValid()) {
                              InventoryComponent.Storage storage = componentAccessor.getComponent(ref, InventoryComponent.Storage.getComponentType());
                              InventoryComponent.Armor armor = componentAccessor.getComponent(ref, InventoryComponent.Armor.getComponentType());
                              InventoryComponent.Hotbar hotbar = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
                              InventoryComponent.Utility utility = componentAccessor.getComponent(ref, InventoryComponent.Utility.getComponentType());
                              InventoryComponent.Tool tool = componentAccessor.getComponent(ref, InventoryComponent.Tool.getComponentType());
                              InventoryComponent.Backpack backpack = componentAccessor.getComponent(ref, InventoryComponent.Backpack.getComponentType());
                              playerRefComponent.getPacketHandler()
                                 .writeNoCache(
                                    new UpdatePlayerInventory(
                                       storage != null ? storage.getInventory().toPacket() : null,
                                       armor != null ? armor.getInventory().toPacket() : null,
                                       hotbar != null ? hotbar.getInventory().toPacket() : null,
                                       utility != null ? utility.getInventory().toPacket() : null,
                                       tool != null ? tool.getInventory().toPacket() : null,
                                       backpack != null ? backpack.getInventory().toPacket() : null
                                    )
                                 );
                           }
                        }
                     );
                  String interactions = context.getRootInteractionId(InteractionType.SwapFrom);
                  InteractionChainData data = new InteractionChainData(-1, UUIDUtil.EMPTY_UUID, null, null, null, -interactionSlot - 1, null);
                  InteractionChain chain = interactionManagerComponent.initChain(
                     data, InteractionType.SwapFrom, context, RootInteraction.getRootInteractionOrUnknown(interactions), null, false
                  );
                  interactionManagerComponent.queueExecuteChain(chain);
                  return;
               }
            }

            fromContainer.moveItemStackFromSlotToSlot((short)fromSlotId, quantity, toContainer, (short)toSlotId);
         }
      }
   }

   @Nonnull
   public static MoveTransaction<ItemStackTransaction> moveItemFromCheckToInventory(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ItemStack itemStack,
      @Nonnull ItemContainer targetContainer,
      short fromSlotId,
      int quantity,
      PlayerSettings settings,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return targetContainer.moveItemStackFromSlot(fromSlotId, quantity, getContainerForItemPickup(ref, itemStack.getItem(), settings, componentAccessor));
   }

   @Nullable
   public static ListTransaction<MoveTransaction<ItemStackTransaction>> takeAll(
      @Nonnull Ref<EntityStore> ref, int inventorySectionId, PlayerSettings settings, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      ItemContainer container = getSectionById(ref, inventorySectionId, componentAccessor);
      return container == null ? null : takeAllWithPriority(ref, container, settings, componentAccessor);
   }

   @Nonnull
   public static ListTransaction<MoveTransaction<ItemStackTransaction>> takeAllWithPriority(
      @Nonnull Ref<EntityStore> ref, ItemContainer fromContainer, PlayerSettings settings, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      List<MoveTransaction<ItemStackTransaction>> transactions = new ObjectArrayList();

      for (int slot = 0; slot < fromContainer.getCapacity(); slot++) {
         ItemStack stack = fromContainer.getItemStack((short)slot);
         if (!ItemStack.isEmpty(stack)) {
            transactions.add(moveItemFromCheckToInventory(ref, stack, fromContainer, (short)slot, stack.getQuantity(), settings, componentAccessor));
         }
      }

      return new ListTransaction<>(true, transactions);
   }

   @Nullable
   public static ListTransaction<MoveTransaction<ItemStackTransaction>> putAll(
      @Nonnull Ref<EntityStore> ref, int inventorySectionId, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      ItemContainer sectionById = getSectionById(ref, inventorySectionId, componentAccessor);
      if (sectionById != null) {
         InventoryComponent.Storage storageComponent = componentAccessor.getComponent(ref, InventoryComponent.Storage.getComponentType());
         if (storageComponent != null) {
            return storageComponent.getInventory().moveAllItemStacksTo(sectionById);
         }
      }

      return null;
   }

   @Nullable
   public static ListTransaction<MoveTransaction<ItemStackTransaction>> quickStack(
      @Nonnull Ref<EntityStore> ref, int inventorySectionId, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      ItemContainer sectionById = getSectionById(ref, inventorySectionId, componentAccessor);
      return sectionById != null ? InventoryComponent.getCombined(componentAccessor, ref, InventoryComponent.HOTBAR_FIRST).quickStackTo(sectionById) : null;
   }

   @Nullable
   public static ItemContainer getSectionById(@Nonnull Ref<EntityStore> ref, int id, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (id >= 0) {
         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
         return playerComponent != null && playerComponent.getWindowManager().getWindow(id) instanceof ItemContainerWindow itemContainerWindow
            ? itemContainerWindow.getItemContainer()
            : null;
      } else {
         ComponentType<EntityStore, ? extends InventoryComponent> componentType = InventoryComponent.getComponentTypeById(id);
         if (componentType == null) {
            return null;
         } else {
            InventoryComponent inventoryComponent = componentAccessor.getComponent(ref, componentType);
            return inventoryComponent != null ? inventoryComponent.inventory : null;
         }
      }
   }

   public static boolean containsBrokenItem(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
      CombinedItemContainer everything = InventoryComponent.getCombined(accessor, ref, InventoryComponent.EVERYTHING);

      for (short i = 0; i < everything.getCapacity(); i++) {
         ItemStack itemStack = everything.getItemStack(i);
         if (!ItemStack.isEmpty(itemStack) && itemStack.isBroken()) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   public static List<ItemStack> dropAllItemStacks(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
      return InventoryComponent.getCombined(accessor, ref, InventoryComponent.EVERYTHING).dropAllItemStacks();
   }

   public static void clear(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
      InventoryComponent.getCombined(accessor, ref, InventoryComponent.EVERYTHING).clear();
   }

   public static void sortStorage(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
      InventoryComponent.Storage storageComponent = accessor.getComponent(ref, InventoryComponent.Storage.getComponentType());
      if (storageComponent != null) {
         storageComponent.getInventory().sortItems(SortType.TYPE);
         storageComponent.markChanged();
      }
   }

   public static byte getActiveSlot(@Nonnull Ref<EntityStore> ref, int inventorySectionId, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return switch (inventorySectionId) {
         case -8 -> {
            InventoryComponent.Tool toolInventoryComponent = componentAccessor.getComponent(ref, InventoryComponent.Tool.getComponentType());
            yield toolInventoryComponent != null ? toolInventoryComponent.getActiveSlot() : -1;
         }
         case -5 -> {
            InventoryComponent.Utility utilityInventoryComponent = componentAccessor.getComponent(ref, InventoryComponent.Utility.getComponentType());
            yield utilityInventoryComponent != null ? utilityInventoryComponent.getActiveSlot() : -1;
         }
         case -1 -> {
            InventoryComponent.Hotbar hotbarInventoryComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
            yield hotbarInventoryComponent != null ? hotbarInventoryComponent.getActiveSlot() : -1;
         }
         default -> throw new IllegalArgumentException("Inventory section with id " + inventorySectionId + " cannot select an active slot");
      };
   }

   /** @deprecated */
   public static void setActiveSlot(@Nonnull Ref<EntityStore> ref, int inventorySectionId, byte slot, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      ComponentType<EntityStore, ? extends ActiveSlotInventoryComponent> componentType = switch (inventorySectionId) {
         case -8 -> InventoryComponent.Tool.getComponentType();
         case -5 -> InventoryComponent.Utility.getComponentType();
         case -1 -> InventoryComponent.Hotbar.getComponentType();
         default -> throw new IllegalArgumentException("Inventory section with id " + inventorySectionId + " cannot select an active slot");
      };
      ActiveSlotInventoryComponent component = componentAccessor.getComponent(ref, componentType);
      if (component != null) {
         component.setActiveSlot(slot, ref, componentAccessor);
      }
   }

   @Nonnull
   public static EquipmentUpdate createEquipmentUpdate(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nullable PlayerSettings playerSettings,
      @Nullable InventoryComponent.Armor armorComponent,
      @Nullable InventoryComponent.Utility utilityComponent
   ) {
      EquipmentUpdate update = new EquipmentUpdate();
      if (armorComponent != null) {
         ItemContainer armorInventory = armorComponent.getInventory();
         update.armorIds = new String[armorInventory.getCapacity()];
         Arrays.fill(update.armorIds, "");
         armorInventory.forEachWithMeta((slot, itemStack, armorIds) -> armorIds[slot] = itemStack.getItemId(), update.armorIds);
         if (playerSettings != null) {
            World world = componentAccessor.getExternalData().getWorld();
            PlayerConfig playerConfig = world.getGameplayConfig().getPlayerConfig();
            PlayerConfig.ArmorVisibilityOption armorVisibilityOption = playerConfig.getArmorVisibilityOption();
            if (armorVisibilityOption.canHideHelmet() && playerSettings.hideHelmet()) {
               update.armorIds[ItemArmorSlot.Head.ordinal()] = "";
            }

            if (armorVisibilityOption.canHideCuirass() && playerSettings.hideCuirass()) {
               update.armorIds[ItemArmorSlot.Chest.ordinal()] = "";
            }

            if (armorVisibilityOption.canHideGauntlets() && playerSettings.hideGauntlets()) {
               update.armorIds[ItemArmorSlot.Hands.ordinal()] = "";
            }

            if (armorVisibilityOption.canHidePants() && playerSettings.hidePants()) {
               update.armorIds[ItemArmorSlot.Legs.ordinal()] = "";
            }
         }
      }

      ItemStack itemInHand = InventoryComponent.getItemInHand(componentAccessor, ref);
      update.rightHandItemId = itemInHand != null ? itemInHand.getItemId() : "Empty";
      ItemStack utilityItem = utilityComponent != null ? utilityComponent.getActiveItem() : null;
      update.leftHandItemId = utilityItem != null ? utilityItem.getItemId() : "Empty";
      return update;
   }
}
