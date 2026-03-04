package com.hypixel.hytale.builtin.portals.systems.voidevent;

import com.hypixel.hytale.builtin.ambience.resources.AmbienceResource;
import com.hypixel.hytale.builtin.portals.components.voidevent.VoidEvent;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventConfig;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventStage;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VoidEventRefSystem extends RefSystem<EntityStore> {
   @Override
   public void onEntityAdded(
      @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PortalWorld portalWorld = store.getResource(PortalWorld.getResourceType());
      if (portalWorld.exists()) {
         VoidEventConfig voidEventConfig = portalWorld.getVoidEventConfig();
         String forcedMusic = voidEventConfig.getMusicAmbienceFX();
         if (forcedMusic != null) {
            AmbienceResource ambienceResource = store.getResource(AmbienceResource.getResourceType());
            ambienceResource.setForcedMusicAmbience(forcedMusic);
         }
      }
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PortalWorld portalWorld = store.getResource(PortalWorld.getResourceType());
      if (portalWorld.exists()) {
         World world = store.getExternalData().getWorld();
         VoidEventConfig voidEventConfig = portalWorld.getVoidEventConfig();
         String forcedMusic = voidEventConfig.getMusicAmbienceFX();
         if (forcedMusic != null) {
            AmbienceResource ambienceResource = store.getResource(AmbienceResource.getResourceType());
            ambienceResource.setForcedMusicAmbience(null);
         }

         VoidEvent voidEvent = commandBuffer.getComponent(ref, VoidEvent.getComponentType());
         VoidEventStage activeStage = voidEvent.getActiveStage();
         if (activeStage != null) {
            VoidEventStagesSystem.stopStage(activeStage, store, commandBuffer);
            voidEvent.setActiveStage(null);
         }
      }
   }

   @Nullable
   @Override
   public Query<EntityStore> getQuery() {
      return VoidEvent.getComponentType();
   }
}
