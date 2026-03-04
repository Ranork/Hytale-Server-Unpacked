package com.hypixel.hytale.server.core.modules.time;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.packets.world.UpdateTime;
import com.hypixel.hytale.protocol.packets.world.UpdateTimeSettings;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.ecs.MoonPhaseChangeEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;

public class WorldTimeResource implements Resource<EntityStore> {
   public static final long NANOS_PER_DAY = ChronoUnit.DAYS.getDuration().toNanos();
   public static final int SECONDS_PER_DAY = (int)ChronoUnit.DAYS.getDuration().getSeconds();
   public static final int HOURS_PER_DAY = (int)ChronoUnit.DAYS.getDuration().toHours();
   public static final int DAYS_PER_YEAR = (int)ChronoUnit.YEARS.getDuration().toDays();
   public static final Instant ZERO_YEAR = Instant.parse("0001-01-01T00:00:00.00Z");
   public static final Instant MAX_TIME = Instant.ofEpochSecond(31553789759L, 99999999L);
   public static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;
   public static final float SUN_HEIGHT = 2.0F;
   public static final boolean USE_SHADOW_MAPPING_SAFE_ANGLE = true;
   public static final float DAYTIME_PORTION_PERCENTAGE = 0.6F;
   public static final int DAYTIME_SECONDS = (int)(SECONDS_PER_DAY * 0.6F);
   public static final int NIGHTTIME_SECONDS = (int)(SECONDS_PER_DAY * 0.39999998F);
   public static final int SUNRISE_SECONDS = NIGHTTIME_SECONDS / 2;
   public static final float SHADOW_MAPPING_SAFE_ANGLE_LERP = 0.35F;
   @Nonnull
   private final UpdateTime currentTimePacket = new UpdateTime();
   private Instant gameTime;
   private LocalDateTime _gameTimeLocalDateTime;
   private int currentHour;
   private double sunlightFactor;
   private double scaledTime;
   private int moonPhase;
   @Nonnull
   private final UpdateTimeSettings currentSettings = new UpdateTimeSettings();
   @Nonnull
   private final UpdateTimeSettings tempSettings = new UpdateTimeSettings();

   @Nonnull
   public static ResourceType<EntityStore, WorldTimeResource> getResourceType() {
      return TimeModule.get().getWorldTimeResourceType();
   }

   public static double getSecondsPerTick(World world) {
      int daytimeDurationSeconds = world.getDaytimeDurationSeconds();
      int nighttimeDurationSeconds = world.getNighttimeDurationSeconds();
      int totalDurationSeconds = daytimeDurationSeconds + nighttimeDurationSeconds;
      return (double)SECONDS_PER_DAY / totalDurationSeconds;
   }

