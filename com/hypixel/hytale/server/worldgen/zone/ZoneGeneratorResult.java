package com.hypixel.hytale.server.worldgen.zone;

public class ZoneGeneratorResult {
   protected Zone zone;
   protected double borderDistance;

   public ZoneGeneratorResult() {
   }

   public ZoneGeneratorResult(Zone zone, double borderDistance) {
      this.zone = zone;
      this.borderDistance = borderDistance;
   }

   public void setZone(Zone zone) {
      this.zone = zone;
   }

   public void setBorderDistance(double borderDistance) {
      this.borderDistance = borderDistance;
   }

   public Zone getZone() {
      return this.zone;
   }

   public double getBorderDistance() {
      return this.borderDistance;
   }
}
