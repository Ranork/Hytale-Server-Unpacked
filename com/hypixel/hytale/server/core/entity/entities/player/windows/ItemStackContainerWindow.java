package com.hypixel.hytale.server.core.entity.entities.player.windows;

import com.google.gson.JsonObject;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemStackItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemStackContainerWindow extends Window implements ItemContainerWindow {
   @Nonnull
   private final JsonObject windowData = new JsonObject();
   @Nonnull
   private final ItemStackItemContainer itemStackItemContainer;
   @Nullable
   private EventRegistration eventRegistration;

   public ItemStackContainerWindow(@Nonnull ItemStackItemContainer itemStackItemContainer) {
      super(WindowType.Container);
      this.itemStackItemContainer = itemStackItemContainer;
   }

   @Nonnull
   @Override
   public JsonObject getData() {
      return this.windowData;
   }

   @Override
   public boolean onOpen0(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      this.eventRegistration = this.itemStackItemContainer.getParentContainer().registerChangeEvent(event -> {
         if (!this.itemStackItemContainer.isItemStackValid()) {
            this.close(ref, store);
         }
      });
      return true;
   }

   @Override
   public void onClose0(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.eventRegistration.unregister();
      this.eventRegistration = null;
   }

   @Nonnull
   @Override
   public ItemContainer getItemContainer() {
      return this.itemStackItemContainer;
   }
}