   public void tick(float dt, @Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      if (!updateTimeSettingsPacket(this.tempSettings, world).equals(this.currentSettings)) {
         boolean wasTimePausedChanged = this.currentSettings.timePaused != this.tempSettings.timePaused;
         updateTimeSettingsPacket(this.currentSettings, world);
         PlayerUtil.broadcastPacketToPlayers(store, this.currentSettings);
         if (wasTimePausedChanged) {
            this.broadcastTimePacket(store);
         }
      }

      if (!world.getWorldConfig().isGameTimePaused()) {
         int secondsOfDay = this._gameTimeLocalDateTime.get(ChronoField.SECOND_OF_DAY);
         int daytimeDurationSeconds = world.getDaytimeDurationSeconds();
         int nighttimeDurationSeconds = world.getNighttimeDurationSeconds();
         int totalDurationSeconds = daytimeDurationSeconds + nighttimeDurationSeconds;
         double daytimeRate = (double)DAYTIME_SECONDS / daytimeDurationSeconds;
         double nighttimeRate = (double)NIGHTTIME_SECONDS / nighttimeDurationSeconds;
         double x0;
         if (secondsOfDay >= SUNRISE_SECONDS && secondsOfDay < SUNRISE_SECONDS + DAYTIME_SECONDS) {
            x0 = (secondsOfDay - SUNRISE_SECONDS) / daytimeRate;
         } else {
            x0 = daytimeDurationSeconds + MathUtil.floorMod(secondsOfDay - SUNRISE_SECONDS - DAYTIME_SECONDS, SECONDS_PER_DAY) / nighttimeRate;
         }

         double x1 = x0 + dt;
         long whole = (long)Math.floor(x1 / totalDurationSeconds) - (long)Math.floor(x0 / totalDurationSeconds);
         double m0 = MathUtil.floorMod(x0, totalDurationSeconds);
         double m1 = MathUtil.floorMod(x1, totalDurationSeconds);
         double f0 = m0 <= daytimeDurationSeconds ? daytimeRate * m0 : DAYTIME_SECONDS + nighttimeRate * (m0 - daytimeDurationSeconds);
         double f1 = m1 <= daytimeDurationSeconds ? daytimeRate * m1 : DAYTIME_SECONDS + nighttimeRate * (m1 - daytimeDurationSeconds);
         double advance = whole * SECONDS_PER_DAY + (f1 - f0);
         Instant temp = this.gameTime.plusNanos((long)(advance * 1.0E9));
         if (temp.isBefore(ZERO_YEAR)) {
            temp = MAX_TIME.minusSeconds(ZERO_YEAR.getEpochSecond() - this.gameTime.getEpochSecond()).minusNanos(ZERO_YEAR.getNano() - this.gameTime.getNano());
         }

         if (temp.isAfter(MAX_TIME)) {
            temp = ZERO_YEAR.plusSeconds(MAX_TIME.getEpochSecond() - this.gameTime.getEpochSecond()).plusNanos(MAX_TIME.getNano() - this.gameTime.getNano());
         }

         this.setGameTime0(temp);
         this.updateMoonPhase(world, store);
      }
   }

   public int getMoonPhase() {
      return this.moonPhase;
   }

   public void setMoonPhase(int moonPhase, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (moonPhase != this.moonPhase) {
         MoonPhaseChangeEvent event = new MoonPhaseChangeEvent(moonPhase);
         componentAccessor.invoke(event);
      }

      this.moonPhase = moonPhase;
   }

   public void updateMoonPhase(@Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      WorldConfig worldGameplayConfig = world.getGameplayConfig().getWorldConfig();
      int totalMoonPhases = worldGameplayConfig.getTotalMoonPhases();
      double dayProgress = (double)this.currentHour / HOURS_PER_DAY;
      int currentDay = this._gameTimeLocalDateTime.getDayOfYear();
      int weekDay = (currentDay - 1) % totalMoonPhases;
      if (dayProgress < 0.5) {
         if (weekDay == 0) {
            this.setMoonPhase(totalMoonPhases - 1, componentAccessor);
         } else {
            this.setMoonPhase(weekDay - 1, componentAccessor);
         }
      } else {
         this.setMoonPhase(weekDay, componentAccessor);
      }
   }

   public boolean isMoonPhaseWithinRange(@Nonnull World world, int minMoonPhase, int maxMoonPhase) {
      WorldConfig worldGameplayConfig = world.getGameplayConfig().getWorldConfig();
      int totalMoonPhases = worldGameplayConfig.getTotalMoonPhases();
      return minMoonPhase <= maxMoonPhase
         ? MathUtil.within(this.moonPhase, minMoonPhase, maxMoonPhase)
         : MathUtil.within(this.moonPhase, minMoonPhase, totalMoonPhases) || MathUtil.within(this.moonPhase, 0.0, maxMoonPhase);
   }

   public void setGameTime0(@Nonnull Instant gameTime) {
      this.gameTime = gameTime;
      this._gameTimeLocalDateTime = LocalDateTime.ofInstant(gameTime, ZONE_OFFSET);
      this.updateTimePacket(this.currentTimePacket);
      this.currentHour = this._gameTimeLocalDateTime.getHour();
      int dayProgress = this._gameTimeLocalDateTime.get(ChronoField.SECOND_OF_DAY);
      float dayDuration = 0.6F * SECONDS_PER_DAY;
      float nightDuration = SECONDS_PER_DAY - dayDuration;
      float halfNight = nightDuration * 0.5F;
      this.updateSunlightFactor(dayProgress, halfNight);
      this.updateScaledTime(dayProgress, dayDuration, halfNight);
   }

