package com.hypixel.hytale.builtin.triggervolumes.prefab;

import com.hypixel.hytale.builtin.triggervolumes.component.TriggerVolume;
import com.hypixel.hytale.builtin.triggervolumes.component.TriggerVolumeGroup;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.event.PrefabPlaceEntityEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class TriggerVolumePasteHandler extends WorldEventSystem<EntityStore, PrefabPlaceEntityEvent> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final ResourceType<EntityStore, TriggerVolumeManager> managerResourceType;
   private final ComponentType<EntityStore, TriggerVolume> triggerVolumeComponentType;
   private final ComponentType<EntityStore, TriggerVolumeGroup> triggerVolumeGroupComponentType;

   public TriggerVolumePasteHandler(
      @Nonnull ResourceType<EntityStore, TriggerVolumeManager> managerResourceType,
      @Nonnull ComponentType<EntityStore, TriggerVolume> triggerVolumeComponentType,
      @Nonnull ComponentType<EntityStore, TriggerVolumeGroup> triggerVolumeGroupComponentType
   ) {
      super(PrefabPlaceEntityEvent.class);
      this.managerResourceType = managerResourceType;
      this.triggerVolumeComponentType = triggerVolumeComponentType;
      this.triggerVolumeGroupComponentType = triggerVolumeGroupComponentType;
   }

   public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PrefabPlaceEntityEvent event) {
      Holder<EntityStore> holder = event.getHolder();
      TriggerVolume tvComponent = holder.getComponent(this.triggerVolumeComponentType);
      TriggerVolumeGroup groupComponent = holder.getComponent(this.triggerVolumeGroupComponentType);
      if (tvComponent != null || groupComponent != null) {
         event.setCancelled(true);
         TriggerVolumeManager manager = null;

         try {
            TransformComponent transform = holder.getComponent(TransformComponent.getComponentType());
            if (transform == null || transform.getPosition() == null) {
               return;
            }

            manager = store.getResource(this.managerResourceType);
            if (manager == null) {
               return;
            }

            World world = manager.getWorld();
            if (world == null) {
               return;
            }

            String worldName = world.getName().toLowerCase(Locale.ROOT);
            Vector3d position = new Vector3d(transform.getPosition());
            if (groupComponent != null) {
               String linkId = groupComponent.getGroupLinkId();
               if (linkId != null && !linkId.isBlank()) {
                  manager.upsertGroupForPrefabLink(linkId, groupComponent, worldName, position);
               }

               return;
            }

            String id = manager.generateUniqueVolumeId();
            VolumeEntry entry = tvComponent.toVolumeEntry(id, worldName, position, transform.getRotation().yaw());
            String linkId = tvComponent.getGroupLinkId();
            if (linkId != null && !linkId.isBlank()) {
               String newGroupId = manager.ensureGroupForPrefabLink(linkId, entry, worldName);
               entry.setGroupId(newGroupId);
               GroupEntry group = manager.getGroup(newGroupId);
               if (group != null) {
                  group.addMember(entry.getId());
               }
            }

            manager.register(id, entry);
            manager.notifyViewersAdd(entry);
            manager.markSpatialDirty();
         } catch (Exception var17) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var17)).log("Skipping malformed trigger volume prefab entity");
            if (manager != null) {
               manager.notifyViewersLegacyVolumeSkipped();
            }
         }
      }
   }
}
