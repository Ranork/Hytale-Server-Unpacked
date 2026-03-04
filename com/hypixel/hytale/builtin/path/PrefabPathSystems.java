package com.hypixel.hytale.builtin.path;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.path.entities.PatrolPathMarkerEntity;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.builtin.path.path.PatrolPath;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.FromWorldGen;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.WorldGenId;
import com.hypixel.hytale.server.core.modules.entity.system.ModelSystems;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.prefab.event.PrefabPlaceEntityEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabPathSystems {
   public static class AddOrRemove extends HolderSystem<EntityStore> {
      public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      @Nullable
      private static final ComponentType<EntityStore, PatrolPathMarkerEntity> PATH_MARKER_ENTITY_COMPONENT_TYPE = PatrolPathMarkerEntity.getComponentType();
      private static final ComponentType<EntityStore, ModelComponent> MODEL_COMPONENT_TYPE = ModelComponent.getComponentType();
      private static final ResourceType<EntityStore, WorldPathData> STORE_WORLD_PATH_DATA_RESOURCE_TYPE = WorldPathData.getResourceType();
      private static final ComponentType<EntityStore, WorldGenId> WORLD_GEN_ID_COMPONENT_TYPE = WorldGenId.getComponentType();
      private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(new SystemDependency<>(Order.BEFORE, ModelSystems.ModelSpawned.class));

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return PATH_MARKER_ENTITY_COMPONENT_TYPE;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         PatrolPathMarkerEntity pathMarker = holder.getComponent(PATH_MARKER_ENTITY_COMPONENT_TYPE);
         WorldPathData worldPathData = store.getResource(STORE_WORLD_PATH_DATA_RESOURCE_TYPE);
         WorldGenId worldGenIdComponent = holder.getComponent(WORLD_GEN_ID_COMPONENT_TYPE);
         int worldgenId = worldGenIdComponent != null ? worldGenIdComponent.getWorldGenId() : 0;
         String pathName = pathMarker.getPathName();
         UUID pathId = pathMarker.getPathId();
         if (pathId == null) {
            pathId = UUID.nameUUIDFromBytes((pathName + worldgenId).getBytes(StandardCharsets.UTF_8));
            pathMarker.setPathId(pathId);
            int lastIndex = pathName.lastIndexOf(126);
            if (lastIndex != -1) {
               pathMarker.setPathName(pathName.substring(0, lastIndex));
               pathMarker.markNeedsSave();
               LOGGER.at(Level.INFO).log("Migrating path marker from path %s to use new UUID %s", pathName, pathId);
            }
         }

         IPrefabPath path = worldPathData.getOrConstructPrefabPath(worldgenId, pathId, pathName, PatrolPath::new);
         path.addLoadedWaypoint(pathMarker, pathMarker.getTempPathLength(), pathMarker.getOrder(), worldgenId);
         pathMarker.setParentPath(path);
         holder.putComponent(MODEL_COMPONENT_TYPE, new ModelComponent(PathPlugin.get().getPathMarkerModel()));
         pathMarker.markNeedsSave();
         holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
         holder.ensureComponent(PrefabCopyableComponent.getComponentType());
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         PatrolPathMarkerEntity pathMarker = holder.getComponent(PatrolPathMarkerEntity.getComponentType());
         WorldPathData worldPathData = store.getResource(WorldPathData.getResourceType());
         WorldGenId worldGenIdComponent = holder.getComponent(WORLD_GEN_ID_COMPONENT_TYPE);
         int worldgenId = worldGenIdComponent != null ? worldGenIdComponent.getWorldGenId() : 0;
         switch (reason) {
            case UNLOAD:
               worldPathData.unloadPrefabPathWaypoint(worldgenId, pathMarker.getPathId(), pathMarker.getOrder());
               break;
            case REMOVE:
               UUID path = pathMarker.getPathId();
               if (path != null) {
                  worldPathData.removePrefabPathWaypoint(worldgenId, path, pathMarker.getOrder());
               }
         }
      }
   }

   public static class AddedFromWorldGen extends HolderSystem<EntityStore> {
      @Nullable
      private static final ComponentType<EntityStore, PatrolPathMarkerEntity> PATH_MARKER_ENTITY_COMPONENT_TYPE = PatrolPathMarkerEntity.getComponentType();
      private static final ComponentType<EntityStore, WorldGenId> WORLD_GEN_ID_COMPONENT_TYPE = WorldGenId.getComponentType();
      private static final ComponentType<EntityStore, FromWorldGen> FROM_WORLD_GEN_COMPONENT_TYPE = FromWorldGen.getComponentType();
      private static final Query<EntityStore> QUERY = Query.and(PATH_MARKER_ENTITY_COMPONENT_TYPE, FROM_WORLD_GEN_COMPONENT_TYPE);
      private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(new SystemDependency<>(Order.BEFORE, PrefabPathSystems.AddOrRemove.class));

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityModule.get().getPreClearMarkersGroup();
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.putComponent(WORLD_GEN_ID_COMPONENT_TYPE, new WorldGenId(holder.getComponent(FROM_WORLD_GEN_COMPONENT_TYPE).getWorldGenId()));
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class NameplateHolderSystem extends HolderSystem<EntityStore> {
      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.of(PatrolPathMarkerEntity.getComponentType());
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         PatrolPathMarkerEntity patrolPathMarkerComponent = holder.getComponent(PatrolPathMarkerEntity.getComponentType());

         assert patrolPathMarkerComponent != null;

         DisplayNameComponent displayNameComponent = holder.getComponent(DisplayNameComponent.getComponentType());
         String displayName = "";
         if (displayNameComponent == null) {
            String legacyDisplayName = patrolPathMarkerComponent.getLegacyDisplayName();
            displayName = legacyDisplayName != null ? legacyDisplayName : "Path Marker";
            Message legacyDisplayNameMessage = Message.raw(displayName);
            displayNameComponent = new DisplayNameComponent(legacyDisplayNameMessage);
            holder.putComponent(DisplayNameComponent.getComponentType(), displayNameComponent);
         }

         Nameplate nameplateComponent = holder.getComponent(Nameplate.getComponentType());
         if (nameplateComponent == null) {
            holder.putComponent(Nameplate.getComponentType(), new Nameplate(displayName));
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class NameplateRefChangeSystem extends RefChangeSystem<EntityStore, DisplayNameComponent> {
      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return PatrolPathMarkerEntity.getComponentType();
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, DisplayNameComponent> componentType() {
         return DisplayNameComponent.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull DisplayNameComponent component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Nameplate nameplateComponent = commandBuffer.ensureAndGetComponent(ref, Nameplate.getComponentType());
         nameplateComponent.setText(component.getDisplayName() != null ? component.getDisplayName().getAnsiMessage() : "");
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         DisplayNameComponent oldComponent,
         @Nonnull DisplayNameComponent newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Nameplate nameplateComponent = commandBuffer.ensureAndGetComponent(ref, Nameplate.getComponentType());
         nameplateComponent.setText(newComponent.getDisplayName() != null ? newComponent.getDisplayName().getAnsiMessage() : "");
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull DisplayNameComponent component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Nameplate nameplateComponent = commandBuffer.ensureAndGetComponent(ref, Nameplate.getComponentType());
         nameplateComponent.setText("");
      }
   }

   public static class PrefabPlaceEntityEventSystem extends WorldEventSystem<EntityStore, PrefabPlaceEntityEvent> {
      public PrefabPlaceEntityEventSystem() {
         super(PrefabPlaceEntityEvent.class);
      }

      public void handle(@Nonnull Store store, @Nonnull CommandBuffer commandBuffer, @Nonnull PrefabPlaceEntityEvent event) {
         Holder<EntityStore> holder = event.getHolder();
         PatrolPathMarkerEntity patrolPathMarkerComponent = holder.getComponent(PatrolPathMarkerEntity.getComponentType());
         if (patrolPathMarkerComponent != null) {
            String pathName = patrolPathMarkerComponent.getPathName();
            UUID pathId = patrolPathMarkerComponent.getPathId();
            if (pathId == null) {
               String newPathName = pathName.substring(0, pathName.lastIndexOf(126));
               patrolPathMarkerComponent.setPathName(newPathName);
            }

            UUID newPathId = BuilderToolsPlugin.get().getNewPathIdOnPrefabPasted(pathId, patrolPathMarkerComponent.getPathName(), event.getPrefabId());
            patrolPathMarkerComponent.setPathId(newPathId);
         }
      }
   }

   public static class WorldGenChangeSystem extends RefChangeSystem<EntityStore, WorldGenId> {
      @Nonnull
      private static final Message MESSAGE_PREFABS_UNKNOWN = Message.translation("server.prefabs.unknown");

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return PatrolPathMarkerEntity.getComponentType();
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, WorldGenId> componentType() {
         return WorldGenId.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull WorldGenId component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         PatrolPathMarkerEntity patrolPathMarkerComponent = commandBuffer.getComponent(ref, PatrolPathMarkerEntity.getComponentType());

         assert patrolPathMarkerComponent != null;

         String displayName = PatrolPathMarkerEntity.generateDisplayName(component.getWorldGenId(), patrolPathMarkerComponent);
         Message displayNameMessage = Message.raw(displayName);
         commandBuffer.putComponent(ref, DisplayNameComponent.getComponentType(), new DisplayNameComponent(displayNameMessage));
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         @Nullable WorldGenId oldComponent,
         @Nonnull WorldGenId newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         PatrolPathMarkerEntity patrolPathMarkerComponent = commandBuffer.getComponent(ref, PatrolPathMarkerEntity.getComponentType());

         assert patrolPathMarkerComponent != null;

         String displayName = PatrolPathMarkerEntity.generateDisplayName(newComponent.getWorldGenId(), patrolPathMarkerComponent);
         Message displayNameMessage = Message.raw(displayName);
         commandBuffer.putComponent(ref, DisplayNameComponent.getComponentType(), new DisplayNameComponent(displayNameMessage));
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull WorldGenId component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.putComponent(ref, DisplayNameComponent.getComponentType(), new DisplayNameComponent(MESSAGE_PREFABS_UNKNOWN));
      }
   }
}
