package com.hypixel.hytale.server.core.modules.physics.util;

import javax.annotation.Nonnull;
import org.joml.Vector3d;

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
      this.nextTickVelocity.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
   }

   public void convertToForces(double dt, double mass) {
      this.externalForce.fma(1.0 / mass, this.externalAcceleration);
      this.externalForce.fma(1.0 / dt, this.externalImpulse);
      this.externalAcceleration.zero();
      this.externalImpulse.zero();
   }

   public void updateVelocity(@Nonnull Vector3d velocity) {
      if (this.nextTickVelocity.x < Double.MAX_VALUE) {
         velocity.set(this.nextTickVelocity);
         this.nextTickVelocity.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
      }

      velocity.add(this.externalVelocity);
      this.externalVelocity.zero();
   }

   public void clear() {
      this.externalForce.zero();
   }
}
