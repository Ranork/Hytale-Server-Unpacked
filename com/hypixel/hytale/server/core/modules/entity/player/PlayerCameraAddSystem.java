package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerCameraAddSystem extends HolderSystem<EntityStore> {
   private static final ComponentType<EntityStore, CameraManager> CAMERA_MANAGER_COMPONENT_TYPE = CameraManager.getComponentType();
   private static final Query<EntityStore> QUERY = Query.and(PlayerRef.getComponentType(), Query.not(CAMERA_MANAGER_COMPONENT_TYPE));

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return QUERY;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      holder.ensureComponent(CAMERA_MANAGER_COMPONENT_TYPE);
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }
}
