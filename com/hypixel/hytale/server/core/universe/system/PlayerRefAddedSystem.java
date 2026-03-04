package com.hypixel.hytale.server.core.universe.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

public class PlayerRefAddedSystem extends RefSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;

   public PlayerRefAddedSystem(@Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType) {
      this.playerRefComponentType = playerRefComponentType;
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return RootDependency.firstSet();
   }

   @Override
   public Query<EntityStore> getQuery() {
      return this.playerRefComponentType;
   }

   @Override
   public void onEntityAdded(
      @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PlayerRef playerRefComponent = store.getComponent(ref, this.playerRefComponentType);

      assert playerRefComponent != null;

      playerRefComponent.addedToStore(ref);
      store.getExternalData().getWorld().trackPlayerRef(playerRefComponent);
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PlayerRef playerRefComponent = store.getComponent(ref, this.playerRefComponentType);

      assert playerRefComponent != null;

      store.getExternalData().getWorld().untrackPlayerRef(playerRefComponent);
   }
}
