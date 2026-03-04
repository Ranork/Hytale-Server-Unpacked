package com.hypixel.hytale.server.worldgen;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import com.hypixel.hytale.server.worldgen.zone.ZoneDiscoveryConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BiomeDataSystem extends DelayedEntitySystem<EntityStore> {
   public BiomeDataSystem() {
      super(1.0F);
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      World world = commandBuffer.getExternalData().getWorld();
      IWorldGen worldGen = world.getChunkStore().getGenerator();
      Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());

      assert playerComponent != null;

      Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
      WorldMapTracker worldMapTracker = playerComponent.getWorldMapTracker();
      if (worldGen instanceof ChunkGenerator generator) {
         TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         int seed = (int)world.getWorldConfig().getSeed();
         int x = (int)position.getX();
         int z = (int)position.getZ();
         ZoneBiomeResult result = generator.getZoneBiomeResultAt(seed, x, z);
         Biome biome = result.getBiome();
         Zone zone = result.getZoneResult().getZone();
         ZoneDiscoveryConfig discoveryConfig = zone.discoveryConfig();
         WorldMapTracker.ZoneDiscoveryInfo zoneDiscoveryInfo = new WorldMapTracker.ZoneDiscoveryInfo(
            discoveryConfig.zone(),
            zone.name(),
            discoveryConfig.display(),
            discoveryConfig.soundEventId(),
            discoveryConfig.icon(),
            discoveryConfig.major(),
            discoveryConfig.duration(),
            discoveryConfig.fadeInDuration(),
            discoveryConfig.fadeOutDuration()
         );
         worldMapTracker.updateCurrentZoneAndBiome(ref, zoneDiscoveryInfo, biome.getName(), commandBuffer);
      } else {
         worldMapTracker.updateCurrentZoneAndBiome(ref, null, null, commandBuffer);
      }
   }

   @Nullable
   @Override
   public Query<EntityStore> getQuery() {
      return Archetype.of(Player.getComponentType(), TransformComponent.getComponentType());
   }
}
