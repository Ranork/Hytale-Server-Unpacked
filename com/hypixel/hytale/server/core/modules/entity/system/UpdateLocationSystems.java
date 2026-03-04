package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateLocationSystems {
   @Nonnull
   private static final Message MESSAGE_GENERAL_PLAYER_IN_INVALID_CHUNK = Message.translation("server.general.playerInInvalidChunk");
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   private static void updateLocation(
      @Nonnull Ref<EntityStore> ref, @Nonnull TransformComponent transformComponent, @Nullable World world, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      if (world != null) {
         Vector3d position = transformComponent.getPosition();
         if (position.getY() < -32.0 && !commandBuffer.getArchetype(ref).contains(Player.getComponentType())) {
            LOGGER.at(Level.WARNING).log("Unable to move entity below the world! -32 < " + position);
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         } else {
            ChunkStore chunkStore = world.getChunkStore();
            Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
            int chunkX = MathUtil.floor(position.getX()) >> 5;
            int chunkZ = MathUtil.floor(position.getZ()) >> 5;
            Ref<ChunkStore> oldChunkRef = transformComponent.getChunkRef();
            boolean hasOldChunk = false;
            int oldChunkX = 0;
            int oldChunkZ = 0;
            if (oldChunkRef != null && oldChunkRef.isValid()) {
               WorldChunk oldWorldChunkComponent = chunkComponentStore.getComponent(oldChunkRef, WorldChunk.getComponentType());
               if (oldWorldChunkComponent != null) {
                  hasOldChunk = true;
                  oldChunkX = oldWorldChunkComponent.getX();
                  oldChunkZ = oldWorldChunkComponent.getZ();
               }
            }

            if (!hasOldChunk || oldChunkX != chunkX || oldChunkZ != chunkZ) {
               long newChunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
               Ref<ChunkStore> newChunkRef = chunkStore.getChunkReference(newChunkIndex);
               if (newChunkRef != null && newChunkRef.isValid()) {
                  WorldChunk newWorldChunkComponent = chunkComponentStore.getComponent(newChunkRef, WorldChunk.getComponentType());
                  updateChunk(ref, transformComponent, oldChunkRef, newChunkRef, newWorldChunkComponent, chunkComponentStore, commandBuffer);
               } else {
                  LOGGER.at(Level.WARNING)
                     .log("Entity has moved into a chunk that isn't currently loaded! " + chunkX + ", " + chunkZ + ", " + transformComponent);
                  CompletableFutureUtil._catch(chunkStore.getChunkReferenceAsync(newChunkIndex).thenAcceptAsync(asyncChunkRef -> {
                     if (asyncChunkRef != null && asyncChunkRef.isValid()) {
                        WorldChunk asyncWorldChunk = chunkComponentStore.getComponent((Ref<ChunkStore>)asyncChunkRef, WorldChunk.getComponentType());
                        updateChunkAsync(ref, (Ref<ChunkStore>)asyncChunkRef, asyncWorldChunk, chunkComponentStore);
                     } else {
                        updateChunkAsync(ref, null, null, chunkComponentStore);
                     }
                  }, world));
               }
            }
         }
      }
   }

   private static void updateChunkAsync(
      @Nonnull Ref<EntityStore> ref, @Nullable Ref<ChunkStore> newChunkRef, @Nullable WorldChunk newWorldChunk, @Nonnull Store<ChunkStore> chunkComponentStore
   ) {
      if (ref.isValid()) {
         Store<EntityStore> entityStore = ref.getStore();
         TransformComponent transformComponent = entityStore.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Ref<ChunkStore> oldChunkRef = transformComponent.getChunkRef();
         updateChunk(ref, transformComponent, oldChunkRef, newChunkRef, newWorldChunk, chunkComponentStore, entityStore);
      }
   }

   private static void updateChunk(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull TransformComponent transformComponent,
      @Nullable Ref<ChunkStore> oldChunkRef,
      @Nullable Ref<ChunkStore> newChunkRef,
      @Nullable WorldChunk newWorldChunkComponent,
      @Nonnull ComponentAccessor<ChunkStore> chunkComponentStore,
      @Nonnull ComponentAccessor<EntityStore> entityComponentAccessor
   ) {
      boolean isPlayer = entityComponentAccessor.getArchetype(ref).contains(Player.getComponentType());
      if (newWorldChunkComponent == null) {
         handleInvalidChunk(ref, transformComponent, isPlayer, entityComponentAccessor);
      } else if (!newWorldChunkComponent.not(ChunkFlag.INIT)) {
         assert newChunkRef != null;

         if (!isPlayer) {
            updateEntityInChunk(ref, oldChunkRef, newChunkRef, newWorldChunkComponent, chunkComponentStore, entityComponentAccessor);
         }

         transformComponent.setChunkLocation(newChunkRef, newWorldChunkComponent);
      }
   }

   private static void handleInvalidChunk(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull TransformComponent transformComponent,
      boolean isPlayer,
      @Nonnull ComponentAccessor<EntityStore> entityComponentAccessor
   ) {
      if (!isPlayer) {
         LOGGER.at(Level.SEVERE).log("Entity is in a chunk that can't be loaded! Removing! %s", transformComponent);
         entityComponentAccessor.removeEntity(ref, EntityStore.REGISTRY.newHolder(), RemoveReason.REMOVE);
      } else {
         LOGGER.at(Level.SEVERE).log("Player is in a chunk that can't be loaded! Moving (-%d,0,0)! %s", 32, transformComponent);
         Vector3d position = transformComponent.getPosition();
         Vector3d targetPosition = position.clone().subtract(32.0, 0.0, 0.0);
         Vector3f bodyRotation = transformComponent.getRotation();
         Teleport teleportComponent = Teleport.createForPlayer(targetPosition, bodyRotation);
         entityComponentAccessor.addComponent(ref, Teleport.getComponentType(), teleportComponent);
         PlayerRef playerRefComponent = entityComponentAccessor.getComponent(ref, PlayerRef.getComponentType());
         if (playerRefComponent != null) {
            playerRefComponent.sendMessage(MESSAGE_GENERAL_PLAYER_IN_INVALID_CHUNK);
         }
      }
   }

   private static void updateEntityInChunk(
      @Nonnull Ref<EntityStore> ref,
      @Nullable Ref<ChunkStore> oldChunkRef,
      @Nonnull Ref<ChunkStore> newChunkRef,
      @Nonnull WorldChunk newWorldChunk,
      @Nonnull ComponentAccessor<ChunkStore> chunkComponentStore,
      @Nonnull ComponentAccessor<EntityStore> entityComponentAccessor
   ) {
      if (oldChunkRef != null && oldChunkRef.isValid()) {
         EntityChunk oldEntityChunkComponent = chunkComponentStore.getComponent(oldChunkRef, EntityChunk.getComponentType());

         assert oldEntityChunkComponent != null;

         oldEntityChunkComponent.removeEntityReference(ref);
      }

      EntityChunk newEntityChunkComponent = chunkComponentStore.getComponent(newChunkRef, EntityChunk.getComponentType());

      assert newEntityChunkComponent != null;

      if (newWorldChunk.not(ChunkFlag.TICKING)) {
         Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
         entityComponentAccessor.removeEntity(ref, holder, RemoveReason.UNLOAD);
         newEntityChunkComponent.addEntityHolder(holder);
      } else {
         newEntityChunkComponent.addEntityReference(ref);
      }
   }

   public static class SpawnSystem extends RefSystem<EntityStore> {
      @Override
      public Query<EntityStore> getQuery() {
         return TransformComponent.getComponentType();
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
         if (chunkRef == null || !chunkRef.isValid()) {
            UpdateLocationSystems.updateLocation(ref, transformComponent, store.getExternalData().getWorld(), commandBuffer);
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class TickingSystem extends EntityTickingSystem<EntityStore> {
      @Override
      public Query<EntityStore> getQuery() {
         return TransformComponent.getComponentType();
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
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

         assert transformComponent != null;

         UpdateLocationSystems.updateLocation(ref, transformComponent, world, commandBuffer);
      }
   }
}
