package com.hypixel.hytale.builtin.adventure.objectives.systems;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.components.ObjectiveHistoryComponent;
import com.hypixel.hytale.builtin.adventure.objectives.config.gameplayconfig.ObjectiveGameplayConfig;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ObjectivePlayerSetupSystem extends RefSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, ObjectiveHistoryComponent> objectiveHistoryComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Player> playerComponentType;
   @Nonnull
   private final ComponentType<EntityStore, UUIDComponent> uuidComponentType = UUIDComponent.getComponentType();
   @Nonnull
   private final Query<EntityStore> query;

   public ObjectivePlayerSetupSystem(
      @Nonnull ComponentType<EntityStore, ObjectiveHistoryComponent> objectiveHistoryComponentType,
      @Nonnull ComponentType<EntityStore, Player> playerComponentType
   ) {
      this.objectiveHistoryComponentType = objectiveHistoryComponentType;
      this.playerComponentType = playerComponentType;
      this.query = Query.and(playerComponentType, this.uuidComponentType);
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
      commandBuffer.ensureComponent(ref, this.objectiveHistoryComponentType);
      Player playerComponent = store.getComponent(ref, this.playerComponentType);

      assert playerComponent != null;

      UUIDComponent uuidComponent = store.getComponent(ref, this.uuidComponentType);

      assert uuidComponent != null;

      ObjectivePlugin objectiveModule = ObjectivePlugin.get();
      UUID playerUuid = uuidComponent.getUuid();
      PlayerConfigData playerConfigData = playerComponent.getPlayerConfigData();
      Set<UUID> activeObjectiveUUIDs = playerConfigData.getActiveObjectiveUUIDs();
      if (activeObjectiveUUIDs != null) {
         for (UUID objectiveUUID : activeObjectiveUUIDs) {
            objectiveModule.addPlayerToExistingObjective(store, playerUuid, objectiveUUID);
         }
      }

      World world = store.getExternalData().getWorld();
      String worldName = world.getName();
      PlayerWorldData perWorldData = playerConfigData.getPerWorldData(worldName);
      if (perWorldData.isFirstSpawn()) {
         ObjectiveGameplayConfig config = ObjectiveGameplayConfig.get(world.getGameplayConfig());
         Map<String, String> starterObjectiveLinePerWorld = config != null ? config.getStarterObjectiveLinePerWorld() : null;
         if (starterObjectiveLinePerWorld != null) {
            String objectiveLineId = starterObjectiveLinePerWorld.get(worldName);
            if (objectiveLineId != null) {
               objectiveModule.startObjectiveLine(store, objectiveLineId, Set.of(playerUuid), world.getWorldConfig().getUuid(), null);
            }
         }
      }
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
   }
}
