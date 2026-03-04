package com.hypixel.hytale.server.core.modules.entity.stamina;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

public class StaminaSystems {
   public static class SprintStaminaEffectSystem extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      private final ComponentType<EntityStore, EntityStatMap> entityStatMapComponentType = EntityStatMap.getComponentType();
      private final ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType = MovementStatesComponent.getComponentType();
      private final ResourceType<EntityStore, SprintStaminaRegenDelay> sprintRegenDelayResourceType = SprintStaminaRegenDelay.getResourceType();
      private final Query<EntityStore> query = Query.and(this.playerComponentType, this.entityStatMapComponentType, this.movementStatesComponentType);
      private final Set<Dependency<EntityStore>> dependencies = Set.of(
         new SystemDependency<>(Order.BEFORE, MovementStatesSystems.TickingSystem.class),
         new SystemDependency<>(Order.BEFORE, EntityStatsModule.PlayerRegenerateStatsSystem.class)
      );

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
      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         if (this.updateResource(store)) {
            super.tick(dt, systemIndex, store);
         }
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         MovementStatesComponent movementStates = archetypeChunk.getComponent(index, this.movementStatesComponentType);
         if (!movementStates.getMovementStates().sprinting && movementStates.getSentMovementStates().sprinting) {
            SprintStaminaRegenDelay regenDelay = store.getResource(this.sprintRegenDelayResourceType);
            EntityStatMap statMap = archetypeChunk.getComponent(index, this.entityStatMapComponentType);
            EntityStatValue statValue = statMap.get(regenDelay.getIndex());
            if (statValue != null && statValue.get() <= regenDelay.getValue()) {
               return;
            }

            statMap.setStatValue(regenDelay.getIndex(), regenDelay.getValue());
         }
      }

      protected boolean updateResource(@Nonnull Store<EntityStore> store) {
         SprintStaminaRegenDelay resource = store.getResource(this.sprintRegenDelayResourceType);
         if (resource.validate()) {
            return resource.hasDelay();
         } else {
            GameplayConfig gameplayConfig = store.getExternalData().getWorld().getGameplayConfig();
            StaminaGameplayConfig staminaConfig = gameplayConfig.getPluginConfig().get(StaminaGameplayConfig.class);
            if (staminaConfig != null && staminaConfig.getSprintRegenDelay().getIndex() != Integer.MIN_VALUE) {
               StaminaGameplayConfig.SprintRegenDelayConfig regenDelay = staminaConfig.getSprintRegenDelay();
               resource.update(regenDelay.getIndex(), regenDelay.getValue());
               return resource.hasDelay();
            } else {
               resource.markEmpty();
               return false;
            }
         }
      }
   }
}
