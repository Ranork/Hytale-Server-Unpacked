package com.hypixel.hytale.server.core.modules.physics.util;

import com.hypixel.hytale.math.vector.Vector3dUtil;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

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
      after.position.set(before.position).fma(dt, before.velocity);
   }

   protected static void updatePositionAfterVelocity(@Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double dt) {
      after.position.set(before.position).fma(dt, after.velocity);
   }

   protected void updateAndClampVelocity(@Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double dt) {
      this.updateVelocity(before, after, dt);
      Vector3dUtil.clipToZero(after.velocity, MIN_VELOCITY);
   }

   protected void updateVelocity(@Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double dt) {
      after.velocity.set(before.velocity).fma(dt, this.acceleration);
   }

   protected void computeAcceleration(double mass) {
      this.acceleration.set(this.accumulator.force).mul(1.0 / mass);
   }

   protected void computeAcceleration(@Nonnull PhysicsBodyState state, boolean onGround, @Nonnull ForceProvider[] forceProviders, double mass, double timeStep) {
      this.accumulator.computeResultingForce(state, onGround, forceProviders, mass, timeStep);
      this.computeAcceleration(mass);
   }

   protected void assignAcceleration(@Nonnull PhysicsBodyState state) {
      state.velocity.set(this.acceleration);
   }

   protected void addAcceleration(@Nonnull PhysicsBodyState state, double scale) {
      state.velocity.fma(scale, this.acceleration);
   }

   protected void addAcceleration(@Nonnull PhysicsBodyState state) {
      state.velocity.add(this.acceleration);
   }

   protected void convertAccelerationToVelocity(@Nonnull PhysicsBodyState before, @Nonnull PhysicsBodyState after, double scale) {
      Vector3dUtil.clipToZero(after.velocity.mul(scale).add(before.velocity), MIN_VELOCITY);
   }
}
