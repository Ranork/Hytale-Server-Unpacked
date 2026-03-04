package com.hypixel.hytale.builtin.ambience.systems;

import com.hypixel.hytale.builtin.ambience.components.AmbienceTracker;
import com.hypixel.hytale.builtin.ambience.resources.AmbienceResource;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.packets.world.UpdateEnvironmentMusic;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ForcedMusicSystems {
   public static class PlayerAdded extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
      @Nonnull
      private final ComponentType<EntityStore, AmbienceTracker> ambienceTrackerComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public PlayerAdded(
         @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType,
         @Nonnull ComponentType<EntityStore, AmbienceTracker> ambienceTrackerComponentType
      ) {
         this.playerRefComponentType = playerRefComponentType;
         this.ambienceTrackerComponentType = ambienceTrackerComponentType;
         this.query = Query.and(playerRefComponentType, ambienceTrackerComponentType);
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.ambienceTrackerComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         AmbienceTracker ambienceTrackerComponent = holder.getComponent(this.ambienceTrackerComponentType);

         assert ambienceTrackerComponent != null;

         PlayerRef playerRefComponent = holder.getComponent(this.playerRefComponentType);

         assert playerRefComponent != null;

         UpdateEnvironmentMusic pooledPacket = ambienceTrackerComponent.getMusicPacket();
         pooledPacket.environmentIndex = 0;
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
      private final ComponentType<EntityStore, AmbienceTracker> ambienceTrackerComponentType;
      @Nonnull
      private final ResourceType<EntityStore, AmbienceResource> ambienceResourceType;
      @Nonnull
      private final Query<EntityStore> query;

      public Tick(
         @Nonnull ComponentType<EntityStore, Player> playerComponentType,
         @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType,
         @Nonnull ComponentType<EntityStore, AmbienceTracker> ambienceTrackerComponentType,
         @Nonnull ResourceType<EntityStore, AmbienceResource> ambienceResourceType
      ) {
         this.playerRefComponentType = playerRefComponentType;
         this.ambienceTrackerComponentType = ambienceTrackerComponentType;
         this.ambienceResourceType = ambienceResourceType;
         this.query = Archetype.of(playerComponentType, playerRefComponentType, ambienceTrackerComponentType);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         AmbienceResource ambienceResource = store.getResource(this.ambienceResourceType);
         AmbienceTracker ambienceTrackerComponent = archetypeChunk.getComponent(index, this.ambienceTrackerComponentType);

         assert ambienceTrackerComponent != null;

         PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.playerRefComponentType);

         assert playerRefComponent != null;

         int have = ambienceTrackerComponent.getForcedMusicIndex();
         int desired = ambienceResource.getForcedMusicIndex();
         if (have != desired) {
            ambienceTrackerComponent.setForcedMusicIndex(desired);
            UpdateEnvironmentMusic pooledPacket = ambienceTrackerComponent.getMusicPacket();
            pooledPacket.environmentIndex = desired;
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
