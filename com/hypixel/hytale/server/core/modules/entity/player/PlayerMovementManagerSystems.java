package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerMovementManagerSystems {
   public static class AssignmentSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private static final ComponentType<EntityStore, MovementManager> MOVEMENT_MANAGER_COMPONENT_TYPE = MovementManager.getComponentType();
      @Nonnull
      private static final Query<EntityStore> QUERY = Query.and(PlayerRef.getComponentType(), Query.not(MOVEMENT_MANAGER_COMPONENT_TYPE));

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(MOVEMENT_MANAGER_COMPONENT_TYPE);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class PostAssignmentSystem extends RefSystem<EntityStore> {
      @Nonnull
      private static final ComponentType<EntityStore, MovementManager> MOVEMENT_MANAGER_COMPONENT_TYPE = MovementManager.getComponentType();
      @Nonnull
      private static final Query<EntityStore> QUERY = Query.and(MOVEMENT_MANAGER_COMPONENT_TYPE, PlayerRef.getComponentType());

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         MovementManager movementManagerComponent = commandBuffer.getComponent(ref, MOVEMENT_MANAGER_COMPONENT_TYPE);

         assert movementManagerComponent != null;

         movementManagerComponent.resetDefaultsAndUpdate(ref, commandBuffer);
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }
}
