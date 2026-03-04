package com.hypixel.hytale.server.core.modules.blockhealth;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.world.UpdateBlockDamage;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldConfig;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.modules.time.TimeModule;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHealthModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(BlockHealthModule.class)
      .depends(LegacyModule.class)
      .depends(TimeModule.class)
      .build();
   private static final long SECONDS_UNTIL_REGENERATION = 5L;
   private static final float HEALING_PER_SECOND = 0.1F;
   private static BlockHealthModule instance;
   private ComponentType<ChunkStore, BlockHealthChunk> blockHealthChunkComponentType;

   public BlockHealthModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   public static BlockHealthModule get() {
      return instance;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = this.getChunkStoreRegistry();
      this.blockHealthChunkComponentType = chunkStoreRegistry.registerComponent(BlockHealthChunk.class, "BlockHealthChunk", BlockHealthChunk.CODEC);
      this.getEntityStoreRegistry().registerSystem(new BlockHealthModule.PlaceBlockEventSystem());
      chunkStoreRegistry.registerSystem(new BlockHealthModule.EnsureBlockHealthSystem(this.blockHealthChunkComponentType));
      chunkStoreRegistry.registerSystem(new BlockHealthModule.BlockHealthSystem(this.blockHealthChunkComponentType));
      chunkStoreRegistry.registerSystem(new BlockHealthModule.BlockHealthPacketSystem(this.blockHealthChunkComponentType));
   }

   public ComponentType<ChunkStore, BlockHealthChunk> getBlockHealthChunkComponentType() {
      return this.blockHealthChunkComponentType;
   }

   private static class BlockHealthPacketSystem extends ChunkStore.LoadPacketDataQuerySystem {
      @Nonnull
      private final ComponentType<ChunkStore, BlockHealthChunk> blockHealthCunkComponentType;
      @Nonnull
      private final Archetype<ChunkStore> archetype;

      public BlockHealthPacketSystem(@Nonnull ComponentType<ChunkStore, BlockHealthChunk> blockHealthChunkComponentType) {
         this.blockHealthCunkComponentType = blockHealthChunkComponentType;
         this.archetype = Archetype.of(blockHealthChunkComponentType, WorldChunk.getComponentType());
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.archetype;
      }

      @Override
      public boolean isParallel() {
         return true;
      }

      public void fetch(
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         Store<ChunkStore> store,
         CommandBuffer<ChunkStore> commandBuffer,
         PlayerRef player,
         @Nonnull List<ToClientPacket> results
      ) {
         BlockHealthChunk blockHealthChunkComponent = archetypeChunk.getComponent(index, this.blockHealthCunkComponentType);

         assert blockHealthChunkComponent != null;

         blockHealthChunkComponent.createBlockDamagePackets(results);
      }
   }

   private static class BlockHealthSystem extends EntityTickingSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, BlockHealthChunk> blockHealthComponentChunkType;
      @Nonnull
      private final ResourceType<EntityStore, TimeResource> timeResourceType;
      private final Archetype<ChunkStore> archetype;

      public BlockHealthSystem(@Nonnull ComponentType<ChunkStore, BlockHealthChunk> blockHealthComponentChunkType) {
         this.blockHealthComponentChunkType = blockHealthComponentChunkType;
         this.timeResourceType = TimeResource.getResourceType();
         this.archetype = Archetype.of(blockHealthComponentChunkType, WorldChunk.getComponentType());
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.archetype;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         BlockHealthChunk blockHealthChunkComponent = archetypeChunk.getComponent(index, this.blockHealthComponentChunkType);

         assert blockHealthChunkComponent != null;

         World world = store.getExternalData().getWorld();
         Store<EntityStore> entityStore = world.getEntityStore().getStore();
         TimeResource uptime = world.getEntityStore().getStore().getResource(this.timeResourceType);
         Instant currentGameTime = uptime.getNow();
         Instant lastRepairGameTime = blockHealthChunkComponent.getLastRepairGameTime();
         blockHealthChunkComponent.setLastRepairGameTime(currentGameTime);
         if (lastRepairGameTime != null) {
            Map<Vector3i, FragileBlock> blockFragilityMap = blockHealthChunkComponent.getBlockFragilityMap();
            if (!blockFragilityMap.isEmpty()) {
               float deltaSeconds = (float)(currentGameTime.toEpochMilli() - lastRepairGameTime.toEpochMilli()) / 1000.0F;
               Iterator<Entry<Vector3i, FragileBlock>> iterator = blockFragilityMap.entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry<Vector3i, FragileBlock> entry = iterator.next();
                  FragileBlock fragileBlock = entry.getValue();
                  float newDuration = fragileBlock.getDurationSeconds() - deltaSeconds;
                  if (newDuration <= 0.0F) {
                     iterator.remove();
                  } else {
                     fragileBlock.setDurationSeconds(newDuration);
                  }
               }
            }

            Map<Vector3i, BlockHealth> blockHealthMap = blockHealthChunkComponent.getBlockHealthMap();
            if (!blockHealthMap.isEmpty()) {
               WorldChunk chunk = archetypeChunk.getComponent(index, WorldChunk.getComponentType());

               assert chunk != null;

               Collection<PlayerRef> allPlayers = world.getPlayerRefs();
               ObjectArrayList<PlayerRef> visibleTo = new ObjectArrayList(allPlayers.size());

               for (PlayerRef playerRef : allPlayers) {
                  Ref<EntityStore> playerReference = playerRef.getReference();
                  if (playerReference != null && playerReference.isValid()) {
                     ChunkTracker chunkTrackerComponent = entityStore.getComponent(playerReference, ChunkTracker.getComponentType());

                     assert chunkTrackerComponent != null;

                     if (chunkTrackerComponent.isLoaded(chunk.getIndex())) {
                        visibleTo.add(playerRef);
                     }
                  }
               }

               float deltaSeconds = (float)(currentGameTime.toEpochMilli() - lastRepairGameTime.toEpochMilli()) / 1000.0F;
               Iterator<Entry<Vector3i, BlockHealth>> iterator = blockHealthMap.entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry<Vector3i, BlockHealth> entry = iterator.next();
                  Vector3i position = entry.getKey();
                  BlockHealth blockHealth = entry.getValue();
                  Instant startRegenerating = blockHealth.getLastDamageGameTime().plusSeconds(5L);
                  if (!currentGameTime.isBefore(startRegenerating)) {
                     float healthDelta = 0.1F * deltaSeconds;
                     float health = blockHealth.getHealth() + healthDelta;
                     if (health < 1.0F) {
                        blockHealth.setHealth(health);
                     } else {
                        iterator.remove();
                        health = BlockHealth.NO_DAMAGE_INSTANCE.getHealth();
                        healthDelta = health - blockHealth.getHealth();
                     }

                     UpdateBlockDamage packet = new UpdateBlockDamage(new BlockPosition(position.getX(), position.getY(), position.getZ()), health, healthDelta);

                     for (int i = 0; i < visibleTo.size(); i++) {
                        ((PlayerRef)visibleTo.get(i)).getPacketHandler().writeNoCache(packet);
                     }
                  }
               }
            }
         }
      }
   }

   private static class EnsureBlockHealthSystem extends HolderSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, BlockHealthChunk> blockHealthChunkComponentType;

      public EnsureBlockHealthSystem(@Nonnull ComponentType<ChunkStore, BlockHealthChunk> blockHealthChunkComponentType) {
         this.blockHealthChunkComponentType = blockHealthChunkComponentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return WorldChunk.getComponentType();
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
         holder.ensureComponent(this.blockHealthChunkComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
      }
   }

   public static class PlaceBlockEventSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
      @Nonnull
      private static final ComponentType<ChunkStore, BlockHealthChunk> BLOCK_HEALTH_CHUNK_COMPONENT_TYPE = BlockHealthModule.get()
         .getBlockHealthChunkComponentType();

      public PlaceBlockEventSystem() {
         super(PlaceBlockEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull PlaceBlockEvent event
      ) {
         World world = commandBuffer.getExternalData().getWorld();
         Vector3i blockLocation = event.getTargetBlock();
         ChunkStore chunkComponentStore = world.getChunkStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(blockLocation.x, blockLocation.z);
         Ref<ChunkStore> chunkReference = chunkComponentStore.getChunkReference(chunkIndex);
         if (chunkReference != null) {
            BlockHealthChunk blockHealthComponent = chunkComponentStore.getStore().getComponent(chunkReference, BLOCK_HEALTH_CHUNK_COMPONENT_TYPE);

            assert blockHealthComponent != null;

            WorldConfig worldGameplayConfig = world.getGameplayConfig().getWorldConfig();
            float blockPlacementFragilityTimer = worldGameplayConfig.getBlockPlacementFragilityTimer();
            blockHealthComponent.makeBlockFragile(blockLocation, blockPlacementFragilityTimer);
         }
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }
   }
}
