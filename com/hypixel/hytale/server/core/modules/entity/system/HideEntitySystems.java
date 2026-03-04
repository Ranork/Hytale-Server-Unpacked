package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HideEntitySystems {
   public static class AdventurePlayerSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType = EntityTrackerSystems.EntityViewer.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, HiddenFromAdventurePlayers> hiddenFromAdventurePlayersComponentType = HiddenFromAdventurePlayers.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, PlayerSettings> playerSettingsComponentType = EntityModule.get().getPlayerSettingsComponentType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(this.entityViewerComponentType, this.playerComponentType, this.playerSettingsComponentType);
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies = Collections.singleton(
         new SystemDependency<>(Order.AFTER, EntityTrackerSystems.CollectVisible.class)
      );

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.FIND_VISIBLE_ENTITIES_GROUP;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityTrackerSystems.EntityViewer entityViewerComponent = archetypeChunk.getComponent(index, this.entityViewerComponentType);

         assert entityViewerComponent != null;

         PlayerSettings playerSettingsComponent = archetypeChunk.getComponent(index, this.playerSettingsComponentType);

         assert playerSettingsComponent != null;

         Player playerComponent = archetypeChunk.getComponent(index, this.playerComponentType);

         assert playerComponent != null;

         if (playerComponent.getGameMode() == GameMode.Adventure || !playerSettingsComponent.showEntityMarkers()) {
            Iterator<Ref<EntityStore>> iterator = entityViewerComponent.visible.iterator();

            while (iterator.hasNext()) {
               Ref<EntityStore> ref = iterator.next();
               if (commandBuffer.getArchetype(ref).contains(this.hiddenFromAdventurePlayersComponentType)) {
                  entityViewerComponent.hiddenCount++;
                  iterator.remove();
               }
            }
         }
      }
   }
}
