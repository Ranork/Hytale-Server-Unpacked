package com.hypixel.hytale.builtin.triggervolumes.prefab;

import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.prefab.event.PrefabPasteEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TriggerVolumePrefabPasteRemapSystem extends WorldEventSystem<EntityStore, PrefabPasteEvent> {
   @Nonnull
   private final ResourceType<EntityStore, TriggerVolumeManager> managerResourceType;

   public TriggerVolumePrefabPasteRemapSystem(@Nonnull ResourceType<EntityStore, TriggerVolumeManager> managerResourceType) {
      super(PrefabPasteEvent.class);
      this.managerResourceType = managerResourceType;
   }

   public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PrefabPasteEvent event) {
      if (event.isPasteStart()) {
         TriggerVolumeManager manager = store.getResource(this.managerResourceType);
         if (manager != null) {
            manager.clearPrefabGroupLinkRemap();
         }
      }
   }
}
