package com.hypixel.hytale.server.spawning.blockstates;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.FromWorldGen;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class SpawnMarkerBlockStateSystems {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   private static void createMarker(
      @Nonnull Ref<ChunkStore> ref,
      @Nonnull SpawnMarkerBlock state,
      @Nonnull BlockModule.BlockStateInfo info,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      if (info.getChunkRef().isValid()) {
         BlockChunk blockChunk = commandBuffer.getComponent(info.getChunkRef(), BlockChunk.getComponentType());
         if (blockChunk != null) {
            int bx = ChunkUtil.xFromBlockInColumn(info.getIndex());
            int by = ChunkUtil.yFromBlockInColumn(info.getIndex());
            int bz = ChunkUtil.zFromBlockInColumn(info.getIndex());
            BlockType blockType = BlockType.getAssetMap().getAsset(blockChunk.getBlock(bx, by, bz));
            if (blockType != null && blockType.getBlockEntity() != null) {
               SpawnMarkerBlock configType = blockType.getBlockEntity().getComponent(SpawnMarkerBlock.getComponentType());
               if (configType != null) {
                  SpawnMarkerBlock.Data data = configType.getConfig();
                  if (data != null) {
                     SpawnMarker marker = SpawnMarker.getAssetMap().getAsset(data.getSpawnMarker());
                     if (marker == null) {
                        LOGGER.at(Level.SEVERE).log(String.format("Marker %s does not exist!", data.getSpawnMarker()));
                        commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
                     } else {
                        Vector3i pos = new Vector3i(
                           ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), bx), by, ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), bz)
                        );
                        Vector3i blockPos = pos.clone();
                        Vector3i offset = data.getMarkerOffset();
                        if (offset != null) {
                           pos.add(offset);
                        }

                        SpawnMarkerEntity spawnMarker = new SpawnMarkerEntity();
                        spawnMarker.setSpawnMarker(marker);
                        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
                        holder.addComponent(SpawnMarkerEntity.getComponentType(), spawnMarker);
                        holder.addComponent(SpawnMarkerBlockReference.getComponentType(), new SpawnMarkerBlockReference(blockPos));
                        Vector3d markerPos = pos.toVector3d();
                        markerPos.add(0.5, 0.0, 0.5);
                        holder.addComponent(Nameplate.getComponentType(), new Nameplate(marker.getId()));
                        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(markerPos, Vector3f.ZERO));
                        UUIDComponent uuidComponent = holder.ensureAndGetComponent(UUIDComponent.getComponentType());
                        Model model = SpawnMarkerEntity.getModel(marker);
                        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
                        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
                        holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
                        Ref<EntityStore> markerRef = store.addEntity(holder, AddReason.SPAWN);
                        PersistentRef persistentRef = new PersistentRef();
                        persistentRef.setEntity(markerRef, uuidComponent.getUuid());
                        state.setSpawnMarkerReference(persistentRef);
                     }
                  }
               }
            }
         }
      }
   }

   public static class AddOrRemove extends RefSystem<ChunkStore> {
      private final ComponentType<ChunkStore, SpawnMarkerBlock> componentType;

      public AddOrRemove(ComponentType<ChunkStore, SpawnMarkerBlock> componentType) {
         this.componentType = componentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         if (reason == RemoveReason.REMOVE) {
            SpawnMarkerBlock state = store.getComponent(ref, this.componentType);
            PersistentRef markerReference = state.getSpawnMarkerReference();
            if (markerReference == null) {
               return;
            }

            World world = store.getExternalData().getWorld();
            world.execute(() -> {
               Store<EntityStore> entityStore = world.getEntityStore().getStore();
               Ref<EntityStore> marker = markerReference.getEntity(entityStore);
               if (marker != null) {
                  entityStore.removeEntity(marker, RemoveReason.REMOVE);
               }
            });
         }
      }
   }

   public static class SpawnMarkerAddedFromExternal extends RefSystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query;

      public SpawnMarkerAddedFromExternal(ComponentType<EntityStore, SpawnMarkerBlockReference> componentType) {
         this.query = Query.and(componentType, Query.or(FromWorldGen.getComponentType(), FromPrefab.getComponentType()));
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class SpawnMarkerTickHeartbeat extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, SpawnMarkerBlockReference> componentType;

      public SpawnMarkerTickHeartbeat(ComponentType<EntityStore, SpawnMarkerBlockReference> componentType) {
         this.componentType = componentType;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         SpawnMarkerBlockReference marker = archetypeChunk.getComponent(index, this.componentType);
         Vector3i pos = marker.getBlockPosition();
         WorldChunk chunk = store.getExternalData().getWorld().getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
         if (chunk != null) {
            Ref<ChunkStore> blockEntityRef = chunk.getBlockComponentEntity(pos.x, pos.y, pos.z);
            if (blockEntityRef != null && blockEntityRef.getStore().getComponent(blockEntityRef, SpawnMarkerBlock.getComponentType()) == null) {
               Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
               commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
               SpawnMarkerBlockStateSystems.LOGGER.at(Level.SEVERE).log("Removing block spawn marker due to blockstate mismatch: %s", ref);
            } else {
               marker.refreshOriginLostTimeout();
            }
         } else if (marker.tickOriginLostTimeout(dt)) {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
            SpawnMarkerBlockStateSystems.LOGGER.at(Level.SEVERE).log("Removing block spawn marker due to origin chunk being unloaded: %s", ref);
         }
      }
   }

   public static class TickHeartbeat extends EntityTickingSystem<ChunkStore> {
      private final ComponentType<ChunkStore, SpawnMarkerBlock> componentType;
      private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType = BlockModule.BlockStateInfo.getComponentType();
      private final Query<ChunkStore> query;

      public TickHeartbeat(ComponentType<ChunkStore, SpawnMarkerBlock> componentType) {
         this.componentType = componentType;
         this.query = Query.and(componentType, this.blockStateInfoComponentType);
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         SpawnMarkerBlock state = archetypeChunk.getComponent(index, this.componentType);

         assert state != null;

         BlockModule.BlockStateInfo info = archetypeChunk.getComponent(index, this.blockStateInfoComponentType);

         assert info != null;

         if (state.getSpawnMarkerReference() == null) {
            Ref<ChunkStore> ref = archetypeChunk.getReferenceTo(index);
            SpawnMarkerBlockStateSystems.createMarker(ref, state, info, store.getExternalData().getWorld().getEntityStore().getStore(), commandBuffer);
         }

         if (state.getSpawnMarkerReference().getEntity(store.getExternalData().getWorld().getEntityStore().getStore()) != null) {
            state.refreshMarkerLostTimeout();
         } else if (state.tickMarkerLostTimeout(dt)) {
            Ref<ChunkStore> ref = archetypeChunk.getReferenceTo(index);
            SpawnMarkerBlockStateSystems.LOGGER.at(Level.SEVERE).log("Creating new spawn marker due to desync with entity: %s", ref);
            SpawnMarkerBlockStateSystems.createMarker(ref, state, info, store.getExternalData().getWorld().getEntityStore().getStore(), commandBuffer);
         }
      }
   }
}
