package com.hypixel.hytale.builtin.triggervolumes.prefab;

import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.saving.PrefabSaveContributor;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.component.TriggerVolume;
import com.hypixel.hytale.builtin.triggervolumes.component.TriggerVolumeGroup;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class TriggerVolumePrefabContributor implements PrefabSaveContributor {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final Vector3d tempMin = new Vector3d();
   private final Vector3d tempMax = new Vector3d();

   @Override
   public void contribute(@Nonnull BlockSelection selection, @Nonnull World world, @Nonnull Vector3i min, @Nonnull Vector3i max) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      Store<EntityStore> store = world.getEntityStore().getStore();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ComponentType<EntityStore, TriggerVolume> volumeComponentType = plugin.getTriggerVolumeComponentType();
         ComponentType<EntityStore, TriggerVolumeGroup> groupComponentType = plugin.getTriggerVolumeGroupComponentType();
         Collection<PrefabEditingMetadata> allMetadata = getAllPrefabMetadata(store);
         HashMap<String, String> groupLinkIds = new HashMap<>();
         HashSet<String> emittedGroups = new HashSet<>();

         for (VolumeEntry entry : manager.getVolumes()) {
            entry.getShape().getWorldAABB(entry.getPosition(), this.tempMin, this.tempMax);
            if (this.aabbIntersects(this.tempMin, this.tempMax, min, max)
               && (allMetadata == null || allMetadata.size() <= 1 || this.isClosestPrefab(entry, min, max, allMetadata))) {
               Vector3d position = new Vector3d(entry.getPosition());
               position.sub(selection.getX(), selection.getY(), selection.getZ());
               Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
               holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, Rotation3f.IDENTITY));
               TriggerVolume tv = TriggerVolume.fromVolumeEntry(entry);
               if (entry.getGroupId() != null) {
                  String groupId = entry.getGroupId();
                  String groupLinkId = groupLinkIds.computeIfAbsent(groupId, k -> UUID.randomUUID().toString());
                  tv.setGroupLinkId(groupLinkId);
                  if (emittedGroups.add(groupId)) {
                     GroupEntry group = manager.getGroup(groupId);
                     if (group != null) {
                        Vector3d groupPosition = new Vector3d(group.getOrigin());
                        groupPosition.sub(selection.getX(), selection.getY(), selection.getZ());
                        Holder<EntityStore> groupHolder = EntityStore.REGISTRY.newHolder();
                        groupHolder.addComponent(TransformComponent.getComponentType(), new TransformComponent(groupPosition, Rotation3f.IDENTITY));
                        groupHolder.addComponent(groupComponentType, TriggerVolumeGroup.fromGroupEntry(groupLinkId, group));
                        selection.addEntityHolderRaw(groupHolder);
                     }
                  }
               }

               holder.addComponent(volumeComponentType, tv);
               selection.addEntityHolderRaw(holder);
            }
         }
      }
   }

   private boolean aabbIntersects(@Nonnull Vector3d volMin, @Nonnull Vector3d volMax, @Nonnull Vector3i prefabMin, @Nonnull Vector3i prefabMax) {
      return volMax.x >= prefabMin.x
         && volMin.x <= prefabMax.x + 1
         && volMax.y >= prefabMin.y
         && volMin.y <= prefabMax.y + 1
         && volMax.z >= prefabMin.z
         && volMin.z <= prefabMax.z + 1;
   }

   private boolean isClosestPrefab(
      @Nonnull VolumeEntry entry, @Nonnull Vector3i currentMin, @Nonnull Vector3i currentMax, @Nonnull Collection<PrefabEditingMetadata> allMetadata
   ) {
      Vector3d origin = entry.getPosition();
      entry.getShape().getWorldAABB(origin, this.tempMin, this.tempMax);
      double bestDist = Double.MAX_VALUE;
      Vector3i bestMin = null;
      Vector3i bestMax = null;
      boolean overlapsMultiple = false;

      for (PrefabEditingMetadata meta : allMetadata) {
         Vector3i mMin = meta.getMinPoint();
         Vector3i mMax = meta.getMaxPoint();
         if (this.aabbIntersects(this.tempMin, this.tempMax, mMin, mMax)) {
            if (bestMin != null) {
               overlapsMultiple = true;
            }

            double dist = pointToBoxSurfaceDistance(origin, mMin, mMax);
            if (dist < bestDist) {
               bestDist = dist;
               bestMin = mMin;
               bestMax = mMax;
            }
         }
      }

      if (overlapsMultiple) {
         LOGGER.at(Level.WARNING).log("Trigger volume '%s' overlaps multiple prefab bounds; assigning to closest", entry.getId());
      }

      return bestMin != null && bestMin.equals(currentMin) && bestMax != null && bestMax.equals(currentMax);
   }

   private static double pointToBoxSurfaceDistance(@Nonnull Vector3d point, @Nonnull Vector3i boxMin, @Nonnull Vector3i boxMax) {
      double px = point.x();
      double py = point.y();
      double pz = point.z();
      double minX = boxMin.x;
      double minY = boxMin.y;
      double minZ = boxMin.z;
      double maxX = boxMax.x + 1;
      double maxY = boxMax.y + 1;
      double maxZ = boxMax.z + 1;
      if (px >= minX && px <= maxX && py >= minY && py <= maxY && pz >= minZ && pz <= maxZ) {
         return Math.min(Math.min(px - minX, maxX - px), Math.min(Math.min(py - minY, maxY - py), Math.min(pz - minZ, maxZ - pz)));
      } else {
         double dx = Math.max(minX - px, Math.max(0.0, px - maxX));
         double dy = Math.max(minY - py, Math.max(0.0, py - maxY));
         double dz = Math.max(minZ - pz, Math.max(0.0, pz - maxZ));
         return Math.sqrt(dx * dx + dy * dy + dz * dz);
      }
   }

   @Nullable
   private static Collection<PrefabEditingMetadata> getAllPrefabMetadata(@Nonnull Store<EntityStore> store) {
      PrefabEditSession session = store.getResource(PrefabEditSession.getResourceType());
      if (session == null) {
         return null;
      } else {
         Map<UUID, PrefabEditingMetadata> map = session.getLoadedPrefabMetadata();
         return map.isEmpty() ? null : map.values();
      }
   }
}
