package com.hypixel.hytale.server.core.modules.projectile.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PredictedProjectile implements Component<EntityStore> {
   @Nonnull
   private final UUID uuid;

   @Nonnull
   public static ComponentType<EntityStore, PredictedProjectile> getComponentType() {
      return ProjectileModule.get().getPredictedProjectileComponentType();
   }

   public PredictedProjectile(@Nonnull UUID uuid) {
      this.uuid = uuid;
   }

   @Nonnull
   public UUID getUuid() {
      return this.uuid;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return this;
   }
}
