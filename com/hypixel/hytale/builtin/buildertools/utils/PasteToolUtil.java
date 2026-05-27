package com.hypixel.hytale.builtin.buildertools.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class PasteToolUtil {
   private static final String PASTE_TOOL_ID = "EditorTool_Paste";

   private PasteToolUtil() {
   }

   public static void switchToPasteTool(@Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      InventoryComponent.Hotbar hotbarComponent = componentAccessor.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
      if (hotbarComponent != null) {
         ItemContainer hotbar = hotbarComponent.getInventory();
         int hotbarSize = hotbar.getCapacity();

         for (short slot = 0; slot < hotbarSize; slot++) {
            ItemStack itemStack = hotbar.getItemStack(slot);
            if (itemStack != null && !itemStack.isEmpty() && "EditorTool_Paste".equals(itemStack.getItemId())) {
               hotbarComponent.setActiveSlot((byte)slot, ref, componentAccessor);
               playerRef.getPacketHandler().writeNoCache(new SetActiveSlot(-1, (byte)slot));
               return;
            }
         }

         short emptySlot = -1;

         for (short slotx = 0; slotx < hotbarSize; slotx++) {
            ItemStack itemStack = hotbar.getItemStack(slotx);
            if (itemStack == null || itemStack.isEmpty()) {
               emptySlot = slotx;
               break;
            }
         }

         if (emptySlot != -1) {
            InventoryComponent.Storage storageComponent = componentAccessor.getComponent(ref, InventoryComponent.Storage.getComponentType());
            if (storageComponent != null) {
               ItemContainer storage = storageComponent.getInventory();

               for (short slotxx = 0; slotxx < storage.getCapacity(); slotxx++) {
                  ItemStack itemStack = storage.getItemStack(slotxx);
                  if (itemStack != null && !itemStack.isEmpty() && "EditorTool_Paste".equals(itemStack.getItemId())) {
                     storage.moveItemStackFromSlotToSlot(slotxx, 1, hotbar, emptySlot);
                     hotbarComponent.setActiveSlot((byte)emptySlot, ref, componentAccessor);
                     playerRef.getPacketHandler().writeNoCache(new SetActiveSlot(-1, (byte)emptySlot));
                     return;
                  }
               }
            }

            InventoryComponent.Tool toolComponent = componentAccessor.getComponent(ref, InventoryComponent.Tool.getComponentType());
            if (toolComponent != null) {
               ItemContainer tools = toolComponent.getInventory();
               ItemStack pasteToolStack = null;

               for (short slotxxx = 0; slotxxx < tools.getCapacity(); slotxxx++) {
                  ItemStack itemStack = tools.getItemStack(slotxxx);
                  if (itemStack != null && !itemStack.isEmpty() && "EditorTool_Paste".equals(itemStack.getItemId())) {
                     pasteToolStack = itemStack;
                     break;
                  }
               }

               if (pasteToolStack != null) {
                  hotbar.setItemStackForSlot(emptySlot, new ItemStack(pasteToolStack.getItemId()));
                  hotbarComponent.setActiveSlot((byte)emptySlot, ref, componentAccessor);
                  playerRef.getPacketHandler().writeNoCache(new SetActiveSlot(-1, (byte)emptySlot));
               }
            }
         }
      }
   }
}
