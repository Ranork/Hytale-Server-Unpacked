package com.hypixel.hytale.server.core.modules.physics.util;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ForceProviderStandardState {
   public double displacedMass;
   public double dragCoefficient;
   public double gravity;
   public final Vector3d nextTickVelocity = new Vector3d();
   public final Vector3d externalVelocity = new Vector3d();
   public final Vector3d externalAcceleration = new Vector3d();
   public final Vector3d externalForce = new Vector3d();
   public final Vector3d externalImpulse = new Vector3d();

   public ForceProviderStandardState() {
      this.nextTickVelocity.assign(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
   }

   public void convertToForces(double dt, double mass) {
      this.externalForce.addScaled(this.externalAcceleration, 1.0 / mass);
      this.externalForce.addScaled(this.externalImpulse, 1.0 / dt);
      this.externalAcceleration.assign(Vector3d.ZERO);
      this.externalImpulse.assign(Vector3d.ZERO);
   }

   public void updateVelocity(@Nonnull Vector3d velocity) {
      if (this.nextTickVelocity.x < Double.MAX_VALUE) {
         velocity.assign(this.nextTickVelocity);
         this.nextTickVelocity.assign(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
      }

      velocity.add(this.externalVelocity);
      this.externalVelocity.assign(Vector3d.ZERO);
   }

   public void clear() {
      this.externalForce.assign(Vector3d.ZERO);
   }
}
