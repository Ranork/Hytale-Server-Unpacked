package com.hypixel.hytale.server.core.modules.entity.damage;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DeferredCorpseRemoval implements Component<EntityStore> {
   protected double timeRemaining;

   public static ComponentType<EntityStore, DeferredCorpseRemoval> getComponentType() {
      return DamageModule.get().getDeferredCorpseRemovalComponentType();
   }

   public DeferredCorpseRemoval(double timeUntilCorpseRemoval) {
      this.timeRemaining = timeUntilCorpseRemoval;
   }

   public boolean tick(float dt) {
      return (this.timeRemaining -= dt) <= 0.0;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new DeferredCorpseRemoval(this.timeRemaining);
   }
}
