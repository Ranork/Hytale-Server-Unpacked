package com.hypixel.hytale.builtin.deployables.component;

import com.hypixel.hytale.builtin.deployables.DeployablesPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class DeployableProjectileComponent implements Component<EntityStore> {
   @Nonnull
   protected Vector3d previousTickPosition;
   @Nullable
   private Ref<EntityStore> hitEntityRef;

   public DeployableProjectileComponent() {
      this(new Vector3d(Vector3dUtil.ZERO));
   }

   public DeployableProjectileComponent(@Nonnull Vector3d previousTickPosition) {
      this.previousTickPosition = previousTickPosition;
   }

   public static ComponentType<EntityStore, DeployableProjectileComponent> getComponentType() {
      return DeployablesPlugin.get().getDeployableProjectileComponentType();
   }

   @Override
   public Component<EntityStore> clone() {
      return new DeployableProjectileComponent(new Vector3d(this.previousTickPosition));
   }

   @Nonnull
   public Vector3d getPreviousTickPosition() {
      return new Vector3d(this.previousTickPosition);
   }

   public void setPreviousTickPosition(@Nonnull Vector3d pos) {
      this.previousTickPosition = new Vector3d(pos);
   }

   public void setHitEntityRef(@Nonnull Ref<EntityStore> ref) {
      this.hitEntityRef = ref;
   }

   @Nullable
   public Ref<EntityStore> takeHitEntityRef() {
      Ref<EntityStore> ref = this.hitEntityRef;
      this.hitEntityRef = null;
      return ref;
   }
}
