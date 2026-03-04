package com.hypixel.hytale.server.core.modules.physics.util;

import javax.annotation.Nonnull;

public class PhysicsBodyStateUpdaterRK4 extends PhysicsBodyStateUpdater {
   private final PhysicsBodyState state = new PhysicsBodyState();

   @Override
   public void update(
      @Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double mass, double dt, boolean onGround, @Nonnull ForceProvider[] forceProvider
   ) {
      double halfTime = dt * 0.5;
      this.computeAcceleration(before, onGround, forceProvider, mass, halfTime);
      this.assignAcceleration(after);
      this.updateVelocity(before, this.state, halfTime);
      updatePositionBeforeVelocity(before, this.state, halfTime);
      this.computeAcceleration(this.state, onGround, forceProvider, mass, halfTime);
      this.addAcceleration(after, 2.0);
      this.updateVelocity(before, this.state, halfTime);
      updatePositionAfterVelocity(before, this.state, halfTime);
      this.computeAcceleration(this.state, onGround, forceProvider, mass, halfTime);
      this.addAcceleration(after, 2.0);
      this.updateVelocity(before, this.state, dt);
      updatePositionAfterVelocity(before, this.state, dt);
      this.computeAcceleration(this.state, onGround, forceProvider, mass, dt);
      this.addAcceleration(after);
      this.convertAccelerationToVelocity(before, after, dt / 6.0);
      updatePositionAfterVelocity(before, after, dt);
   }
}
