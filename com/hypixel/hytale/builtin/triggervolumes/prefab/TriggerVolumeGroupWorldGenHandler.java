package com.hypixel.hytale.builtin.triggervolumes.prefab;

import com.hypixel.hytale.builtin.triggervolumes.component.TriggerVolumeGroup;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
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
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefabInstance;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class TriggerVolumeGroupWorldGenHandler extends RefSystem<EntityStore> {
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   private static final ComponentType<EntityStore, FromPrefabInstance> FROM_PREFAB_INSTANCE_COMPONENT_TYPE = FromPrefabInstance.getComponentType();
   @Nonnull
   private final ResourceType<EntityStore, TriggerVolumeManager> managerResourceType;
   @Nonnull
   private final ComponentType<EntityStore, TriggerVolumeGroup> triggerVolumeGroupComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public TriggerVolumeGroupWorldGenHandler(
      @Nonnull ResourceType<EntityStore, TriggerVolumeManager> managerResourceType,
      @Nonnull ComponentType<EntityStore, TriggerVolumeGroup> triggerVolumeGroupComponentType
   ) {
      this.managerResourceType = managerResourceType;
      this.triggerVolumeGroupComponentType = triggerVolumeGroupComponentType;
      this.query = Query.and(triggerVolumeGroupComponentType, FROM_PREFAB_INSTANCE_COMPONENT_TYPE, TRANSFORM_COMPONENT_TYPE);
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
               TriggerVolumeGroup groupComponent = store.getComponent(ref, this.triggerVolumeGroupComponentType);
               TransformComponent transform = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);
               FromPrefabInstance fromPrefabInstance = store.getComponent(ref, FROM_PREFAB_INSTANCE_COMPONENT_TYPE);
               if (groupComponent != null && transform != null && transform.getPosition() != null && fromPrefabInstance != null) {
                  String linkId = groupComponent.getGroupLinkId();
                  if (linkId != null && !linkId.isBlank()) {
                     String worldName = world.getName().toLowerCase(Locale.ROOT);
                     manager.upsertWorldGenGroup(
                        fromPrefabInstance.getPrefabInstanceId(), linkId, groupComponent, worldName, new Vector3d(transform.getPosition())
                     );
                  }

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
