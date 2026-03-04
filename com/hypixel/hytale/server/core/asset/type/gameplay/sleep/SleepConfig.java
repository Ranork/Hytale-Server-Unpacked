package com.hypixel.hytale.server.core.asset.type.gameplay.sleep;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.annotation.Nullable;

public class SleepConfig {
   public static final BuilderCodec<SleepConfig> CODEC = BuilderCodec.builder(SleepConfig.class, SleepConfig::new)
      .append(new KeyedCodec<>("WakeUpHour", Codec.FLOAT), (sleepConfig, i) -> sleepConfig.wakeUpHour = i, o -> o.wakeUpHour)
      .documentation("The in-game hour at which players naturally wake up from sleep.")
      .add()
      .<double[]>append(
         new KeyedCodec<>("AllowedSleepHoursRange", Codec.DOUBLE_ARRAY),
         (sleepConfig, i) -> sleepConfig.allowedSleepHoursRange = i,
         o -> o.allowedSleepHoursRange
      )
      .addValidator(Validators.doubleArraySize(2))
      .documentation("The in-game hours during which players can sleep to skip to the WakeUpHour. If missing, there is no restriction.")
      .add()
      .append(new KeyedCodec<>("Sounds", SleepSoundsConfig.CODEC), (sleepConfig, i) -> sleepConfig.sounds = i, sleepConfig -> sleepConfig.sounds)
      .add()
      .build();
   public static final SleepConfig DEFAULT = new SleepConfig();
   private float wakeUpHour = 5.5F;
   private double[] allowedSleepHoursRange;
   private SleepSoundsConfig sounds = new SleepSoundsConfig();

   public float getWakeUpHour() {
      return this.wakeUpHour;
   }

   @Nullable
   public double[] getAllowedSleepHoursRange() {
      return this.allowedSleepHoursRange;
   }

   public SleepSoundsConfig getSounds() {
      return this.sounds;
   }

   @Nullable
   public LocalTime getSleepStartTime() {
      if (this.allowedSleepHoursRange == null) {
         return null;
      } else {
         double sleepStartHour = this.allowedSleepHoursRange[0];
         int hour = (int)sleepStartHour;
         int minute = (int)((sleepStartHour - hour) * 60.0);
         return LocalTime.of(hour, minute);
      }
   }

   public boolean isWithinSleepHoursRange(LocalDateTime gameTime) {
      if (this.allowedSleepHoursRange == null) {
         return true;
      } else {
         float hour = getFractionalHourOfDay(gameTime);
         double min = this.allowedSleepHoursRange[0];
         double max = this.allowedSleepHoursRange[1];
         return (hour - min + 24.0) % 24.0 <= (max - min + 24.0) % 24.0;
      }
   }

   public Duration computeDurationUntilSleep(LocalDateTime now) {
      if (this.allowedSleepHoursRange == null) {
         return Duration.ZERO;
      } else {
         float currentHour = getFractionalHourOfDay(now);
         double sleepStartHour = this.allowedSleepHoursRange[0];
         double hoursUntilSleep = (sleepStartHour - currentHour + 24.0) % 24.0;
         long seconds = (long)(hoursUntilSleep * 3600.0);
         return Duration.ofSeconds(seconds);
      }
   }

   private static float getFractionalHourOfDay(LocalDateTime dateTime) {
      return dateTime.getHour() + dateTime.getMinute() / 60.0F + dateTime.getSecond() / 3600.0F;
   }
}
