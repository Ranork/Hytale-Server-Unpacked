package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PrefabDirtySystems {
   private PrefabDirtySystems() {
   }

   private static void markDirtyAtPosition(@Nonnull Vector3i position) {
      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();

      for (Entry<UUID, PrefabEditSession> entry : prefabEditSessionManager.getActiveEditSessions().entrySet()) {
         PrefabEditSession editSession = entry.getValue();
         editSession.markPrefabsDirtyAtPosition(position);
      }
   }

   public static class BlockBreakDirtySystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
      public BlockBreakDirtySystem() {
         super(BreakBlockEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull BreakBlockEvent event
      ) {
         PrefabDirtySystems.markDirtyAtPosition(event.getTargetBlock());
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }
   }

   public static class BlockPlaceDirtySystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
      public BlockPlaceDirtySystem() {
         super(PlaceBlockEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull PlaceBlockEvent event
      ) {
         PrefabDirtySystems.markDirtyAtPosition(event.getTargetBlock());
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }
   }
}
