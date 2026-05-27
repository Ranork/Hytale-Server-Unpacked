package com.hypixel.hytale.server.core.io.handlers.game;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.ItemSoundEvent;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.inventory.DropCreativeItem;
import com.hypixel.hytale.protocol.packets.inventory.DropItemStack;
import com.hypixel.hytale.protocol.packets.inventory.InventoryAction;
import com.hypixel.hytale.protocol.packets.inventory.MoveItemStack;
import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import com.hypixel.hytale.protocol.packets.inventory.SetCreativeItem;
import com.hypixel.hytale.protocol.packets.inventory.SmartGiveCreativeItem;
import com.hypixel.hytale.protocol.packets.inventory.SmartMoveItemStack;
import com.hypixel.hytale.protocol.packets.inventory.SwitchHotbarBlockSet;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.BlockGroup;
import com.hypixel.hytale.server.core.asset.type.item.config.BlockSelectorToolData;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.itemsound.config.ItemSoundSet;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ItemContainerWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.InventoryActiveSlotRequestEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SortType;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.util.TempAssetIdUtil;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class InventoryPacketHandler implements SubPacketHandler {
   private final IPacketHandler packetHandler;

   public InventoryPacketHandler(IPacketHandler packetHandler) {
      this.packetHandler = packetHandler;
   }

   @Override
   public void registerHandlers() {
      this.packetHandler.registerHandler(171, p -> this.handle((SetCreativeItem)p));
      this.packetHandler.registerHandler(172, p -> this.handle((DropCreativeItem)p));
      this.packetHandler.registerHandler(173, p -> this.handle((SmartGiveCreativeItem)p));
      this.packetHandler.registerHandler(174, p -> this.handle((DropItemStack)p));
      this.packetHandler.registerHandler(175, p -> this.handle((MoveItemStack)p));
      this.packetHandler.registerHandler(176, p -> this.handle((SmartMoveItemStack)p));
      this.packetHandler.registerHandler(177, p -> this.handle((SetActiveSlot)p));
      this.packetHandler.registerHandler(178, p -> this.handle((SwitchHotbarBlockSet)p));
      this.packetHandler.registerHandler(179, p -> this.handle((InventoryAction)p));
   }

   public void handle(@Nonnull SetCreativeItem packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(
            () -> {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

               assert playerRefComponent != null;

               if (playerComponent.getGameMode() != GameMode.Creative) {
                  NotificationUtil.sendNotification(
                     playerRefComponent.getPacketHandler(), Message.translation("server.general.setCreativeItem.notInCreativeMode")
                  );
               } else {
                  int quantity = packet.item.quantity;
                  if (quantity > 0) {
                     ItemStack itemStack = ItemStack.fromPacket(packet.item);
                     if (packet.slotId < 0) {
                        CombinedItemContainer combinedInventory = InventoryComponent.getCombined(store, ref, InventoryComponent.HOTBAR_FIRST);
                        ItemStackTransaction transaction = combinedInventory.addItemStack(itemStack);
                        ItemStack remainder = transaction.getRemainder();
                        if (remainder != null && !remainder.isEmpty()) {
                           ItemUtils.dropItem(ref, remainder, store);
                        }
                     } else {
                        ItemContainer sectionById = InventoryUtils.getSectionById(ref, packet.inventorySectionId, store);
                        if (sectionById != null) {
                           if (packet.override) {
                              sectionById.setItemStackForSlot((short)packet.slotId, itemStack);
                           } else {
                              ItemStack existing = sectionById.getItemStack((short)packet.slotId);
                              if (existing != null && !existing.isEmpty() && existing.isStackableWith(itemStack)) {
                                 sectionById.addItemStackToSlot((short)packet.slotId, itemStack);
                              } else {
                                 sectionById.setItemStackForSlot((short)packet.slotId, itemStack);
                              }
                           }
                        }
                     }
                  } else if (packet.override) {
                     ItemContainer sectionById = InventoryUtils.getSectionById(ref, packet.inventorySectionId, store);
                     if (sectionById != null) {
                        sectionById.setItemStackForSlot((short)packet.slotId, null);
                     }
                  }
               }
            }
         );
      }
   }

   public void handle(@Nonnull DropCreativeItem packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         ItemStack itemStack = ItemStack.fromPacket(packet.item);
         if (itemStack != null) {
            Item item = itemStack.getItem();
            if (item != Item.UNKNOWN) {
               itemStack = itemStack.withQuantity(Math.min(itemStack.getQuantity(), item.getMaxStack()));
               world.execute(
                  () -> {
                     Player playerComponent = store.getComponent(ref, Player.getComponentType());

                     assert playerComponent != null;

                     PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

                     assert playerRefComponent != null;

                     if (playerComponent.getGameMode() != GameMode.Creative) {
                        NotificationUtil.sendNotification(
                           playerRefComponent.getPacketHandler(), Message.translation("server.general.setCreativeItem.notInCreativeMode")
                        );
                     } else {
                        ItemUtils.dropItem(ref, itemStack, store);
                     }
                  }
               );
            }
         }
      }
   }

   public void handle(SwitchHotbarBlockSet packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(
            () -> {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               InventoryComponent.Hotbar hotbarComponent = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());

               assert hotbarComponent != null;

               ItemContainer hotbar = hotbarComponent.getInventory();
               byte activeHotbarSlot = hotbarComponent.getActiveSlot();
               if (activeHotbarSlot != -1) {
                  ItemStack stack = hotbar.getItemStack(activeHotbarSlot);
                  if (stack != null && !stack.isEmpty()) {
                     BlockGroup set = BlockGroup.findItemGroup(stack.getItem());
                     if (set != null) {
                        Item desiredItem = Item.getAssetMap().getAsset(packet.itemId);
                        if (desiredItem != null) {
                           int currentIndex = set.getIndex(stack.getItem());
                           if (currentIndex != -1) {
                              int desiredIndex = set.getIndex(desiredItem);
                              if (desiredIndex != -1 && desiredIndex != currentIndex) {
                                 ItemStack maxSelectorTool = null;
                                 short maxSlot = -1;
                                 CombinedItemContainer combinedInventory = InventoryComponent.getCombined(
                                    store, ref, InventoryComponent.ARMOR_HOTBAR_UTILITY_STORAGE
                                 );

                                 for (short i = 0; i < combinedInventory.getCapacity(); i++) {
                                    ItemStack potentialSelector = combinedInventory.getItemStack(i);
                                    if (!ItemStack.isEmpty(potentialSelector)) {
                                       Item item = potentialSelector.getItem();
                                       BlockSelectorToolData selectorTool = item.getBlockSelectorToolData();
                                       if (selectorTool != null
                                          && (maxSelectorTool == null || maxSelectorTool.getDurability() < potentialSelector.getDurability())) {
                                          maxSelectorTool = potentialSelector;
                                          maxSlot = i;
                                       }
                                    }
                                 }

                                 if (maxSelectorTool != null) {
                                    BlockSelectorToolData toolData = maxSelectorTool.getItem().getBlockSelectorToolData();
                                    if (ItemUtils.canDecreaseItemStackDurability(ref, store) && !maxSelectorTool.isUnbreakable()) {
                                       ItemUtils.updateItemStackDurability(
                                          ref, maxSelectorTool, combinedInventory, maxSlot, -toolData.getDurabilityLossOnUse(), store
                                       );
                                    }

                                    ItemStack replacement = new ItemStack(set.get(desiredIndex), stack.getQuantity());
                                    hotbar.setItemStackForSlot(activeHotbarSlot, replacement);
                                    ItemSoundSet soundSet = ItemSoundSet.getAssetMap().getAsset(desiredItem.getItemSoundSetIndex());
                                    if (soundSet != null) {
                                       String dragSound = soundSet.getSoundEventIds().get(ItemSoundEvent.Drop);
                                       if (dragSound != null) {
                                          int dragSoundIndex = SoundEvent.getAssetMap().getIndex(dragSound);
                                          if (dragSoundIndex != 0) {
                                             SoundUtil.playSoundEvent2d(ref, dragSoundIndex, SoundCategory.UI, store);
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         );
      }
   }

   public void handle(@Nonnull SmartGiveCreativeItem packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(
            () -> {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

               assert playerRefComponent != null;

               if (playerComponent.getGameMode() != GameMode.Creative) {
                  NotificationUtil.sendNotification(
                     playerRefComponent.getPacketHandler(), Message.translation("server.general.setCreativeItem.notInCreativeMode")
                  );
               } else {
                  ItemStack itemStack = ItemStack.fromPacket(packet.item);
                  if (itemStack != null) {
                     switch (packet.moveType) {
                        case EquipOrMergeStack:
                           Item item = itemStack.getItem();
                           ItemArmor itemArmor = item.getArmor();
                           if (itemArmor != null) {
                              InventoryComponent.Armor armorComponent = store.getComponent(ref, InventoryComponent.Armor.getComponentType());
                              if (armorComponent != null) {
                                 armorComponent.getInventory().setItemStackForSlot((short)itemArmor.getArmorSlot().ordinal(), itemStack);
                              }

                              return;
                           }

                           int quantity = itemStack.getQuantity();
                           if (item.getUtility().isUsable()) {
                              InventoryComponent.Utility utilityComponent = store.getComponent(ref, InventoryComponent.Utility.getComponentType());
                              if (utilityComponent != null) {
                                 ItemStackTransaction transaction = utilityComponent.getInventory().addItemStack(itemStack);
                                 ItemStack remainder = transaction.getRemainder();
                                 if (ItemStack.isEmpty(remainder) || remainder.getQuantity() != quantity) {
                                    for (ItemStackSlotTransaction slotTransaction : transaction.getSlotTransactions()) {
                                       if (slotTransaction.succeeded()) {
                                          utilityComponent.setActiveSlot((byte)slotTransaction.getSlot(), ref, store);
                                       }
                                    }
                                 }
                              }
                           }
                           break;
                        case PutInHotbarOrWindow:
                           Player.giveItem(itemStack, ref, store);
                        case PutInHotbarOrBackpack:
                     }
                  }
               }
            }
         );
      }
   }

   public void handle(@Nonnull DropItemStack packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(
            () -> {
               DropItemEvent.PlayerRequest event = new DropItemEvent.PlayerRequest(packet.inventorySectionId, (short)packet.slotId);
               store.invoke(ref, event);
               if (!event.isCancelled()) {
                  ItemContainer sectionById = InventoryUtils.getSectionById(ref, event.getInventorySectionId(), store);
                  if (sectionById == null) {
                     HytaleLogger.getLogger()
                        .at(Level.WARNING)
                        .log("%s attempted to drop an ItemStack from an invalid inventory section! %s", playerRef.getUsername(), event.getInventorySectionId());
                     return;
                  }

                  ItemStackSlotTransaction transaction = sectionById.removeItemStackFromSlot(event.getSlotId(), packet.quantity);
                  ItemStack item = transaction.getOutput();
                  if (item == null || item.isEmpty()) {
                     HytaleLogger.getLogger().at(Level.WARNING).log("%s attempted to drop an empty ItemStack!", playerRef.getUsername());
                     return;
                  }

                  String itemId = item.getItemId();
                  if (!ItemModule.exists(itemId)) {
                     HytaleLogger.getLogger().at(Level.WARNING).log("%s attempted to drop an unregistered ItemStack! %s", playerRef.getUsername(), itemId);
                     return;
                  }

                  ItemUtils.throwItem(ref, item, 6.0F, store);
                  SoundUtil.playSoundEvent2d(ref, TempAssetIdUtil.getSoundEventIndex("SFX_Player_Drop_Item"), SoundCategory.UI, store);
               } else {
                  ComponentType<EntityStore, ? extends InventoryComponent> type = InventoryComponent.getComponentTypeById(packet.inventorySectionId);
                  if (type == null) {
                     return;
                  }

                  InventoryComponent inv = store.getComponent(ref, type);
                  if (inv == null) {
                     return;
                  }

                  inv.markDirty();
               }
            }
         );
      }
   }

   public void handle(@Nonnull MoveItemStack packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(() -> {
            InventoryUtils.moveItem(ref, packet.fromSectionId, packet.fromSlotId, packet.quantity, packet.toSectionId, packet.toSlotId, store);
            if (packet.toSectionId != packet.fromSectionId && packet.toSectionId == -5) {
               byte newSlot = (byte)packet.toSlotId;
               int inventorySectionId = packet.toSectionId;
               byte currentSlot = InventoryUtils.getActiveSlot(ref, inventorySectionId, store);
               if (currentSlot == newSlot) {
                  return;
               }

               InventoryActiveSlotRequestEvent event = new InventoryActiveSlotRequestEvent(inventorySectionId, currentSlot, newSlot, true);
               store.invoke(ref, event);
               if (event.isCancelled() || event.getNewSlot() == currentSlot) {
                  return;
               }

               newSlot = event.getNewSlot();
               InventoryUtils.setActiveSlot(ref, inventorySectionId, newSlot, store);
               playerRef.getPacketHandler().writeNoCache(new SetActiveSlot(inventorySectionId, newSlot));
            }
         });
      }
   }

   public void handle(@Nonnull SmartMoveItemStack packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(() -> {
            PlayerSettings settings = store.getComponent(ref, PlayerSettings.getComponentType());
            if (settings == null) {
               settings = PlayerSettings.defaults();
            }

            InventoryUtils.smartMoveItem(ref, packet.fromSectionId, packet.fromSlotId, packet.quantity, packet.moveType, settings, store);
         });
      }
   }

   public void handle(@Nonnull SetActiveSlot packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(
            () -> {
               PacketHandler packetHandler = playerRef.getPacketHandler();
               if (packet.inventorySectionId == -1) {
                  packetHandler.disconnect(Message.translation("server.general.disconnect.hotbarChangeWithoutInteraction"));
               } else {
                  ItemContainer sectionById = InventoryUtils.getSectionById(ref, packet.inventorySectionId, store);
                  if (sectionById == null) {
                     packetHandler.disconnect(
                        Message.translation("server.general.disconnect.invalidInventorySection").param("inventorySectionId", packet.inventorySectionId)
                     );
                  } else if (packet.activeSlot < -1 || packet.activeSlot >= sectionById.getCapacity()) {
                     packetHandler.disconnect(
                        Message.translation("server.general.disconnect.hotbarSlotOutOfRange").param("inventorySectionId", packet.inventorySectionId)
                     );
                  } else if (packet.activeSlot == InventoryUtils.getActiveSlot(ref, packet.inventorySectionId, store)) {
                     packetHandler.disconnect(Message.translation("server.general.disconnect.hotbarSlotAlreadySelected"));
                  } else {
                     byte previousSlot = InventoryUtils.getActiveSlot(ref, packet.inventorySectionId, store);
                     byte targetSlot = (byte)packet.activeSlot;
                     InventoryActiveSlotRequestEvent event = new InventoryActiveSlotRequestEvent(packet.inventorySectionId, previousSlot, targetSlot, false);
                     store.invoke(ref, event);
                     if (event.isCancelled()) {
                        targetSlot = previousSlot;
                     } else if (targetSlot != event.getNewSlot()) {
                        targetSlot = event.getNewSlot();
                     }

                     if (targetSlot != packet.activeSlot) {
                        packetHandler.writeNoCache(new SetActiveSlot(packet.inventorySectionId, targetSlot));
                     }

                     if (targetSlot != previousSlot) {
                        InventoryUtils.setActiveSlot(ref, packet.inventorySectionId, targetSlot, store);
                     }
                  }
               }
            }
         );
      }
   }

   public void handle(@Nonnull InventoryAction packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         if (packet.inventorySectionId >= 0 || packet.inventorySectionId == -9) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(
               () -> {
                  Player playerComponent = store.getComponent(ref, Player.getComponentType());

                  assert playerComponent != null;

                  PlayerSettings settings = store.getComponent(ref, PlayerSettings.getComponentType());
                  if (settings == null) {
                     settings = PlayerSettings.defaults();
                  }

                  switch (packet.inventoryActionType) {
                     case TakeAll:
                        if (packet.inventorySectionId == -9) {
                           InventoryUtils.takeAll(ref, packet.inventorySectionId, settings, store);
                           return;
                        }

                        Window window = playerComponent.getWindowManager().getWindow(packet.inventorySectionId);
                        if (window instanceof ItemContainerWindow itemContainerWindow) {
                           if (window.getType() == WindowType.Processing) {
                              if (itemContainerWindow.getItemContainer() instanceof CombinedItemContainer combinedItemContainer
                                 && combinedItemContainer.getContainersSize() >= 3) {
                                 ItemContainer outputContainer = combinedItemContainer.getContainer(2);
                                 InventoryUtils.takeAllWithPriority(ref, outputContainer, settings, store);
                              }
                           } else {
                              InventoryUtils.takeAll(ref, packet.inventorySectionId, settings, store);
                           }
                        }
                        break;
                     case PutAll:
                        if (packet.inventorySectionId == -9) {
                           InventoryUtils.putAll(ref, packet.inventorySectionId, store);
                           return;
                        }

                        Window window = playerComponent.getWindowManager().getWindow(packet.inventorySectionId);
                        if (window instanceof ItemContainerWindow) {
                           InventoryUtils.putAll(ref, packet.inventorySectionId, store);
                        }
                        break;
                     case QuickStack:
                        if (packet.inventorySectionId == -9) {
                           InventoryUtils.quickStack(ref, packet.inventorySectionId, store);
                           return;
                        }

                        Window window = playerComponent.getWindowManager().getWindow(packet.inventorySectionId);
                        if (window instanceof ItemContainerWindow) {
                           InventoryUtils.quickStack(ref, packet.inventorySectionId, store);
                        }
                        break;
                     case Sort:
                        if (packet.inventorySectionId == 0) {
                           InventoryUtils.sortStorage(ref, store);
                        } else {
                           InventoryComponent.Backpack backpackComponent = store.getComponent(ref, InventoryComponent.Backpack.getComponentType());
                           if (packet.inventorySectionId == -9 && backpackComponent != null) {
                              backpackComponent.getInventory().sortItems(SortType.TYPE);
                              return;
                           }

                           if (playerComponent.getWindowManager().getWindow(packet.inventorySectionId) instanceof ItemContainerWindow itemContainerWindow) {
                              itemContainerWindow.getItemContainer().sortItems(SortType.TYPE);
                           }
                        }
                  }
               }
            );
         }
      }
   }
}
