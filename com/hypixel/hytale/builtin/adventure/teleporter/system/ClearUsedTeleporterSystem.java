package com.hypixel.hytale.builtin.adventure.teleporter.system;

import com.hypixel.hytale.builtin.adventure.teleporter.interaction.server.UsedTeleporter;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportRecord;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ClearUsedTeleporterSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   public static final Duration TELEPORTER_GLOBAL_COOLDOWN = Duration.ofMillis(100L);
   @Nonnull
   private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(new SystemDependency<>(Order.AFTER, TeleportSystems.PlayerMoveSystem.class));
   @Nonnull
   private final ComponentType<EntityStore, UsedTeleporter> usedTeleporterComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TeleportRecord> teleportRecordComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Teleport> teleportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PendingTeleport> pendingTeleportComponentType;

   public ClearUsedTeleporterSystem(
      @Nonnull ComponentType<EntityStore, UsedTeleporter> usedTeleporterComponentType,
      @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
      @Nonnull ComponentType<EntityStore, TeleportRecord> teleportRecordComponentType,
      @Nonnull ComponentType<EntityStore, Teleport> teleportComponentType,
      @Nonnull ComponentType<EntityStore, PendingTeleport> pendingTeleportComponentType
   ) {
      this.usedTeleporterComponentType = usedTeleporterComponentType;
      this.transformComponentType = transformComponentType;
      this.teleportRecordComponentType = teleportRecordComponentType;
      this.teleportComponentType = teleportComponentType;
      this.pendingTeleportComponentType = pendingTeleportComponentType;
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return DEPENDENCIES;
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      World world = store.getExternalData().getWorld();
      if (this.shouldClear(world, index, archetypeChunk)) {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         commandBuffer.removeComponent(ref, this.usedTeleporterComponentType);
      }
   }

   private boolean shouldClear(@Nonnull World world, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk) {
      UsedTeleporter usedTeleporter = archetypeChunk.getComponent(index, this.usedTeleporterComponentType);

      assert usedTeleporter != null;

      TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);
      TeleportRecord teleportRecord = archetypeChunk.getComponent(index, this.teleportRecordComponentType);
      if (transformComponent == null) {
         return true;
      } else {
         Archetype<EntityStore> archetype = archetypeChunk.getArchetype();
         if (!archetype.contains(this.teleportComponentType) && !archetype.contains(this.pendingTeleportComponentType)) {
            if (teleportRecord != null && !teleportRecord.hasElapsedSinceLastTeleport(System.nanoTime(), TELEPORTER_GLOBAL_COOLDOWN)) {
               return false;
            } else {
               UUID destinationWorldUuid = usedTeleporter.getDestinationWorldUuid();
               if (destinationWorldUuid != null && !world.getWorldConfig().getUuid().equals(destinationWorldUuid)) {
                  return true;
               } else {
                  Vector3d entityPosition = transformComponent.getPosition();
                  Vector3d destinationPosition = usedTeleporter.getDestinationPosition();
                  double deltaY = Math.abs(entityPosition.y - destinationPosition.y);
                  double distanceXZsq = Vector2d.distanceSquared(entityPosition.x, entityPosition.z, destinationPosition.x, destinationPosition.z);
                  return deltaY > usedTeleporter.getClearOutY() || distanceXZsq > usedTeleporter.getClearOutXZ();
               }
            }
         } else {
            return false;
         }
      }
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.usedTeleporterComponentType;
   }
}
