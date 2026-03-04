package com.hypixel.hytale.builtin.portals.systems.voidevent;

import com.hypixel.hytale.builtin.portals.components.voidevent.VoidSpawner;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.InvasionPortalConfig;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventConfig;
import com.hypixel.hytale.builtin.portals.integrations.PortalGameplayConfig;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VoidSpawnerSystems {
   @Nonnull
   private static final Query<EntityStore> QUERY = Query.and(VoidSpawner.getComponentType(), TransformComponent.getComponentType());

   public static class Instantiate extends RefSystem<EntityStore> {
      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         HytaleLogger.getLogger().at(Level.INFO).log("Adding void spawner...");
         World world = store.getExternalData().getWorld();
         PortalGameplayConfig gameplayConfig = world.getGameplayConfig().getPluginConfig().get(PortalGameplayConfig.class);
         VoidEventConfig voidEventConfig = gameplayConfig.getVoidEvent();
         InvasionPortalConfig invasionPortalConfig = voidEventConfig.getInvasionPortalConfig();
         List<String> spawnBeacons = invasionPortalConfig.getSpawnBeaconsList();
         if (spawnBeacons.isEmpty()) {
            HytaleLogger.getLogger()
               .at(Level.WARNING)
               .log("No spawn beacons configured for void spawn in GameplayConfig for portal world (no mobs will spawn during void event)");
         } else {
            VoidSpawner voidSpawner = commandBuffer.getComponent(ref, VoidSpawner.getComponentType());
            TransformComponent transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
            Vector3d position = transform.getPosition();

            for (int i = 0; i < spawnBeacons.size(); i++) {
               String spawnBeacon = spawnBeacons.get(i);
               Vector3d beaconPos = position.clone().add(0.0, 0.5 + 0.1 * i, 0.0);
               int beaconAssetId = BeaconNPCSpawn.getAssetMap().getIndexOrDefault(spawnBeacon, -1);
               if (beaconAssetId == -1) {
                  HytaleLogger.getLogger().at(Level.WARNING).log("No asset found for spawn beacon \"" + spawnBeacon + "\" in GameplayConfig for portal world");
               } else {
                  BeaconSpawnWrapper beaconSpawnWrapper = SpawningPlugin.get().getBeaconSpawnWrapper(beaconAssetId);
                  Holder<EntityStore> spawnBeaconRef = LegacySpawnBeaconEntity.createHolder(beaconSpawnWrapper, beaconPos, transform.getRotation());
                  commandBuffer.addEntity(spawnBeaconRef, AddReason.SPAWN);
                  UUID beaconUuid = spawnBeaconRef.getComponent(UUIDComponent.getComponentType()).getUuid();
                  voidSpawner.getSpawnBeaconUuids().add(beaconUuid);
               }
            }

            String onSpawnParticles = invasionPortalConfig.getOnSpawnParticles();
            if (onSpawnParticles != null) {
               ParticleUtil.spawnParticleEffect(onSpawnParticles, position, commandBuffer);
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         World world = store.getExternalData().getWorld();
         VoidSpawner voidSpawnerComponent = commandBuffer.getComponent(ref, VoidSpawner.getComponentType());

         assert voidSpawnerComponent != null;

         for (UUID spawnBeaconUuid : voidSpawnerComponent.getSpawnBeaconUuids()) {
            Ref<EntityStore> spawnBeaconRef = world.getEntityStore().getRefFromUUID(spawnBeaconUuid);
            if (spawnBeaconRef != null) {
               commandBuffer.removeEntity(spawnBeaconRef, RemoveReason.REMOVE);
            }
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return VoidSpawnerSystems.QUERY;
      }
   }
}
