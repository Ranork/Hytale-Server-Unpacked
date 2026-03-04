package com.hypixel.hytale.server.npc.movement.steeringforces;

import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.npc.movement.Steering;
import javax.annotation.Nonnull;

public class SteeringForceEvade extends SteeringForceWithTarget {
   private double slowdownDistance;
   private double stopDistance;
   private double falloff = 3.0;
   private double squaredSlowdownDistance;
   private double squaredStopDistance;
   private double distanceDelta;
   private float directionHint;
   private boolean adhereToDirectionHint;

   public SteeringForceEvade() {
      this(20.0, 25.0);
   }

   public SteeringForceEvade(double slowdownDistance, double stopDistance) {
      this.setDistances(slowdownDistance, stopDistance);
   }

   public void setDistances(double min, double max) {
      this.slowdownDistance = min;
      this.stopDistance = max;
      this.squaredSlowdownDistance = min * min;
      this.squaredStopDistance = max * max;
      this.distanceDelta = max - min;
   }

   public void setDirectionHint(float heading) {
      this.directionHint = heading;
   }

   public void setAdhereToDirectionHint(boolean adhereToDirectionHint) {
      this.adhereToDirectionHint = adhereToDirectionHint;
   }

   @Override
   public boolean compute(@Nonnull Steering output) {
      if (super.compute(output)) {
         output.setTranslation(this.selfPosition).getTranslation().subtract(this.targetPosition);
         double distanceSquared = output.getTranslation().squaredLength();
         if (distanceSquared >= this.squaredStopDistance) {
            output.clear();
            return false;
         } else {
            output.clearRotation();
            if (distanceSquared < 1.0E-6) {
               output.setTranslation(PhysicsMath.headingX(this.directionHint), 0.0, PhysicsMath.headingZ(this.directionHint));
               return true;
            } else {
               if (this.adhereToDirectionHint) {
                  output.setTranslation(PhysicsMath.headingX(this.directionHint), 0.0, PhysicsMath.headingZ(this.directionHint));
               }

               if (!(distanceSquared < this.squaredSlowdownDistance) && this.distanceDelta != 0.0) {
                  double scale = Math.pow((this.stopDistance - Math.sqrt(distanceSquared)) / this.distanceDelta, 1.0 / this.falloff);
                  output.getTranslation().setLength(scale);
                  return true;
               } else {
                  output.getTranslation().normalize();
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public double getSlowdownDistance() {
      return this.slowdownDistance;
   }

   public void setSlowdownDistance(double slowdownDistance) {
      this.setDistances(slowdownDistance, this.getStopDistance());
   }

   public double getStopDistance() {
      return this.stopDistance;
   }

   public void setStopDistance(double stopDistance) {
      this.setDistances(this.getSlowdownDistance(), stopDistance);
      this.stopDistance = stopDistance;
   }

   public double getFalloff() {
      return this.falloff;
   }

   public void setFalloff(double falloff) {
      this.falloff = falloff;
   }
}
