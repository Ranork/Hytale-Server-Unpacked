package com.hypixel.hytale.builtin.deployables.component;

import com.hypixel.hytale.builtin.deployables.DeployablesPlugin;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class DeployableProjectileShooterComponent implements Component<EntityStore> {
   @Nonnull
   protected final List<Ref<EntityStore>> projectiles = new ReferenceArrayList();
   @Nonnull
   protected final List<Ref<EntityStore>> projectilesForRemoval = new ReferenceArrayList();
   protected Ref<EntityStore> activeTarget;

   public static ComponentType<EntityStore, DeployableProjectileShooterComponent> getComponentType() {
      return DeployablesPlugin.get().getDeployableProjectileShooterComponentType();
   }

   public void spawnProjectile(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull ProjectileConfig projectileConfig,
      @Nonnull Vector3d spawnPos,
      @Nonnull Vector3d direction
   ) {
      Ref<EntityStore> projectileRef = ProjectileModule.get().spawnProjectile(entityRef, commandBuffer, projectileConfig, spawnPos, direction);
      DeployableProjectileComponent deployableProjectileComponent = commandBuffer.addComponent(projectileRef, DeployableProjectileComponent.getComponentType());
      deployableProjectileComponent.setPreviousTickPosition(spawnPos);
      commandBuffer.run(store -> {
         if (projectileRef.isValid()) {
            StandardPhysicsProvider physics = store.getComponent(projectileRef, StandardPhysicsProvider.getComponentType());
            if (physics != null) {
               physics.setImpactConsumer((ref, pos, targetRef, detail, cb) -> {
                  if (targetRef != null) {
                     deployableProjectileComponent.setHitEntityRef(targetRef);
                  }
               });
               physics.setBounceConsumer(null);
            }
         }
      });
      this.projectiles.add(projectileRef);
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
