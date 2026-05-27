package com.hypixel.hytale.builtin.triggervolumes.prefab;

import com.hypixel.hytale.builtin.triggervolumes.component.TriggerVolume;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefabInstance;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class TriggerVolumeWorldGenHandler extends RefSystem<EntityStore> {
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   private static final ComponentType<EntityStore, FromPrefabInstance> FROM_PREFAB_INSTANCE_COMPONENT_TYPE = FromPrefabInstance.getComponentType();
   @Nonnull
   private final ResourceType<EntityStore, TriggerVolumeManager> managerResourceType;
   @Nonnull
   private final ComponentType<EntityStore, TriggerVolume> triggerVolumeComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public TriggerVolumeWorldGenHandler(
      @Nonnull ResourceType<EntityStore, TriggerVolumeManager> managerResourceType,
      @Nonnull ComponentType<EntityStore, TriggerVolume> triggerVolumeComponentType
   ) {
      this.managerResourceType = managerResourceType;
      this.triggerVolumeComponentType = triggerVolumeComponentType;
      this.query = Query.and(triggerVolumeComponentType, FROM_PREFAB_INSTANCE_COMPONENT_TYPE, TRANSFORM_COMPONENT_TYPE);
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Nullable
   @Override
   public SystemGroup<EntityStore> getGroup() {
      return EntityModule.get().getPreClearMarkersGroup();
   }

   @Override
   public void onEntityAdded(
      @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      if (reason == AddReason.LOAD) {
         commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
      } else {
         TriggerVolumeManager manager = store.getResource(this.managerResourceType);
         if (manager != null) {
            World world = manager.getWorld();
            if (world != null) {
               TriggerVolume tvComponent = store.getComponent(ref, this.triggerVolumeComponentType);
               TransformComponent transform = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);
               FromPrefabInstance fromPrefabInstance = store.getComponent(ref, FROM_PREFAB_INSTANCE_COMPONENT_TYPE);
               if (tvComponent != null && transform != null && transform.getPosition() != null && fromPrefabInstance != null) {
                  String worldName = world.getName().toLowerCase(Locale.ROOT);
                  Vector3d position = new Vector3d(transform.getPosition());
                  long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x(), position.z());
                  if (manager.consumeWorldGenRegenChunk(chunkIndex)) {
                     manager.removeWorldGenVolumesInChunk(chunkIndex);
                  }

                  String id = manager.generateUniqueVolumeId();
                  VolumeEntry entry = tvComponent.toVolumeEntry(id, worldName, position, transform.getRotation().yaw());
                  entry.setFromWorldGen(true);
                  String linkId = tvComponent.getGroupLinkId();
                  if (linkId != null && !linkId.isBlank()) {
                     String newGroupId = manager.ensureWorldGenGroup(fromPrefabInstance.getPrefabInstanceId(), linkId, entry, worldName);
                     if (newGroupId != null) {
                        entry.setGroupId(newGroupId);
                        GroupEntry group = manager.getGroup(newGroupId);
                        if (group != null) {
                           group.addMember(entry.getId());
                        }
                     }
                  }

                  manager.register(id, entry);
                  manager.notifyViewersAdd(entry);
                  commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
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
}
