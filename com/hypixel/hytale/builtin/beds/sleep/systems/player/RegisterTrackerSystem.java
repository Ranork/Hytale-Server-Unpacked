package com.hypixel.hytale.builtin.beds.sleep.systems.player;

import com.hypixel.hytale.builtin.beds.sleep.components.SleepTracker;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RegisterTrackerSystem extends HolderSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, SleepTracker> sleepTrackerComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public RegisterTrackerSystem(
      @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType, @Nonnull ComponentType<EntityStore, SleepTracker> sleepTrackerComponentType
   ) {
      this.sleepTrackerComponentType = sleepTrackerComponentType;
      this.query = playerRefComponentType;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      holder.ensureComponent(this.sleepTrackerComponentType);
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
