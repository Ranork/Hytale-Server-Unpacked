package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerChunkTrackerSystems {
   public static class AddSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private static final ComponentType<EntityStore, ChunkTracker> CHUNK_TRACKER_COMPONENT_TYPE = ChunkTracker.getComponentType();

      @Override
      public Query<EntityStore> getQuery() {
         return CHUNK_TRACKER_COMPONENT_TYPE;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         ChunkTracker chunkTrackerComponent = holder.getComponent(CHUNK_TRACKER_COMPONENT_TYPE);

         assert chunkTrackerComponent != null;

         chunkTrackerComponent.setReadyForChunks(true);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class UpdateSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private static final ComponentType<EntityStore, ChunkTracker> CHUNK_TRACKER_COMPONENT_TYPE = ChunkTracker.getComponentType();
      @Nonnull
      private static final ComponentType<EntityStore, Player> PLAYER_COMPONENT_TYPE = Player.getComponentType();
      @Nonnull
      private static final ComponentType<EntityStore, PlayerRef> PLAYER_REF_COMPONENT_TYPE = PlayerRef.getComponentType();
      @Nonnull
      private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();

      @Override
      public Query<EntityStore> getQuery() {
         return Query.and(CHUNK_TRACKER_COMPONENT_TYPE, PLAYER_COMPONENT_TYPE, PLAYER_REF_COMPONENT_TYPE, TRANSFORM_COMPONENT_TYPE);
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         ChunkTracker chunkTrackerComponent = archetypeChunk.getComponent(index, CHUNK_TRACKER_COMPONENT_TYPE);

         assert chunkTrackerComponent != null;

         Player playerComponent = archetypeChunk.getComponent(index, PLAYER_COMPONENT_TYPE);

         assert playerComponent != null;

         PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PLAYER_REF_COMPONENT_TYPE);

         assert playerRefComponent != null;

         TransformComponent transformComponent = archetypeChunk.getComponent(index, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         chunkTrackerComponent.tick(playerComponent, playerRefComponent, transformComponent, dt, commandBuffer);
      }
   }
}
