package com.hypixel.hytale.builtin.audio.systems;

import com.hypixel.hytale.builtin.audio.components.ForcedMusicTracker;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.packets.world.UpdateForcedMusic;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ForcedMusicSystems {
   public static class PlayerAdded extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
      @Nonnull
      private final ComponentType<EntityStore, ForcedMusicTracker> forcedMusicTrackerComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public PlayerAdded(
         @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType,
         @Nonnull ComponentType<EntityStore, ForcedMusicTracker> forcedMusicTrackerComponentType
      ) {
         this.playerRefComponentType = playerRefComponentType;
         this.forcedMusicTrackerComponentType = forcedMusicTrackerComponentType;
         this.query = Query.and(playerRefComponentType);
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.forcedMusicTrackerComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         ForcedMusicTracker forcedMusicTracker = holder.getComponent(this.forcedMusicTrackerComponentType);

         assert forcedMusicTracker != null;

         PlayerRef playerRefComponent = holder.getComponent(this.playerRefComponentType);

         assert playerRefComponent != null;

         UpdateForcedMusic pooledPacket = forcedMusicTracker.getMusicPacket();
         pooledPacket.containerIndex = 0;
         playerRefComponent.getPacketHandler().write(pooledPacket);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class Tick extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
      @Nonnull
      private final ComponentType<EntityStore, ForcedMusicTracker> forcedMusicTrackerComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public Tick(
         @Nonnull ComponentType<EntityStore, Player> playerComponentType,
         @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType,
         @Nonnull ComponentType<EntityStore, ForcedMusicTracker> forcedMusicTrackerComponentType
      ) {
         this.playerRefComponentType = playerRefComponentType;
         this.forcedMusicTrackerComponentType = forcedMusicTrackerComponentType;
         this.query = Archetype.of(playerComponentType, playerRefComponentType, forcedMusicTrackerComponentType);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         ForcedMusicTracker forcedMusicTracker = archetypeChunk.getComponent(index, this.forcedMusicTrackerComponentType);

         assert forcedMusicTracker != null;

         PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.playerRefComponentType);

         assert playerRefComponent != null;

         int have = forcedMusicTracker.getLastSentContainerIndex();
         int desired = forcedMusicTracker.getCurrentContainerIndex();
         if (have != desired) {
            forcedMusicTracker.setLastSentContainerIndex(desired);
            UpdateForcedMusic pooledPacket = forcedMusicTracker.getMusicPacket();
            pooledPacket.containerIndex = desired;
            playerRefComponent.getPacketHandler().write(pooledPacket);
         }
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }
}
