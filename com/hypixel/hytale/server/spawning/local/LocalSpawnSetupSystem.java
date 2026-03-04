package com.hypixel.hytale.server.spawning.local;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LocalSpawnSetupSystem extends RefSystem<EntityStore> {
   private final ComponentType<EntityStore, Player> componentType;

   public LocalSpawnSetupSystem(ComponentType<EntityStore, Player> componentType) {
      this.componentType = componentType;
   }

   @Override
   public void onEntityAdded(
      @Nonnull Ref<EntityStore> reference, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      commandBuffer.ensureComponent(reference, LocalSpawnController.getComponentType());
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<EntityStore> reference, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
   }

   @Override
   public Query<EntityStore> getQuery() {
      return this.componentType;
   }
}
