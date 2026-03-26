package com.hypixel.hytale.server.core.modules.entity.damage;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeferredCorpseRemoval implements Component<EntityStore> {
   protected double timeRemaining;
   @Nullable
   protected String deathParticles;

   public static ComponentType<EntityStore, DeferredCorpseRemoval> getComponentType() {
      return DamageModule.get().getDeferredCorpseRemovalComponentType();
   }

   public DeferredCorpseRemoval(double timeUntilCorpseRemoval, @Nullable String deathParticles) {
      this.timeRemaining = timeUntilCorpseRemoval;
      this.deathParticles = deathParticles;
   }

   public void tick(float dt) {
      this.timeRemaining -= dt;
   }

   public boolean shouldRemove() {
      return this.timeRemaining <= 0.0;
   }

   @Nullable
   public String getDeathParticles() {
      return this.deathParticles;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new DeferredCorpseRemoval(this.timeRemaining, this.deathParticles);
   }
}
