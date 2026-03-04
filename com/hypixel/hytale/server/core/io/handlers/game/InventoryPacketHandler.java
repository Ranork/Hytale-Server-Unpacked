package com.hypixel.hytale.server.core.io.handlers.game;

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
import com.hypixel.hytale.server.core.event.events.ecs.SwitchActiveSlotEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
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
                  Inventory inventory = playerComponent.getInventory();
                  int quantity = packet.item.quantity;
                  if (quantity > 0) {
                     ItemStack itemStack = ItemStack.fromPacket(packet.item);
                     if (packet.slotId < 0) {
                        ItemStackTransaction transaction = inventory.getCombinedHotbarFirst().addItemStack(itemStack);
                        ItemStack remainder = transaction.getRemainder();
                        if (remainder != null && !remainder.isEmpty()) {
                           ItemUtils.dropItem(ref, remainder, store);
                        }
                     } else {
                        ItemContainer sectionById = inventory.getSectionById(packet.inventorySectionId);
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
                  } else if (packet.override) {
                     inventory.getSectionById(packet.inventorySectionId).setItemStackForSlot((short)packet.slotId, null);
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

               Inventory inventory = playerComponent.getInventory();
               byte slot = inventory.getActiveHotbarSlot();
               if (slot != -1) {
                  ItemContainer hotbar = inventory.getHotbar();
                  ItemStack stack = hotbar.getItemStack(slot);
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
                                 CombinedItemContainer combinedInventory = inventory.getCombinedArmorHotbarUtilityStorage();

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
                                    if (playerComponent.canDecreaseItemStackDurability(ref, store) && !maxSelectorTool.isUnbreakable()) {
                                       playerComponent.updateItemStackDurability(
                                          ref, maxSelectorTool, combinedInventory, maxSlot, -toolData.getDurabilityLossOnUse(), store
                                       );
                                    }

                                    ItemStack replacement = new ItemStack(set.get(desiredIndex), stack.getQuantity());
                                    hotbar.setItemStackForSlot(slot, replacement);
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
                  Inventory inventory = playerComponent.getInventory();
                  ItemStack itemStack = ItemStack.fromPacket(packet.item);
                  if (itemStack != null) {
                     switch (packet.moveType) {
                        case EquipOrMergeStack:
                           Item item = itemStack.getItem();
                           ItemArmor itemArmor = item.getArmor();
                           if (itemArmor != null) {
                              inventory.getArmor().setItemStackForSlot((short)itemArmor.getArmorSlot().ordinal(), itemStack);
                              return;
                           }

                           int quantity = itemStack.getQuantity();
                           if (item.getUtility().isUsable()) {
                              ItemStackTransaction transaction = inventory.getUtility().addItemStack(itemStack);
                              ItemStack remainder = transaction.getRemainder();
                              if (ItemStack.isEmpty(remainder) || remainder.getQuantity() != quantity) {
                                 for (ItemStackSlotTransaction slotTransaction : transaction.getSlotTransactions()) {
                                    if (slotTransaction.succeeded()) {
                                       inventory.setActiveUtilitySlot((byte)slotTransaction.getSlot());
                                    }
                                 }
                              }

                              return;
                           }
                           break;
                        case PutInHotbarOrWindow:
                           playerComponent.giveItem(itemStack, ref, store);
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
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               Inventory inventory = playerComponent.getInventory();
               if (!event.isCancelled()) {
                  ItemStackSlotTransaction transaction = inventory.getSectionById(event.getInventorySectionId())
                     .removeItemStackFromSlot(event.getSlotId(), packet.quantity);
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
                  playerComponent.sendInventory();
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
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            Inventory inventory = playerComponent.getInventory();
            inventory.moveItem(packet.fromSectionId, packet.fromSlotId, packet.quantity, packet.toSectionId, packet.toSlotId);
            if (packet.toSectionId != packet.fromSectionId && packet.toSectionId == -5) {
               byte newSlot = (byte)packet.toSlotId;
               int inventorySectionId = packet.toSectionId;
               byte currentSlot = inventory.getActiveSlot(inventorySectionId);
               if (currentSlot == newSlot) {
                  return;
               }

               SwitchActiveSlotEvent event = new SwitchActiveSlotEvent(inventorySectionId, currentSlot, newSlot, true);
               store.invoke(ref, event);
               if (event.isCancelled() || event.getNewSlot() == currentSlot) {
                  return;
               }

               newSlot = event.getNewSlot();
               inventory.setActiveSlot(inventorySectionId, newSlot);
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
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            Inventory inventory = playerComponent.getInventory();
            PlayerSettings settings = store.getComponent(ref, PlayerSettings.getComponentType());
            if (settings == null) {
               settings = PlayerSettings.defaults();
            }

            inventory.smartMoveItem(packet.fromSectionId, packet.fromSlotId, packet.quantity, packet.moveType, settings);
         });
      }
   }

   public void handle(@Nonnull SetActiveSlot packet) {
      PlayerRef playerRef = this.packetHandler.getPlayerRef();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         world.execute(() -> {
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            Inventory inventory = playerComponent.getInventory();
            PacketHandler packetHandler = playerRef.getPacketHandler();
            if (packet.inventorySectionId == -1) {
               packetHandler.disconnect("Attempted to change the hotbar slot without an interaction");
            } else if (packet.activeSlot < -1 || packet.activeSlot >= inventory.getSectionById(packet.inventorySectionId).getCapacity()) {
               packetHandler.disconnect("Attempted to set " + packet.inventorySectionId + " slot outside of range!");
            } else if (packet.activeSlot == inventory.getActiveSlot(packet.inventorySectionId)) {
               packetHandler.disconnect("Attempted to set hotbar slot to already selected hotbar slot!");
            } else {
               byte previousSlot = inventory.getActiveSlot(packet.inventorySectionId);
               byte targetSlot = (byte)packet.activeSlot;
               SwitchActiveSlotEvent event = new SwitchActiveSlotEvent(packet.inventorySectionId, previousSlot, targetSlot, false);
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
                  inventory.setActiveSlot(packet.inventorySectionId, targetSlot);
               }
            }
         });
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

                  Inventory inventory = playerComponent.getInventory();
                  PlayerSettings settings = store.getComponent(ref, PlayerSettings.getComponentType());
                  if (settings == null) {
                     settings = PlayerSettings.defaults();
                  }

                  switch (packet.inventoryActionType) {
                     case TakeAll:
                        if (packet.inventorySectionId == -9) {
                           inventory.takeAll(packet.inventorySectionId, settings);
                           return;
                        }

                        Window window = playerComponent.getWindowManager().getWindow(packet.inventorySectionId);
                        if (window instanceof ItemContainerWindow itemContainerWindow) {
                           if (window.getType() == WindowType.Processing) {
                              if (itemContainerWindow.getItemContainer() instanceof CombinedItemContainer combinedItemContainer
                                 && combinedItemContainer.getContainersSize() >= 3) {
                                 ItemContainer outputContainer = combinedItemContainer.getContainer(2);
                                 inventory.takeAllWithPriority(outputContainer, settings);
                              }
                           } else {
                              inventory.takeAll(packet.inventorySectionId, settings);
                           }
                        }
                        break;
                     case PutAll:
                        if (packet.inventorySectionId == -9) {
                           inventory.putAll(packet.inventorySectionId);
                           return;
                        }

                        Window window = playerComponent.getWindowManager().getWindow(packet.inventorySectionId);
                        if (window instanceof ItemContainerWindow) {
                           inventory.putAll(packet.inventorySectionId);
                        }
                        break;
                     case QuickStack:
                        if (packet.inventorySectionId == -9) {
                           inventory.quickStack(packet.inventorySectionId);
                           return;
                        }

                        Window window = playerComponent.getWindowManager().getWindow(packet.inventorySectionId);
                        if (window instanceof ItemContainerWindow) {
                           inventory.quickStack(packet.inventorySectionId);
                        }
                        break;
                     case Sort:
                        SortType sortType = SortType.VALUES[packet.actionData];
                        if (packet.inventorySectionId == 0) {
                           inventory.sortStorage(sortType);
                        } else {
                           if (packet.inventorySectionId == -9 && inventory.getBackpack() != null) {
                              inventory.getBackpack().sortItems(sortType);
                              return;
                           }

                           if (playerComponent.getWindowManager().getWindow(packet.inventorySectionId) instanceof ItemContainerWindow itemContainerWindow) {
                              itemContainerWindow.getItemContainer().sortItems(sortType);
                           }
                        }
                  }
               }
            );
         }
      }
   }
}