   private void updateSunlightFactor(int dayProgress, float halfNight) {
      float dawnRelativeProgress = (dayProgress - halfNight) / SECONDS_PER_DAY;
      this.sunlightFactor = MathUtil.clamp(TrigMathUtil.sin((float) (Math.PI * 2) * dawnRelativeProgress) + 0.2, 0.0, 1.0);
   }

   private void updateScaledTime(float dayProgress, float dayDuration, float halfNight) {
      if (dayProgress <= halfNight) {
         this.scaledTime = MathUtil.lerp(0.0F, 0.25F, dayProgress / halfNight);
      } else {
         dayProgress -= halfNight;
         if (dayProgress <= dayDuration) {
            this.scaledTime = MathUtil.lerp(0.25F, 0.75F, dayProgress / dayDuration);
         } else {
            dayProgress -= dayDuration;
            this.scaledTime = MathUtil.lerp(0.75F, 1.0F, dayProgress / halfNight);
         }
      }
   }

   public Instant getGameTime() {
      return this.gameTime;
   }

   public LocalDateTime getGameDateTime() {
      return this._gameTimeLocalDateTime;
   }

   public double getSunlightFactor() {
      return this.sunlightFactor;
   }

   public void setGameTime(@Nonnull Instant gameTime, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      this.setGameTime0(gameTime);
      this.updateMoonPhase(world, store);
      this.broadcastTimePacket(store);
   }

   public void setDayTime(double dayTime, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (!(dayTime < 0.0) && !(dayTime > 1.0)) {
         Instant oldGameTime = this.gameTime;
         Instant dayStart = oldGameTime.truncatedTo(ChronoUnit.DAYS);
         Instant newGameTime = dayStart.plusNanos((long)(dayTime * NANOS_PER_DAY));
         if (newGameTime.isBefore(oldGameTime)) {
            this.setGameTime(newGameTime.plus(1L, ChronoUnit.DAYS), world, store);
         } else {
            this.setGameTime(newGameTime, world, store);
         }
      } else {
         throw new IllegalArgumentException("Day time must be between 0 and 1");
      }
   }

   public void broadcastTimePacket(@Nonnull Store<EntityStore> store) {
      PlayerUtil.broadcastPacketToPlayers(store, this.currentTimePacket);
   }

   public void sendTimePackets(@Nonnull PlayerRef playerRef) {
      playerRef.getPacketHandler().write(this.currentSettings);
      playerRef.getPacketHandler().write(this.currentTimePacket);
   }

   public boolean isDayTimeWithinRange(double minTime, double maxTime) {
      double dayProgress = (double)this._gameTimeLocalDateTime.getHour() / HOURS_PER_DAY;
      return !(minTime > maxTime)
         ? MathUtil.within(dayProgress, minTime, maxTime)
         : MathUtil.within(dayProgress, minTime, 1.0) || MathUtil.within(dayProgress, 0.0, maxTime);
   }

   public void updateTimePacket(@Nonnull UpdateTime currentTimePacket) {
      if (currentTimePacket.gameTime == null) {
         currentTimePacket.gameTime = new InstantData();
      }

      currentTimePacket.gameTime.seconds = this.gameTime.getEpochSecond();
      currentTimePacket.gameTime.nanos = this.gameTime.getNano();
   }

   @Nonnull
   public static UpdateTimeSettings updateTimeSettingsPacket(@Nonnull UpdateTimeSettings settings, @Nonnull World world) {
      WorldConfig worldGameplayConfig = world.getGameplayConfig().getWorldConfig();
      settings.daytimeDurationSeconds = world.getDaytimeDurationSeconds();
      settings.nighttimeDurationSeconds = world.getNighttimeDurationSeconds();
      settings.totalMoonPhases = (byte)worldGameplayConfig.getTotalMoonPhases();
      settings.timePaused = world.getWorldConfig().isGameTimePaused();
      return settings;
   }

