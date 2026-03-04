package com.hypixel.hytale.server.core.modules.physics.util;

import javax.annotation.Nonnull;

public class PhysicsBodyStateUpdaterSymplecticEuler extends PhysicsBodyStateUpdater {
   @Override
   public void update(
      @Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double mass, double dt, boolean onGround, @Nonnull ForceProvider[] forceProvider
   ) {
      this.computeAcceleration(before, onGround, forceProvider, mass, dt);
      this.updateAndClampVelocity(before, after, dt);
      updatePositionAfterVelocity(before, after, dt);
   }
}
