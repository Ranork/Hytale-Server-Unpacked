package com.hypixel.hytale.builtin.path.waypoint;

public class RelativeWaypointDefinition {
   protected final float rotation;
   protected final double distance;

   public RelativeWaypointDefinition(float rotation, double distance) {
      this.rotation = rotation;
      this.distance = distance;
   }

   public float getRotation() {
      return this.rotation;
   }

   public double getDistance() {
      return this.distance;
   }
}
