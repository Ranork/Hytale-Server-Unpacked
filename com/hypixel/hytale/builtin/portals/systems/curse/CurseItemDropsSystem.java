package com.hypixel.hytale.builtin.portals.systems.curse;

import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.AdventureMetadata;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurseItemDropsSystem extends RefSystem<EntityStore> {
   @Override
   public void onEntityAdded(
      @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PortalWorld portalWorld = store.getResource(PortalWorld.getResourceType());
      if (portalWorld.exists()) {
         Set<String> cursedItemsInWorld = portalWorld.getPortalType().getCursedItems();
         if (!cursedItemsInWorld.isEmpty()) {
            ItemComponent itemComponent = store.getComponent(ref, ItemComponent.getComponentType());
            ItemStack itemStack = itemComponent.getItemStack();
            if (itemStack != null) {
               String itemId = itemStack.getItemId().toString();
               if (cursedItemsInWorld.contains(itemId)) {
                  AdventureMetadata adventureMeta = itemStack.getFromMetadataOrDefault("Adventure", AdventureMetadata.CODEC);
                  adventureMeta.setCursed(true);
                  ItemStack cursed = itemStack.withMetadata(AdventureMetadata.KEYED_CODEC, adventureMeta);
                  itemComponent.setItemStack(cursed);
               }
            }
         }
      }
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
   }

   @Nullable
   @Override
   public Query<EntityStore> getQuery() {
      return ItemComponent.getComponentType();
   }
}
