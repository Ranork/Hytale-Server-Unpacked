package com.hypixel.hytale.server.core.entity.entities.player.windows;

import com.google.gson.JsonObject;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.window.SortItemsAction;
import com.hypixel.hytale.protocol.packets.window.WindowAction;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SortType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ContainerBlockWindow extends BlockWindow implements ItemContainerWindow {
   @Nonnull
   private final JsonObject windowData;
   @Nonnull
   private final ItemContainer itemContainer;

   public ContainerBlockWindow(int x, int y, int z, int rotationIndex, @Nonnull BlockType blockType, @Nonnull ItemContainer itemContainer) {
      super(WindowType.Container, x, y, z, rotationIndex, blockType);
      this.itemContainer = itemContainer;
      this.windowData = new JsonObject();
      Item item = blockType.getItem();
      this.windowData.addProperty("blockItemId", item.getId());
   }

   @Nonnull
   @Override
   public JsonObject getData() {
      return this.windowData;
   }

   @Override
   public boolean onOpen0(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      return true;
   }

   @Override
   public void onClose0(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
   }

   @Nonnull
   @Override
   public ItemContainer getItemContainer() {
      return this.itemContainer;
   }

   @Override
   public void handleAction(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WindowAction action) {
      if (action instanceof SortItemsAction sortAction) {
         SortType sortType = SortType.fromPacket(sortAction.sortType);
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getInventory().setSortType(sortType);
         this.itemContainer.sortItems(sortType);
         this.invalidate();
      }
   }
}
