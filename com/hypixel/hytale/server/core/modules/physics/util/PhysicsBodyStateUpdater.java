package com.hypixel.hytale.server.core.modules.physics.util;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class PhysicsBodyStateUpdater {
   protected static double MIN_VELOCITY = 1.0E-6;
   @Nonnull
   protected Vector3d acceleration = new Vector3d();
   protected final ForceAccumulator accumulator = new ForceAccumulator();

   public void update(
      @Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double mass, double dt, boolean onGround, @Nonnull ForceProvider[] forceProvider
   ) {
      this.computeAcceleration(before, onGround, forceProvider, mass, dt);
      updatePositionBeforeVelocity(before, after, dt);
      this.updateAndClampVelocity(before, after, dt);
   }

   protected static void updatePositionBeforeVelocity(@Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double dt) {
      after.position.assign(before.position).addScaled(before.velocity, dt);
   }

   protected static void updatePositionAfterVelocity(@Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double dt) {
      after.position.assign(before.position).addScaled(after.velocity, dt);
   }

   protected void updateAndClampVelocity(@Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double dt) {
      this.updateVelocity(before, after, dt);
      after.velocity.clipToZero(MIN_VELOCITY);
   }

   protected void updateVelocity(@Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double dt) {
      after.velocity.assign(before.velocity).addScaled(this.acceleration, dt);
   }

   protected void computeAcceleration(double mass) {
      this.acceleration.assign(this.accumulator.force).scale(1.0 / mass);
   }

   protected void computeAcceleration(@Nonnull PhysicsBodyState state, boolean onGround, @Nonnull ForceProvider[] forceProviders, double mass, double timeStep) {
      this.accumulator.computeResultingForce(state, onGround, forceProviders, mass, timeStep);
      this.computeAcceleration(mass);
   }

   protected void assignAcceleration(@Nonnull PhysicsBodyState state) {
      state.velocity.assign(this.acceleration);
   }

   protected void addAcceleration(@Nonnull PhysicsBodyState state, double scale) {
      state.velocity.addScaled(this.acceleration, scale);
   }

   protected void addAcceleration(@Nonnull PhysicsBodyState state) {
      state.velocity.add(this.acceleration);
   }

   protected void convertAccelerationToVelocity(@Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double scale) {
      after.velocity.scale(scale).add(before.velocity).clipToZero(MIN_VELOCITY);
   }
}