   public boolean isScaledDayTimeWithinRange(double minTime, double maxTime) {
      return !(minTime > maxTime)
         ? MathUtil.within(this.scaledTime, minTime, maxTime)
         : MathUtil.within(this.scaledTime, minTime, 1.0) || MathUtil.within(this.scaledTime, 0.0, maxTime);
   }

   public boolean isYearWithinRange(double minTime, double maxTime) {
      return false;
   }

   public int getCurrentHour() {
      return this.currentHour;
   }

   public float getDayProgress() {
      return (float)this._gameTimeLocalDateTime.get(ChronoField.SECOND_OF_DAY) / SECONDS_PER_DAY;
   }

   @Nonnull
   public Vector3f getSunDirection() {
      float dayTime = this.getDayProgress() * HOURS_PER_DAY;
      float daylightDuration = 0.6F * HOURS_PER_DAY;
      float nightDuration = HOURS_PER_DAY - daylightDuration;
      float halfNightDuration = nightDuration * 0.5F;
      float sunAngle;
      if (dayTime < halfNightDuration) {
         float inverseAllNightDay = 1.0F / (nightDuration * 2.0F);
         sunAngle = MathUtil.wrapAngle((dayTime * inverseAllNightDay - halfNightDuration * inverseAllNightDay) * (float) (Math.PI * 2));
      } else if (dayTime > HOURS_PER_DAY - halfNightDuration) {
         float inverseAllNightDay = 1.0F / (nightDuration * 2.0F);
         sunAngle = MathUtil.wrapAngle((dayTime * inverseAllNightDay - (HOURS_PER_DAY + halfNightDuration) * inverseAllNightDay) * (float) (Math.PI * 2));
      } else {
         float halfDaylightDuration = daylightDuration * 0.5F;
         float inverseAllDaylightDay = 1.0F / (daylightDuration * 2.0F);
         sunAngle = MathUtil.wrapAngle(
            (dayTime * inverseAllDaylightDay - (HOURS_PER_DAY * 0.5F - halfDaylightDuration) * inverseAllDaylightDay) * (float) (Math.PI * 2)
         );
      }

      Vector3f sunPosition = new Vector3f(TrigMathUtil.cos(sunAngle), TrigMathUtil.sin(sunAngle) * 2.0F, TrigMathUtil.sin(sunAngle));
      sunPosition.normalize();
      float tweakedSunHeight = sunPosition.y + 0.2F;
      if (tweakedSunHeight > 0.0F) {
         sunPosition.scale(-1.0F);
      }

      sunPosition.x = MathUtil.lerp(sunPosition.x, Vector3f.DOWN.x, 0.35F);
      sunPosition.y = MathUtil.lerp(sunPosition.y, Vector3f.DOWN.y, 0.35F);
      sunPosition.z = MathUtil.lerp(sunPosition.z, Vector3f.DOWN.z, 0.35F);
      return sunPosition;
   }

   @Nonnull
   public static InstantData instantToInstantData(@Nonnull Instant instant) {
      return new InstantData(instant.getEpochSecond(), instant.getNano());
   }

   @Nonnull
   public static Instant instantDataToInstant(@Nonnull InstantData instantData) {
      return Instant.ofEpochSecond(instantData.seconds, instantData.nanos);
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      WorldTimeResource worldTimeComponent = new WorldTimeResource();
      worldTimeComponent.gameTime = this.gameTime;
      worldTimeComponent._gameTimeLocalDateTime = this._gameTimeLocalDateTime;
      worldTimeComponent.currentHour = this.currentHour;
      worldTimeComponent.sunlightFactor = this.sunlightFactor;
      worldTimeComponent.scaledTime = this.scaledTime;
      return worldTimeComponent;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WorldTimeResource{, gameTime=" + this.gameTime + "}";
   }
}
