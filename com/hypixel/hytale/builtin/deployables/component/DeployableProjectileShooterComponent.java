package com.hypixel.hytale.builtin.deployables.component;

import com.hypixel.hytale.builtin.deployables.DeployablesPlugin;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public class DeployableProjectileShooterComponent implements Component<EntityStore> {
   @Nonnull
   protected final List<Ref<EntityStore>> projectiles = new ObjectArrayList();
   @Nonnull
   protected final List<Ref<EntityStore>> projectilesForRemoval = new ObjectArrayList();
   protected Ref<EntityStore> activeTarget;

   public static ComponentType<EntityStore, DeployableProjectileShooterComponent> getComponentType() {
      return DeployablesPlugin.get().getDeployableProjectileShooterComponentType();
   }

   public void spawnProjectile(
      Ref<EntityStore> entityRef,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ProjectileConfig projectileConfig,
      @Nonnull UUID ownerUuid,
      @Nonnull Vector3d spawnPos,
      @Nonnull Vector3d direction
   ) {
      commandBuffer.getExternalData().getWorld().execute(() -> {});
   }

   @Nonnull
   public List<Ref<EntityStore>> getProjectiles() {
      return this.projectiles;
   }

   @Nonnull
   public List<Ref<EntityStore>> getProjectilesForRemoval() {
      return this.projectilesForRemoval;
   }

   public Ref<EntityStore> getActiveTarget() {
      return this.activeTarget;
   }

   public void setActiveTarget(Ref<EntityStore> target) {
      this.activeTarget = target;
   }

   @Override
   public Component<EntityStore> clone() {
      return this;
   }
}
