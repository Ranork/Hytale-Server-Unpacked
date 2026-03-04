package com.hypixel.hytale.server.npc.movement.steeringforces;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.npc.movement.Steering;
import javax.annotation.Nonnull;

public abstract class SteeringForceWithTarget implements SteeringForce {
   protected final Vector3d selfPosition = new Vector3d();
   protected final Vector3d targetPosition = new Vector3d();
   private Vector3d componentSelector;

   @Nonnull
   public Vector3d getSelfPosition() {
      return this.selfPosition;
   }

   public void setSelfPosition(@Nonnull Vector3d selfPosition) {
      this.selfPosition.assign(selfPosition);
   }

   @Nonnull
   public Vector3d getTargetPosition() {
      return this.targetPosition;
   }

   public void setTargetPosition(@Nonnull Vector3d targetPosition) {
      this.targetPosition.assign(targetPosition);
   }

   public void setTargetPosition(double x, double y, double z) {
      this.targetPosition.assign(x, y, z);
   }

   public void setPositions(@Nonnull Vector3d self, @Nonnull Vector3d target) {
      this.setSelfPosition(self);
      this.setTargetPosition(target);
   }

   public void setSelfPosition(double x, double y, double z) {
      this.selfPosition.assign(x, y, z);
   }

   public void setComponentSelector(Vector3d componentSelector) {
      this.componentSelector = componentSelector;
   }

   @Override
   public boolean compute(Steering output) {
      this.selfPosition.scale(this.componentSelector);
      this.targetPosition.scale(this.componentSelector);
      return true;
   }
}
