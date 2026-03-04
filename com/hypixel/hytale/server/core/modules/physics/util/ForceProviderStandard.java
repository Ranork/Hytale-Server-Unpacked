package com.hypixel.hytale.server.core.modules.physics.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public abstract class ForceProviderStandard implements ForceProvider {
   @Nonnull
   public static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   protected Vector3d dragForce = new Vector3d();

   public abstract double getMass(double var1);

   public abstract double getVolume();

   public abstract double getDensity();

   public abstract double getProjectedArea(PhysicsBodyState var1, double var2);

   public abstract double getFrictionCoefficient();

   public abstract ForceProviderStandardState getForceProviderStandardState();

   @Override
   public void update(@Nonnull PhysicsBodyState bodyState, @Nonnull ForceAccumulator accumulator, boolean onGround) {
      ForceProviderStandardState standardState = this.getForceProviderStandardState();
      Vector3d extForce = standardState.externalForce;
      double extForceY = extForce.y;
      accumulator.force.add(extForce);
      double speed = accumulator.speed;
      double dragForceDivSpeed = standardState.dragCoefficient * this.getProjectedArea(bodyState, speed) * speed;
      this.dragForce.assign(bodyState.velocity).scale(-dragForceDivSpeed);
      this.clipForce(this.dragForce, accumulator.resistanceForceLimit);
      accumulator.force.add(this.dragForce);
      double gravityForce = -standardState.gravity * this.getMass(this.getVolume());
      if (onGround) {
         double frictionForce = (gravityForce + extForceY) * this.getFrictionCoefficient();
         if (speed > 0.0 && frictionForce > 0.0) {
            frictionForce /= speed;
            accumulator.force.x = accumulator.force.x - bodyState.velocity.x * frictionForce;
            accumulator.force.z = accumulator.force.z - bodyState.velocity.z * frictionForce;
         }
      } else {
         accumulator.force.y += gravityForce;
      }

      if (standardState.displacedMass != 0.0) {
         accumulator.force.y = accumulator.force.y + standardState.displacedMass * standardState.gravity;
      }
   }

   public void clipForce(@Nonnull Vector3d value, @Nonnull Vector3d threshold) {
      if (threshold.x < 0.0) {
         if (value.x < threshold.x) {
            value.x = threshold.x;
         }
      } else if (value.x > threshold.x) {
         value.x = threshold.x;
      }

      if (threshold.y < 0.0) {
         if (value.y < threshold.y) {
            value.y = threshold.y;
         }
      } else if (value.y > threshold.y) {
         value.y = threshold.y;
      }

      if (threshold.z < 0.0) {
         if (value.z < threshold.z) {
            value.z = threshold.z;
         }
      } else if (value.z > threshold.z) {
         value.z = threshold.z;
      }
   }
}
