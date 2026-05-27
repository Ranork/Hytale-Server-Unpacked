package com.hypixel.hytale.builtin.portals.systems.voidevent;

import com.hypixel.hytale.builtin.audio.components.ForcedMusicTracker;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventConfig;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class VoidEventPlayerJoinSystem extends HolderSystem<EntityStore> {
   @Nonnull
   private final Query<EntityStore> query;

   public VoidEventPlayerJoinSystem(@Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType) {
      this.query = playerRefComponentType;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      PortalWorld portalWorld = store.getResource(PortalWorld.getResourceType());
      if (portalWorld.exists()) {
         if (portalWorld.isVoidEventActive()) {
            VoidEventConfig voidEventConfig = portalWorld.getVoidEventConfig();
            if (voidEventConfig != null) {
               int musicIndex = voidEventConfig.getMusicContainerIndex();
               if (musicIndex > 0) {
                  holder.ensureComponent(ForcedMusicTracker.getComponentType());
                  ForcedMusicTracker tracker = holder.getComponent(ForcedMusicTracker.getComponentType());
                  if (tracker != null) {
                     tracker.setCurrentContainerIndex(musicIndex);
                  }
               }
            }
         }
      }
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }
}
