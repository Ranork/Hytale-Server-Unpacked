package com.hypixel.hytale.server.core.modules.projectile.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class Projectile implements Component<EntityStore> {
   @Nonnull
   public static Projectile INSTANCE = new Projectile();
   @Nonnull
   public static final BuilderCodec<Projectile> CODEC = BuilderCodec.builder(Projectile.class, () -> INSTANCE).build();

   public static ComponentType<EntityStore, Projectile> getComponentType() {
      return ProjectileModule.get().getProjectileComponentType();
   }

   private Projectile() {
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return this;
   }
}
