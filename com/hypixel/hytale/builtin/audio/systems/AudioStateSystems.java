package com.hypixel.hytale.builtin.audio.systems;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.audio.components.AudioStateComponent;
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
import com.hypixel.hytale.protocol.AudioStateAuthority;
import com.hypixel.hytale.protocol.StateTransition;
import com.hypixel.hytale.protocol.packets.world.SetAudioState;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioState;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class AudioStateSystems {
   public static class PlayerAdded extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, AudioStateComponent> audioStateComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public PlayerAdded(
         @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType,
         @Nonnull ComponentType<EntityStore, AudioStateComponent> audioStateComponentType
      ) {
         this.audioStateComponentType = audioStateComponentType;
         this.query = Query.and(playerRefComponentType);
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.audioStateComponentType);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
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
      private final ComponentType<EntityStore, AudioStateComponent> audioStateComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public Tick(
         @Nonnull ComponentType<EntityStore, Player> playerComponentType,
         @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType,
         @Nonnull ComponentType<EntityStore, AudioStateComponent> audioStateComponentType
      ) {
         this.playerRefComponentType = playerRefComponentType;
         this.audioStateComponentType = audioStateComponentType;
         this.query = Archetype.of(playerComponentType, playerRefComponentType, audioStateComponentType);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         AudioStateComponent component = archetypeChunk.getComponent(index, this.audioStateComponentType);

         assert component != null;

         PlayerRef playerRefComponent = archetypeChunk.getComponent(index, this.playerRefComponentType);

         assert playerRefComponent != null;

         int[] versions = component.getValueVersions();
         int[] lastSent = component.getLastSentValueVersions();
         int[] currentValues = component.getCurrentValueIndices();
         StateTransition[] overrides = component.getActiveTransitionOverrides();
         SetAudioState packet = component.getPooledPacket();
         IndexedLookupTableAssetMap<String, AudioState> assetMap = AudioState.getAssetMap();

         for (int axis = 0; axis < versions.length; axis++) {
            if (versions[axis] != lastSent[axis]) {
               AudioState asset = assetMap.getAsset(axis);
               if (asset != null && asset.getAuthority() == AudioStateAuthority.Server) {
                  component.setLastSentValueVersion(axis, versions[axis]);
                  packet.audioStateIndex = axis;
                  packet.valueIndex = currentValues[axis];
                  packet.transitionOverride = overrides[axis];
                  playerRefComponent.getPacketHandler().write(packet);
               } else {
                  lastSent[axis] = versions[axis];
               }
            }
         }
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }
}
