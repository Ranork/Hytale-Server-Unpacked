package com.hypixel.hytale.server.npc.movement.steeringforces;

import com.hypixel.hytale.server.npc.movement.Steering;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class SteeringForcePursue extends SteeringForceWithTarget {
   private double stopDistance;
   private double slowdownDistance;
   private double falloff = 3.0;
   private double invFalloff = 1.0 / this.falloff;
   private double squaredStopDistance;
   private double squaredSlowdownDistance;
   private double distanceDelta;

   public SteeringForcePursue() {
      this(20.0, 25.0);
   }

   public SteeringForcePursue(double stopDistance, double slowdownDistance) {
      this.setDistances(stopDistance, slowdownDistance);
   }

   public void setDistances(double slowdown, double stop) {
      this.stopDistance = stop;
      this.slowdownDistance = slowdown;
      this.squaredStopDistance = stop * stop;
      this.squaredSlowdownDistance = slowdown * slowdown;
      this.distanceDelta = slowdown - stop;
   }

   @Override
   public boolean compute(@Nonnull Steering output) {
      if (super.compute(output)) {
         output.setTranslation(this.targetPosition);
         Vector3d translation = output.getTranslation();
         translation.sub(this.selfPosition);
         double distanceSquared = translation.lengthSquared();
         if (distanceSquared <= this.squaredStopDistance) {
            output.clear();
            return false;
         } else {
            double distance = Math.sqrt(distanceSquared);
            if (distanceSquared >= this.squaredSlowdownDistance) {
               translation.mul(1.0 / distance);
               output.clearRotation();
               return true;
            } else {
               double scale = Math.pow((distance - this.stopDistance) / this.distanceDelta, this.invFalloff);
               translation.normalize(scale);
               output.clearRotation();
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public double getStopDistance() {
      return this.stopDistance;
   }

   public void setStopDistance(double stopDistance) {
      this.setDistances(this.getSlowdownDistance(), stopDistance);
   }

   public double getSlowdownDistance() {
      return this.slowdownDistance;
   }

   public void setSlowdownDistance(double slowdownDistance) {
      this.setDistances(slowdownDistance, this.getStopDistance());
   }

   public double getFalloff() {
      return this.falloff;
   }

   public void setFalloff(double falloff) {
      this.falloff = falloff;
      this.invFalloff = 1.0 / falloff;
   }
}
