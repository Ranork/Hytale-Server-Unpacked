package com.hypixel.hytale.builtin.beds.sleep.resources;

import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.packets.world.SleepClock;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import java.time.Instant;
import javax.annotation.Nonnull;

public final class WorldSlumber implements WorldSleep {
   @Nonnull
   private final Instant startInstant;
   @Nonnull
   private final Instant targetInstant;
   @Nonnull
   private final InstantData startInstantData;
   @Nonnull
   private final InstantData targetInstantData;
   private final float irlDurationSeconds;
   private float progressSeconds;

   public WorldSlumber(@Nonnull Instant startInstant, @Nonnull Instant targetInstant, float irlDurationSeconds) {
      this.startInstant = startInstant;
      this.targetInstant = targetInstant;
      this.startInstantData = WorldTimeResource.instantToInstantData(startInstant);
      this.targetInstantData = WorldTimeResource.instantToInstantData(targetInstant);
      this.irlDurationSeconds = irlDurationSeconds;
   }

   @Nonnull
   public Instant getStartInstant() {
      return this.startInstant;
   }

   @Nonnull
   public Instant getTargetInstant() {
      return this.targetInstant;
   }

   @Nonnull
   public InstantData getStartInstantData() {
      return this.startInstantData;
   }

   @Nonnull
   public InstantData getTargetInstantData() {
      return this.targetInstantData;
   }

   public float getProgressSeconds() {
      return this.progressSeconds;
   }

   public void incrementProgressSeconds(float seconds) {
      this.progressSeconds += seconds;
      this.progressSeconds = Math.min(this.progressSeconds, this.irlDurationSeconds);
   }

   public float getIrlDurationSeconds() {
      return this.irlDurationSeconds;
   }

   @Nonnull
   public SleepClock createSleepClock() {
      float progress = this.progressSeconds / this.irlDurationSeconds;
      return new SleepClock(this.startInstantData, this.targetInstantData, progress, this.irlDurationSeconds);
   }
}
