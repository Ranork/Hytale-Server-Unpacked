package com.hypixel.hytale.builtin.adventure.objectives.completion;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.ObjectiveCompletionAsset;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public abstract class ObjectiveCompletion {
   @Nonnull
   protected final ObjectiveCompletionAsset asset;

   public ObjectiveCompletion(@Nonnull ObjectiveCompletionAsset asset) {
      this.asset = asset;
   }

   @Nonnull
   public ObjectiveCompletionAsset getAsset() {
      return this.asset;
   }

   public abstract void handle(@Nonnull Objective var1, @Nonnull ComponentAccessor<EntityStore> var2);

   @Nonnull
   @Override
   public String toString() {
      return "ObjectiveCompletion{asset=" + this.asset + "}";
   }
}
