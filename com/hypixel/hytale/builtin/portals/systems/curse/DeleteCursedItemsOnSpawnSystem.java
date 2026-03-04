package com.hypixel.hytale.builtin.portals.systems.curse;

import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.builtin.portals.utils.CursedItems;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeleteCursedItemsOnSpawnSystem extends RefSystem<EntityStore> {
   @Override
   public void onEntityAdded(
      @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PortalWorld portalWorld = store.getResource(PortalWorld.getResourceType());
      if (!portalWorld.exists()) {
         Player player = store.getComponent(ref, Player.getComponentType());
         CursedItems.deleteAll(player);
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
      return Player.getComponentType();
   }
}
