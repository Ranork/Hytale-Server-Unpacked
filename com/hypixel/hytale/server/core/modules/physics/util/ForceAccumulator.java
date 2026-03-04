package com.hypixel.hytale.server.core.modules.physics.util;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ForceAccumulator {
   public double speed;
   public final Vector3d force = new Vector3d();
   public final Vector3d resistanceForceLimit = new Vector3d();

   public void initialize(@Nonnull PhysicsBodyState state, double mass, double timeStep) {
      this.force.assign(Vector3d.ZERO);
      this.speed = state.velocity.length();
      this.resistanceForceLimit.assign(state.velocity).scale(-mass / timeStep);
   }

   protected void computeResultingForce(
      @Nonnull PhysicsBodyState state, boolean onGround, @Nonnull ForceProvider[] forceProviders, double mass, double timeStep
   ) {
      this.initialize(state, mass, timeStep);

      for (ForceProvider provider : forceProviders) {
         provider.update(state, this, onGround);
      }
   }
}
