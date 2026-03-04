package com.hypixel.hytale.server.spawning.suppression.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawnsuppression.SpawnSuppression;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import com.hypixel.hytale.server.spawning.suppression.SpawnSuppressorEntry;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionController;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class SpawnMarkerSuppressionSystem extends RefSystem<EntityStore> {
   private final ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType;
   private final ResourceType<EntityStore, SpawnSuppressionController> spawnSuppressionControllerResourceType;
   private final ComponentType<EntityStore, UUIDComponent> uuidComponentType = UUIDComponent.getComponentType();
   private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
   @Nonnull
   private final Query<EntityStore> query;

   public SpawnMarkerSuppressionSystem(
      ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType,
      ResourceType<EntityStore, SpawnSuppressionController> spawnSuppressionControllerResourceType
   ) {
      this.spawnMarkerEntityComponentType = spawnMarkerEntityComponentType;
      this.spawnSuppressionControllerResourceType = spawnSuppressionControllerResourceType;
      this.query = Query.and(spawnMarkerEntityComponentType, this.uuidComponentType, this.transformComponentType);
   }

   @Override
   public void onEntityAdded(
      @Nonnull Ref<EntityStore> reference, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      SpawnSuppressionController suppressionController = store.getResource(this.spawnSuppressionControllerResourceType);
      SpawnMarkerEntity marker = store.getComponent(reference, this.spawnMarkerEntityComponentType);
      TransformComponent transform = commandBuffer.getComponent(reference, this.transformComponentType);
      UUIDComponent uuid = commandBuffer.getComponent(reference, this.uuidComponentType);
      Map<UUID, SpawnSuppressorEntry> spawnSuppressorMap = suppressionController.getSpawnSuppressorMap();
      spawnSuppressorMap.forEach((id, entry) -> {
         SpawnSuppression suppression = SpawnSuppression.getAssetMap().getAsset(entry.getSuppressionId());
         if (suppression == null) {
            throw new NullPointerException(String.format("No such suppression with ID %s", entry.getSuppressionId()));
         } else if (suppression.isSuppressSpawnMarkers()) {
            double radius = suppression.getRadius();
            double radiusSquared = radius * radius;
            if (transform.getPosition().distanceSquaredTo(entry.getPosition()) <= radiusSquared) {
               marker.suppress(id);
               SpawningPlugin.get().getLogger().at(Level.FINEST).log("Suppressing spawn marker %s on add/load", uuid.getUuid());
            }
         }
      });
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<EntityStore> reference, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }
}
