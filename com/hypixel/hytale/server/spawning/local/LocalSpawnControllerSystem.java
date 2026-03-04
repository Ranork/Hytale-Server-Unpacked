package com.hypixel.hytale.server.spawning.local;

import com.hypixel.hytale.builtin.weather.components.WeatherTracker;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.function.function.TriIntObjectDoubleToByteFunction;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.LightType;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import com.hypixel.hytale.server.spawning.util.LightRangePredicate;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class LocalSpawnControllerSystem extends TickingSystem<EntityStore> {
   public static final double RUN_FREQUENCY_SECONDS = 5.0;
   private static final int LIGHT_LEVEL_EVALUATION_RADIUS = 4;
   private final Archetype<EntityStore> controllerArchetype;
   private final ComponentType<EntityStore, LocalSpawnController> spawnControllerComponentType;
   private final ComponentType<EntityStore, TransformComponent> transformComponentype;
   private final ComponentType<EntityStore, WeatherTracker> weatherTrackerComponentType;
   private final ComponentType<EntityStore, LocalSpawnBeacon> localSpawnBeaconComponentType;
   private final ComponentType<EntityStore, LegacySpawnBeaconEntity> spawnBeaconComponentType;
   private final ResourceType<EntityStore, LocalSpawnState> localSpawnStateResourceType;
   private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> beaconSpatialComponent;

   public LocalSpawnControllerSystem(
      ComponentType<EntityStore, LocalSpawnController> spawnControllerComponentType,
      ComponentType<EntityStore, TransformComponent> transformComponentype,
      ComponentType<EntityStore, WeatherTracker> weatherTrackerComponentType,
      ComponentType<EntityStore, LocalSpawnBeacon> localSpawnBeaconComponentType,
      ComponentType<EntityStore, LegacySpawnBeaconEntity> spawnBeaconComponentType,
      ResourceType<EntityStore, LocalSpawnState> localSpawnStateResourceType,
      ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> beaconSpatialComponent
   ) {
      this.spawnControllerComponentType = spawnControllerComponentType;
      this.transformComponentype = transformComponentype;
      this.weatherTrackerComponentType = weatherTrackerComponentType;
      this.localSpawnBeaconComponentType = localSpawnBeaconComponentType;
      this.spawnBeaconComponentType = spawnBeaconComponentType;
      this.localSpawnStateResourceType = localSpawnStateResourceType;
      this.beaconSpatialComponent = beaconSpatialComponent;
      this.controllerArchetype = Archetype.of(spawnControllerComponentType, PlayerRef.getComponentType(), transformComponentype, weatherTrackerComponentType);
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      LocalSpawnState localSpawnState = store.getResource(this.localSpawnStateResourceType);
      List<Ref<EntityStore>> controllers = localSpawnState.getLocalControllerList();
      store.forEachChunk(this.controllerArchetype, (archetypeChunk, commandBuffer) -> {
         for (int indexx = 0; indexx < archetypeChunk.size(); indexx++) {
            LocalSpawnController spawnControllerComponentx = archetypeChunk.getComponent(indexx, this.spawnControllerComponentType);

            assert spawnControllerComponentx != null;

            if (spawnControllerComponentx.tickTimeToNextRunSeconds(dt)) {
               controllers.add(archetypeChunk.getReferenceTo(indexx));
            }
         }
      });
      if (!controllers.isEmpty()) {
         World world = store.getExternalData().getWorld();
         List<LegacySpawnBeaconEntity> pendingSpawns = localSpawnState.getLocalPendingSpawns();
         ObjectList<Ref<EntityStore>> existingBeacons = SpatialResource.getThreadLocalReferenceList();

         for (int index = 0; index < controllers.size(); index++) {
            Ref<EntityStore> reference = controllers.get(index);
            LocalSpawnController spawnControllerComponent = store.getComponent(reference, this.spawnControllerComponentType);

            assert spawnControllerComponent != null;

            PlayerRef playerRefComponent = store.getComponent(reference, PlayerRef.getComponentType());

            assert playerRefComponent != null;

            SpawningPlugin.get().getLogger().at(Level.FINE).log("Running local spawn controller for player %s", playerRefComponent.getUsername());
            TransformComponent transformComponent = store.getComponent(reference, this.transformComponentype);

            assert transformComponent != null;

            WeatherTracker weatherTrackerComponent = store.getComponent(reference, this.weatherTrackerComponentType);

            assert weatherTrackerComponent != null;

            weatherTrackerComponent.updateEnvironment(transformComponent, store);
            int environmentIndex = weatherTrackerComponent.getEnvironmentId();
            List<BeaconSpawnWrapper> possibleBeacons = SpawningPlugin.get().getBeaconSpawnsForEnvironment(environmentIndex);
            if (possibleBeacons != null && !possibleBeacons.isEmpty()) {
               BeaconSpawnWrapper firstBeacon = possibleBeacons.getFirst();
               double largestDistance = firstBeacon.getBeaconRadius();
               int[] firstRange = firstBeacon.getSpawn().getYRange();
               int lowestY = firstRange[0];
               int highestY = firstRange[1];

               for (int i = 1; i < possibleBeacons.size(); i++) {
                  BeaconSpawnWrapper beacon = possibleBeacons.get(i);
                  double radius = beacon.getBeaconRadius();
                  if (radius > largestDistance) {
                     largestDistance = radius;
                  }

                  int[] yRange = beacon.getSpawn().getYRange();
                  if (yRange[0] < lowestY) {
                     lowestY = yRange[0];
                  }

                  if (yRange[1] > highestY) {
                     highestY = yRange[1];
                  }
               }

               largestDistance *= 2.0;
               Vector3d position = transformComponent.getPosition();
               double largestDistanceSquared = largestDistance * largestDistance;
               int yDistance = Math.abs(lowestY) + Math.abs(highestY);
               int y = MathUtil.floor(position.getY());
               int minY = Math.max(0, y - yDistance);
               int maxY = Math.min(319, y + yDistance);
               SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.beaconSpatialComponent);
               spatialResource.getSpatialStructure().ordered(position, largestDistance, existingBeacons);
               WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
               double sunlightFactor = worldTimeResource.getSunlightFactor();
               int xPos = MathUtil.floor(position.getX());
               int yPos = MathUtil.floor(position.getY());
               int zPos = MathUtil.floor(position.getZ());
               Object2ByteOpenHashMap<LightType> averageLightValues = new Object2ByteOpenHashMap();
               averageLightValues.defaultReturnValue((byte)-1);

               label134:
               for (int i = 0; i < possibleBeacons.size(); i++) {
                  BeaconSpawnWrapper possibleBeacon = possibleBeacons.get(i);
                  if (possibleBeacon.spawnParametersMatch(store)) {
                     for (int j = 0; j < existingBeacons.size(); j++) {
                        Ref<EntityStore> existingBeaconReference = (Ref<EntityStore>)existingBeacons.get(j);
                        LegacySpawnBeaconEntity existingBeaconComponent = store.getComponent(existingBeaconReference, this.spawnBeaconComponentType);

                        assert existingBeaconComponent != null;

                        TransformComponent existingBeaconTransformComponent = store.getComponent(existingBeaconReference, this.transformComponentype);

                        assert existingBeaconTransformComponent != null;

                        double existingY = existingBeaconTransformComponent.getPosition().getY();
                        if (!(existingY > maxY) && !(existingY < minY)) {
                           int existingBeaconIndex = existingBeaconComponent.getSpawnWrapper().getSpawnIndex();
                           if (existingBeaconIndex == possibleBeacon.getSpawnIndex()) {
                              continue label134;
                           }
                        }
                     }

                     for (int j = 0; j < pendingSpawns.size(); j++) {
                        LegacySpawnBeaconEntity pending = pendingSpawns.get(j);
                        Ref<EntityStore> pendingReference = pending.getReference();
                        TransformComponent pendingTransformComponent = store.getComponent(pendingReference, TransformComponent.getComponentType());

                        assert pendingTransformComponent != null;

                        Vector3d pendingPosition = pendingTransformComponent.getPosition();
                        double pendingY = pendingPosition.getY();
                        if (!(pendingY > maxY) && !(pendingY < minY)) {
                           double xDiff = position.x - pendingPosition.x;
                           double zDiff = position.z - pendingPosition.z;
                           double distSquared = xDiff * xDiff + zDiff * zDiff;
                           if (!(distSquared > largestDistanceSquared)) {
                              int existingBeaconIndex = pending.getSpawnWrapper().getSpawnIndex();
                              if (existingBeaconIndex == possibleBeacon.getSpawnIndex()) {
                                 continue label134;
                              }
                           }
                        }
                     }

                     if (spawnLightLevelMatches(world, xPos, yPos, zPos, sunlightFactor, possibleBeacon, averageLightValues)) {
                        Pair<Ref<EntityStore>, LegacySpawnBeaconEntity> beaconEntityPair = LegacySpawnBeaconEntity.create(
                           possibleBeacon, transformComponent.getPosition(), transformComponent.getRotation(), store
                        );
                        Ref<EntityStore> beaconRef = (Ref<EntityStore>)beaconEntityPair.first();
                        if (beaconRef != null && beaconRef.isValid()) {
                           store.ensureComponent(beaconRef, this.localSpawnBeaconComponentType);
                           SpawningPlugin.get()
                              .getLogger()
                              .at(Level.FINE)
                              .log(
                                 "Placed spawn beacon of type %s at position %s for player %s",
                                 possibleBeacon.getSpawn().getId(),
                                 position,
                                 playerRefComponent.getUsername()
                              );
                           pendingSpawns.add((LegacySpawnBeaconEntity)beaconEntityPair.second());
                        }
                     }
                  }
               }

               existingBeacons.clear();
               averageLightValues.clear();
               spawnControllerComponent.setTimeToNextRunSeconds(5.0);
            } else {
               spawnControllerComponent.setTimeToNextRunSeconds(5.0);
            }
         }

         controllers.clear();
         pendingSpawns.clear();
      }
   }

   private static boolean spawnLightLevelMatches(
      @Nonnull World world, int x, int y, int z, double sunlightFactor, @Nonnull BeaconSpawnWrapper wrapper, @Nonnull Object2ByteMap<LightType> averageValues
   ) {
      LightRangePredicate lightRangePredicate = wrapper.getLightRangePredicate();
      if (lightRangePredicate.isTestLightValue()) {
         byte lightValue = getCachedAverageLightValue(
            LightType.Light,
            world,
            x,
            y,
            z,
            sunlightFactor,
            (_x, _y, _z, _chunk, _sunlightFactor) -> LightRangePredicate.calculateLightValue(_chunk, _x, _y, _z, _sunlightFactor),
            averageValues
         );
         if (!lightRangePredicate.testLight(lightValue)) {
            return false;
         }
      }

      if (lightRangePredicate.isTestSkyLightValue()) {
         byte lightValue = getCachedAverageLightValue(
            LightType.SkyLight, world, x, y, z, sunlightFactor, (_x, _y, _z, _chunk, _sunlightFactor) -> _chunk.getSkyLight(_x, _y, _z), averageValues
         );
         if (!lightRangePredicate.testSkyLight(lightValue)) {
            return false;
         }
      }

      if (lightRangePredicate.isTestSunlightValue()) {
         byte lightValue = getCachedAverageLightValue(
            LightType.Sunlight,
            world,
            x,
            y,
            z,
            sunlightFactor,
            (_x, _y, _z, _chunk, _sunlightFactor) -> (byte)(_chunk.getSkyLight(_x, _y, _z) * _sunlightFactor),
            averageValues
         );
         if (!lightRangePredicate.testSunlight(lightValue)) {
            return false;
         }
      }

      if (lightRangePredicate.isTestRedLightValue()) {
         byte lightValue = getCachedAverageLightValue(
            LightType.RedLight, world, x, y, z, sunlightFactor, (_x, _y, _z, _chunk, _sunlightFactor) -> _chunk.getRedBlockLight(_x, _y, _z), averageValues
         );
         if (!lightRangePredicate.testRedLight(lightValue)) {
            return false;
         }
      }

      if (lightRangePredicate.isTestGreenLightValue()) {
         byte lightValue = getCachedAverageLightValue(
            LightType.GreenLight, world, x, y, z, sunlightFactor, (_x, _y, _z, _chunk, _sunlightFactor) -> _chunk.getGreenBlockLight(_x, _y, _z), averageValues
         );
         if (!lightRangePredicate.testGreenLight(lightValue)) {
            return false;
         }
      }

      if (lightRangePredicate.isTestBlueLightValue()) {
         byte lightValue = getCachedAverageLightValue(
            LightType.BlueLight, world, x, y, z, sunlightFactor, (_x, _y, _z, _chunk, _sunlightFactor) -> _chunk.getBlueBlockLight(_x, _y, _z), averageValues
         );
         return lightRangePredicate.testBlueLight(lightValue);
      } else {
         return true;
      }
   }

   private static byte getCachedAverageLightValue(
      LightType lightType,
      @Nonnull World world,
      int x,
      int y,
      int z,
      double sunlightFactor,
      @Nonnull TriIntObjectDoubleToByteFunction<BlockChunk> valueCalculator,
      @Nonnull Object2ByteMap<LightType> averageValues
   ) {
      byte cachedValue = averageValues.getByte(lightType);
      if (cachedValue < 0) {
         int counted = 0;
         int total = 0;

         for (int xOffset = x - 4; xOffset < x + 4; xOffset++) {
            for (int zOffset = z - 4; zOffset < z + 4; zOffset++) {
               WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(xOffset, zOffset));
               if (chunk != null) {
                  BlockChunk blockChunk = chunk.getBlockChunk();

                  for (int yOffset = y; yOffset < y + 4; yOffset++) {
                     int blockId = chunk.getBlock(xOffset, yOffset, zOffset);
                     if (blockId == 0 || BlockType.getAssetMap().getAsset(blockId).getMaterial() != BlockMaterial.Solid) {
                        counted++;
                        total += valueCalculator.apply(xOffset, yOffset, zOffset, blockChunk, sunlightFactor);
                     }
                  }
               }
            }
         }

         cachedValue = counted > 0 ? (byte)((float)total / counted) : 0;
         averageValues.put(lightType, cachedValue);
      }

      return cachedValue;
   }
}
