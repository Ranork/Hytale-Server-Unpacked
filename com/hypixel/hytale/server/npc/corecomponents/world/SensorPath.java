package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.path.PathPlugin;
import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.entities.PatrolPathMarkerEntity;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.component.WorldGenId;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.IPathWaypoint;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorPath;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.entities.PathManager;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PathProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorPath extends SensorBase {
   protected final double range;
   protected final SensorPath.PathType pathType;
   protected final Vector3d closestWaypoint = new Vector3d(Vector3d.MIN);
   protected final HashSet<UUID> disallowedPaths = new HashSet<>();
   protected final PathProvider pathProvider = new PathProvider();
   protected final PositionProvider positionProvider = new PositionProvider(null, this.pathProvider);
   protected final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> prefabPathSpatialResource;
   @Nullable
   protected final ComponentType<EntityStore, PatrolPathMarkerEntity> patrolPathMarkerEntityComponentType;
   protected final ComponentType<EntityStore, WorldGenId> worldGenIdComponentType;
   @Nullable
   protected String path;
   protected int pathIndex;
   protected int pathChangeRevision;
   protected double distanceSquared = Double.MAX_VALUE;
   @Nonnull
   protected SensorPath.LoadStatus loadStatus = SensorPath.LoadStatus.WAITING;

   public SensorPath(@Nonnull BuilderSensorPath builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.path = builder.getPath(support);
      if (this.path != null && !this.path.isEmpty()) {
         this.pathIndex = AssetRegistry.getOrCreateTagIndex(this.path);
      }

      this.range = builder.getRange(support);
      this.pathType = builder.getPathType(support);
      this.prefabPathSpatialResource = PathPlugin.get().getPrefabPathSpatialResource();
      this.patrolPathMarkerEntityComponentType = PatrolPathMarkerEntity.getComponentType();
      this.worldGenIdComponentType = WorldGenId.getComponentType();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (super.matches(ref, role, dt, store) && this.loadStatus != SensorPath.LoadStatus.FAILED) {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         Vector3d position = transformComponent.getPosition();
         PathManager pathManager = npcComponent.getPathManager();
         int newRevision = NPCPlugin.get().getPathChangeRevision();
         boolean newPathRequested = role.getWorldSupport().consumeNewPathRequested();
         if (pathManager.isFollowingPath() && newRevision == this.pathChangeRevision && !newPathRequested) {
            IPath<?> path = pathManager.getPath(ref, store);
            if (path == null) {
               this.pathProvider.clear();
               this.positionProvider.clear();
               return false;
            } else {
               this.findClosestWaypoint(path, position, this.closestWaypoint, store);
               if (this.pathMatches(path) && this.isInRange(this.distanceSquared)) {
                  this.pathProvider.setPath((IPath<? extends IPathWaypoint>)path);
                  this.positionProvider.setTarget(this.closestWaypoint);
                  return true;
               } else {
                  this.pathProvider.clear();
                  this.positionProvider.clear();
                  return false;
               }
            }
         } else {
            this.pathChangeRevision = newRevision;
            if (newPathRequested && pathManager.isFollowingPath()) {
               UUID pathId = pathManager.getCurrentPathHint();
               if (pathId != null) {
                  this.disallowedPaths.add(pathId);
               }
            }

            IPath<?> path = this.findPath(ref, position, store, this.disallowedPaths, newPathRequested);
            if (path == null) {
               this.pathProvider.clear();
               this.positionProvider.clear();
               return false;
            } else {
               this.closestWaypoint.assign(Vector3d.MIN);
               this.findClosestWaypoint(path, position, this.closestWaypoint, store);
               if (!this.isInRange(this.distanceSquared)) {
                  this.pathProvider.clear();
                  this.positionProvider.clear();
                  return false;
               } else {
                  if (this.pathType == SensorPath.PathType.WorldPath) {
                     pathManager.setTransientPath(path);
                  } else {
                     pathManager.setPrefabPath(path.getId(), (IPrefabPath)path);
                     store.putComponent(ref, this.worldGenIdComponentType, new WorldGenId(((IPrefabPath)path).getWorldGenId()));
                  }

                  this.disallowedPaths.clear();
                  this.pathProvider.setPath((IPath<? extends IPathWaypoint>)path);
                  this.positionProvider.setTarget(this.closestWaypoint);
                  return true;
               }
            }
         }
      } else {
         this.pathProvider.clear();
         this.positionProvider.clear();
         return false;
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }

   protected boolean pathMatches(@Nonnull IPath<?> path) {
      return this.path == null || this.path.isEmpty() || this.path.equals(path.getName());
   }

   protected boolean isInRange(double squaredDistance) {
      return this.range <= 0.0 || this.range * this.range > squaredDistance;
   }

   @Nullable
   protected IPath<?> findPath(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d position,
      @Nonnull Store<EntityStore> store,
      @Nonnull Set<UUID> disallowedPaths,
      boolean newPathRequested
   ) {
      World world = store.getExternalData().getWorld();
      NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      PathManager pathManager = npcComponent.getPathManager();
      WorldGenId worldGenIdComponent = store.getComponent(ref, this.worldGenIdComponentType);
      int worldGenId = worldGenIdComponent != null ? worldGenIdComponent.getWorldGenId() : 0;
      IPath<? extends IPathWaypoint> path;
      switch (this.pathType) {
         case WorldPath:
            path = world.getWorldPathConfig().getPath(this.path);
            if (path == null) {
               NPCPlugin.get().getLogger().at(Level.WARNING).log("Path sensor: Path %s does not exist", this.path);
               this.loadStatus = SensorPath.LoadStatus.FAILED;
               return null;
            }
            break;
         case CurrentPrefabPath:
            WorldPathData worldPathData = store.getResource(WorldPathData.getResourceType());
            if (this.path == null) {
               path = worldPathData.getNearestPrefabPath(worldGenId, position, disallowedPaths, store);
            } else {
               UUID entityPath = pathManager.getCurrentPathHint();
               if (entityPath != null && !newPathRequested) {
                  path = worldPathData.getPrefabPath(worldGenId, entityPath, false);
               } else {
                  path = worldPathData.getNearestPrefabPath(worldGenId, this.pathIndex, position, disallowedPaths, store);
               }
            }

            if (path == null || !((IPrefabPath)path).isFullyLoaded()) {
               this.loadStatus = SensorPath.LoadStatus.WAITING;
               return null;
            }

            this.path = path.getName();
            break;
         case AnyPrefabPath:
            SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.prefabPathSpatialResource);
            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            spatialResource.getSpatialStructure().ordered(position, this.range, results);
            if (results.isEmpty()) {
               this.loadStatus = SensorPath.LoadStatus.WAITING;
               return null;
            }

            double nearest2 = Double.MAX_VALUE;
            PatrolPathMarkerEntity nearestWaypoint = null;
            int i = 0;

            for (; i < results.size(); i++) {
               Ref<EntityStore> eRef = (Ref<EntityStore>)results.get(i);
               PatrolPathMarkerEntity ePatrolPathMarkerEntityComponent = store.getComponent(eRef, this.patrolPathMarkerEntityComponentType);

               assert ePatrolPathMarkerEntityComponent != null;

               if (!disallowedPaths.contains(ePatrolPathMarkerEntityComponent.getParentPath().getId())) {
                  TransformComponent eTransformComponent = store.getComponent(eRef, TransformComponent.getComponentType());

                  assert eTransformComponent != null;

                  double dist2 = position.distanceSquaredTo(eTransformComponent.getPosition());
                  if (dist2 < nearest2) {
                     nearest2 = dist2;
                     nearestWaypoint = ePatrolPathMarkerEntityComponent;
                  }
               }
            }

            if (nearestWaypoint == null) {
               this.loadStatus = SensorPath.LoadStatus.WAITING;
               return null;
            }

            path = nearestWaypoint.getParentPath();
            if (path == null || !((IPrefabPath)path).isFullyLoaded()) {
               this.loadStatus = SensorPath.LoadStatus.WAITING;
               return null;
            }
            break;
         case TransientPath:
            path = (IPath<? extends IPathWaypoint>)pathManager.getPath(ref, store);
            this.path = null;
            break;
         default:
            throw new IllegalStateException();
      }

      this.loadStatus = SensorPath.LoadStatus.SUCCESS;
      return path;
   }

   protected void findClosestWaypoint(
      @Nonnull IPath<?> path, @Nonnull Vector3d position, @Nonnull Vector3d cachedTarget, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      double prevDistanceSquared = this.distanceSquared;
      if (!cachedTarget.equals(Vector3d.MIN)) {
         double newDistance = position.distanceSquaredTo(cachedTarget);
         if (newDistance <= this.distanceSquared) {
            this.distanceSquared = newDistance;
            return;
         }
      }

      this.distanceSquared = Double.MAX_VALUE;

      for (int i = 0; i < path.length(); i++) {
         IPathWaypoint pathWaypoint = path.get(i);
         if (pathWaypoint != null) {
            Vector3d waypoint = pathWaypoint.getWaypointPosition(componentAccessor);
            double distance = position.distanceSquaredTo(waypoint);
            if (distance < this.distanceSquared) {
               this.distanceSquared = distance;
               cachedTarget.assign(waypoint);
            }
         }
      }

      if (this.distanceSquared == Double.MAX_VALUE) {
         this.distanceSquared = prevDistanceSquared;
      }
   }

   protected static enum LoadStatus {
      WAITING,
      FAILED,
      SUCCESS;
   }

   public static enum PathType implements Supplier<String> {
      WorldPath("named world path"),
      CurrentPrefabPath("a path from the prefab the NPC spawned in"),
      AnyPrefabPath("a path from any prefab"),
      TransientPath("a transient path (testing purposes only)");

      private final String description;

      private PathType(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
