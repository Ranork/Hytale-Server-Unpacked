package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerCollisionResultAddSystem extends HolderSystem<EntityStore> {
   @Nonnull
   private final Query<EntityStore> query;
   @Nonnull
   private final ComponentType<EntityStore, CollisionResultComponent> collisionResultComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Player> playerComponentType;

   public PlayerCollisionResultAddSystem(
      @Nonnull ComponentType<EntityStore, Player> playerComponentType,
      @Nonnull ComponentType<EntityStore, CollisionResultComponent> collisionResultComponentType
   ) {
      this.collisionResultComponentType = collisionResultComponentType;
      this.playerComponentType = playerComponentType;
      this.query = Query.and(playerComponentType, Query.not(collisionResultComponentType));
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      Player playerComponent = holder.getComponent(this.playerComponentType);

      assert playerComponent != null;

      CollisionResultComponent collisionResultComponent = new CollisionResultComponent();
      CollisionResult collisionResult = collisionResultComponent.getCollisionResult();
      collisionResult.setDefaultPlayerSettings();
      collisionResultComponent.resetLocationChange();
      playerComponent.configTriggerBlockProcessing(true, true, collisionResultComponent);
      holder.addComponent(this.collisionResultComponentType, collisionResultComponent);
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
