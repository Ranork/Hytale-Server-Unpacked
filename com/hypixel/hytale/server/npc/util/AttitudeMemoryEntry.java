package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.common.thread.ticking.Tickable;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;

public class AttitudeMemoryEntry implements Tickable {
   private final Attitude attitudeOverride;
   private final double initialDuration;
   private double remainingDuration;

   public AttitudeMemoryEntry(Attitude attitudeOverride, double initialDuration) {
      this.attitudeOverride = attitudeOverride;
      this.initialDuration = initialDuration;
      this.remainingDuration = initialDuration;
   }

   @Override
   public void tick(float dt) {
      this.remainingDuration = Math.max(this.remainingDuration - dt, 0.0);
   }

   public double getRemainingDuration() {
      return this.remainingDuration;
   }

   public double getInitialDuration() {
      return this.initialDuration;
   }

   public Attitude getAttitudeOverride() {
      return this.attitudeOverride;
   }

   public boolean isExpired() {
      return this.remainingDuration <= 0.0;
   }
}
