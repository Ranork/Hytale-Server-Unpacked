package com.hypixel.hytale.builtin.parkour;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class ParkourCheckpointSystems {
   public static class EnsureNetworkSendable extends HolderSystem<EntityStore> {
      private final Query<EntityStore> query = Query.and(ParkourCheckpoint.getComponentType(), Query.not(NetworkId.getComponentType()));

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
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

   public static class Init extends RefSystem<EntityStore> {
      private final ComponentType<EntityStore, ParkourCheckpoint> parkourCheckpointComponentType;
      @Nonnull
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public Init(ComponentType<EntityStore, ParkourCheckpoint> parkourCheckpointComponentType) {
         this.parkourCheckpointComponentType = parkourCheckpointComponentType;
         this.uuidComponentComponentType = UUIDComponent.getComponentType();
         ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
         this.query = Query.and(parkourCheckpointComponentType, transformComponentType);
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
         ParkourCheckpoint entity = store.getComponent(ref, this.parkourCheckpointComponentType);
         ParkourPlugin.get().updateLastIndex(entity.getIndex());
         ParkourPlugin.get().getCheckpointUUIDMap().put(entity.getIndex(), store.getComponent(ref, this.uuidComponentComponentType).getUuid());
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class Ticking extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, ParkourCheckpoint> parkourCheckpointComponentType;
      private final ComponentType<EntityStore, Player> playerComponentType;
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent;
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentType;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public Ticking(
         ComponentType<EntityStore, ParkourCheckpoint> parkourCheckpointComponentType,
         ComponentType<EntityStore, Player> playerComponentType,
         ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent
      ) {
         this.parkourCheckpointComponentType = parkourCheckpointComponentType;
         this.playerComponentType = playerComponentType;
         this.playerSpatialComponent = playerSpatialComponent;
         this.transformComponentType = TransformComponent.getComponentType();
         this.uuidComponentType = UUIDComponent.getComponentType();
         this.query = Query.and(parkourCheckpointComponentType, this.transformComponentType);
         this.dependencies = Set.of(new SystemDependency<>(Order.AFTER, PlayerSpatialSystem.class, OrderPriority.CLOSEST));
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         int lastIndex = ParkourPlugin.get().getLastIndex();
         if (lastIndex != 0) {
            int parkourCheckpointIndex = archetypeChunk.getComponent(index, this.parkourCheckpointComponentType).getIndex();
            SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.playerSpatialComponent);
            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            Vector3d position = archetypeChunk.getComponent(index, this.transformComponentType).getPosition();
            spatialResource.getSpatialStructure().ordered(position, 1.0, results);
            ParkourPlugin parkourPlugin = ParkourPlugin.get();
            Object2IntMap<UUID> currentCheckpointByPlayerMap = parkourPlugin.getCurrentCheckpointByPlayerMap();
            Object2LongMap<UUID> startTimeByPlayerMap = parkourPlugin.getStartTimeByPlayerMap();

            for (int i = 0; i < results.size(); i++) {
               Ref<EntityStore> otherReference = (Ref<EntityStore>)results.get(i);
               UUIDComponent uuidComponent = commandBuffer.getComponent(otherReference, this.uuidComponentType);
               UUID playerUuid = uuidComponent.getUuid();
               Player player = commandBuffer.getComponent(otherReference, this.playerComponentType);
               handleCheckpointUpdate(currentCheckpointByPlayerMap, startTimeByPlayerMap, player, playerUuid, parkourCheckpointIndex, lastIndex);
            }
         }
      }

      private static void handleCheckpointUpdate(
         @Nonnull Object2IntMap<UUID> currentCheckpointByPlayerMap,
         @Nonnull Object2LongMap<UUID> startTimeByPlayerMap,
         @Nonnull Player player,
         UUID playerUuid,
         int checkpointIndex,
         int lastIndex
      ) {
         int currentCheckpoint = currentCheckpointByPlayerMap.getOrDefault(playerUuid, -1);
         if (currentCheckpoint == -1) {
            if (checkpointIndex != 0) {
               return;
            }

            currentCheckpointByPlayerMap.put(playerUuid, 0);
            startTimeByPlayerMap.put(playerUuid, System.nanoTime());
            player.sendMessage(Message.translation("server.general.parkourRun.started"));
         } else {
            if (currentCheckpoint + 1 != checkpointIndex) {
               return;
            }

            if (lastIndex == checkpointIndex) {
               long completionTimeNano = System.nanoTime() - startTimeByPlayerMap.getLong(playerUuid);
               long completionTimeMillis = TimeUnit.NANOSECONDS.toMillis(completionTimeNano);
               player.sendMessage(Message.translation("server.general.parkourRun.completed").param("seconds", completionTimeMillis / 1000.0));
               currentCheckpointByPlayerMap.remove(playerUuid, currentCheckpoint);
               return;
            }

            currentCheckpointByPlayerMap.put(playerUuid, checkpointIndex);
            player.sendMessage(
               Message.translation("server.general.parkourRun.checkpointReached").param("checkpoint", checkpointIndex).param("checkpoints", lastIndex)
            );
         }
      }
   }
}
