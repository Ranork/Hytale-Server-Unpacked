package com.hypixel.hytale.server.npc.movement.steeringforces;

import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.npc.movement.Steering;
import javax.annotation.Nonnull;

public class SteeringForceWander implements SteeringForce {
   private double time;
   private double turnInterval;
   private double jitter = 0.5;
   private final Vector3d velocity = new Vector3d(0.0, 0.0, -1.0);

   public SteeringForceWander() {
      this.setTurnTime(5.0);
      this.jitter = 0.5;
   }

   public void setTurnTime(double t) {
      this.turnInterval = 0.0;
      this.time = this.turnInterval;
   }

   public void updateTime(double dt) {
      this.time += dt;
   }

   public void setHeading(float heading) {
      this.velocity.x = PhysicsMath.headingX(heading);
      this.velocity.z = PhysicsMath.headingZ(heading);
   }

   @Override
   public boolean compute(@Nonnull Steering output) {
      if (this.time < this.turnInterval) {
         this.velocity.x = this.velocity.x + RandomExtra.randomBinomial() * this.jitter;
         this.velocity.z = this.velocity.z + RandomExtra.randomBinomial() * this.jitter;
         this.velocity.normalize();
      }

      output.clearRotation();
      return true;
   }
}
