package com.hypixel.hytale.builtin.deployables.component;

import com.hypixel.hytale.builtin.deployables.DeployablesPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DeployableProjectileComponent implements Component<EntityStore> {
   @Nonnull
   protected Vector3d previousTickPosition;

   public DeployableProjectileComponent() {
      this(Vector3d.ZERO.clone());
   }

   public DeployableProjectileComponent(@Nonnull Vector3d previousTickPosition) {
      this.previousTickPosition = previousTickPosition;
   }

   public static ComponentType<EntityStore, DeployableProjectileComponent> getComponentType() {
      return DeployablesPlugin.get().getDeployableProjectileComponentType();
   }

   @Override
   public Component<EntityStore> clone() {
      return new DeployableProjectileComponent(this.previousTickPosition.clone());
   }

   @Nonnull
   public Vector3d getPreviousTickPosition() {
      return this.previousTickPosition.clone();
   }

   public void setPreviousTickPosition(@Nonnull Vector3d pos) {
      this.previousTickPosition = pos.clone();
   }
}
