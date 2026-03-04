package com.hypixel.hytale.builtin.adventure.npcobjectives.transaction;

import com.hypixel.hytale.builtin.adventure.npcobjectives.resources.KillTrackerResource;
import com.hypixel.hytale.builtin.adventure.npcobjectives.task.KillTask;
import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class KillTaskTransaction extends TransactionRecord {
   @Nonnull
   private final KillTask task;
   @Nonnull
   private final Objective objective;
   @Nonnull
   private final ComponentAccessor<EntityStore> componentAccessor;

   public KillTaskTransaction(@Nonnull KillTask task, @Nonnull Objective objective, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.task = task;
      this.objective = objective;
      this.componentAccessor = componentAccessor;
   }

   @Override
   public void revert() {
      this.componentAccessor.getResource(KillTrackerResource.getResourceType()).unwatch(this);
   }

   @Override
   public void complete() {
      this.componentAccessor.getResource(KillTrackerResource.getResourceType()).unwatch(this);
   }

   @Override
   public void unload() {
      this.componentAccessor.getResource(KillTrackerResource.getResourceType()).unwatch(this);
   }

   @Nonnull
   public KillTask getTask() {
      return this.task;
   }

   @Nonnull
   public Objective getObjective() {
      return this.objective;
   }

   @Override
   public boolean shouldBeSerialized() {
      return false;
   }
}
