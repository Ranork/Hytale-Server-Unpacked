package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.common.benchmark.ContinuousValueRecorder;
import com.hypixel.hytale.common.benchmark.DiscreteValueRecorder;
import com.hypixel.hytale.common.benchmark.TimeRecorder;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.math.util.MathUtil;
import java.util.Formatter;
import javax.annotation.Nonnull;

public class SensorSupportBenchmark {
   public static final char DEFAULT_COLUMN_SEPARATOR = '|';
   public static final String DEFAULT_COLUMN_FORMAT_HEADER = "|%-6.6s";
   public static final String DEFAULT_COLUMN_FORMAT_VALUE = "|%6.6s";
   public static final String[] DEFAULT_COLUMNS_UPDATE = new String[]{
      "KIND", "COUNT", "G-AVG", "G-MIN", "G-MAX", "L-AVG", "L-MAX", "SD-MAX", "UD-MAX", "AD-MAX"
   };
   public static final String[] DEFAULT_COLUMNS_LOS = new String[]{
      "L-CNT", "L-AVG", "L-MAX", "L-HIT%", "T-CNT", "T-AVG", "T-MIN", "T-MAX", "I-CNT", "I-AVG", "I-MAX", "I-HIT%", "F-CNT", "F-AVG", "F-MAX", "F-HIT%"
   };
   @Nonnull
   protected TimeRecorder playerGetTime = new TimeRecorder();
   @Nonnull
   protected DiscreteValueRecorder playerDistance = new DiscreteValueRecorder();
   @Nonnull
   protected DiscreteValueRecorder playerDistanceSorted = new DiscreteValueRecorder();
   @Nonnull
   protected DiscreteValueRecorder playerDistanceAvoidance = new DiscreteValueRecorder();
   @Nonnull
   protected DiscreteValueRecorder playerCount = new DiscreteValueRecorder();
   @Nonnull
   protected TimeRecorder entityGetTime = new TimeRecorder();
   @Nonnull
   protected DiscreteValueRecorder entityDistance = new DiscreteValueRecorder();
   @Nonnull
   protected DiscreteValueRecorder entityDistanceSorted = new DiscreteValueRecorder();
   @Nonnull
   protected DiscreteValueRecorder entityDistanceAvoidance = new DiscreteValueRecorder();
   @Nonnull
   protected DiscreteValueRecorder entityCount = new DiscreteValueRecorder();
   @Nonnull
   protected DiscreteValueRecorder losTest = new DiscreteValueRecorder();
   @Nonnull
   protected ContinuousValueRecorder losCacheHit = new ContinuousValueRecorder();
   @Nonnull
   protected DiscreteValueRecorder inverseLosTest = new DiscreteValueRecorder();
   @Nonnull
   protected ContinuousValueRecorder inverseLosCacheHit = new ContinuousValueRecorder();
   @Nonnull
   protected DiscreteValueRecorder friendlyBlockingTest = new DiscreteValueRecorder();
   @Nonnull
   protected ContinuousValueRecorder friendlyBlockingCacheHit = new ContinuousValueRecorder();
   @Nonnull
   protected TimeRecorder losTestTime = new TimeRecorder();
   protected long losTestTick;
   protected long losCacheHitTick;
   protected long inverseLosTestTick;
   protected long inverseLosCacheHitTick;
   protected long friendlyBlockingTestTick;
   protected long friendlyBlockingCacheHitTick;

   public void collectPlayerList(long getNanos, double maxPlayerDistanceSorted, double maxPlayerDistance, double maxPlayerDistanceAvoidance, int numPlayers) {
      this.playerGetTime.recordNanos(getNanos);
      this.playerDistance.record(MathUtil.fastCeil(maxPlayerDistance));
      this.playerDistanceSorted.record(MathUtil.fastCeil(maxPlayerDistanceSorted));
      this.playerDistanceAvoidance.record(MathUtil.fastCeil(maxPlayerDistanceAvoidance));
      this.playerCount.record(numPlayers);
   }

   public void collectEntityList(long getNanos, double maxEntityDistanceSorted, double maxEntityDistance, double maxEntityDistanceAvoidance, int numEntities) {
      this.entityGetTime.recordNanos(getNanos);
      this.entityDistance.record(MathUtil.fastCeil(maxEntityDistance));
      this.entityDistanceSorted.record(MathUtil.fastCeil(maxEntityDistanceSorted));
      this.entityDistanceAvoidance.record(MathUtil.fastCeil(maxEntityDistanceAvoidance));
      this.entityCount.record(numEntities);
   }

   public void collectLosTest(boolean cacheHit, long time) {
      this.losTestTick++;
      if (cacheHit) {
         this.losCacheHitTick++;
      } else {
         this.losTestTime.recordNanos(time);
      }
   }

   public void collectInverseLosTest(boolean cacheHit) {
      this.inverseLosTestTick++;
      if (cacheHit) {
         this.inverseLosCacheHitTick++;
      }
   }

   public void collectFriendlyBlockingTest(boolean cacheHit) {
      this.friendlyBlockingTestTick++;
      if (cacheHit) {
         this.friendlyBlockingCacheHitTick++;
      }
   }

   public void tickDone() {
      this.losTest.record(this.losTestTick);
      if (this.losTestTick > 0L) {
         this.losCacheHit.record((double)this.losCacheHitTick / this.losTestTick);
      }

      this.losTestTick = 0L;
      this.losCacheHitTick = 0L;
      this.inverseLosTest.record(this.inverseLosTestTick);
      if (this.inverseLosTestTick > 0L) {
         this.inverseLosCacheHit.record((double)this.inverseLosCacheHitTick / this.inverseLosTestTick);
      }

      this.inverseLosTestTick = 0L;
      this.inverseLosCacheHitTick = 0L;
      this.friendlyBlockingTest.record(this.friendlyBlockingTestTick);
      if (this.friendlyBlockingTestTick > 0L) {
         this.friendlyBlockingCacheHit.record((double)this.friendlyBlockingCacheHitTick / this.friendlyBlockingTestTick);
      }

      this.friendlyBlockingTestTick = 0L;
      this.friendlyBlockingCacheHitTick = 0L;
   }

   public void formatHeaderUpdateTimes(@Nonnull Formatter formatter) {
      FormatUtil.formatArray(formatter, "|%-6.6s", DEFAULT_COLUMNS_UPDATE);
   }

   public void formatValuesUpdateTimePlayer(@Nonnull Formatter formatter) {
      this.formatValuesUpdateTime(
         formatter, "Player", this.playerGetTime, this.playerCount, this.playerDistanceSorted, this.playerDistance, this.playerDistanceAvoidance
      );
   }

   public void formatValuesUpdateTimeEntity(@Nonnull Formatter formatter) {
      this.formatValuesUpdateTime(
         formatter, "Entity", this.entityGetTime, this.entityCount, this.entityDistanceSorted, this.entityDistance, this.entityDistanceAvoidance
      );
   }

   public void formatValuesUpdateTime(
      @Nonnull Formatter formatter,
      String kind,
      @Nonnull TimeRecorder getTime,
      @Nonnull DiscreteValueRecorder count,
      @Nonnull DiscreteValueRecorder distanceSorted,
      @Nonnull DiscreteValueRecorder distance,
      @Nonnull DiscreteValueRecorder distanceAvoidance
   ) {
      long distanceMaxValue = distance.getMaxValue();
      long distanceMinValue = distance.getMinValue();
      long distanceMaxValueSorted = distanceSorted.getMaxValue();
      long distanceMinValueSorted = distanceSorted.getMinValue();
      long distanceMaxValueAvoidance = distanceAvoidance.getMaxValue();
      long distanceMinValueAvoidance = distanceAvoidance.getMinValue();
      formatter.format("|%-6.6s", kind);
      FormatUtil.formatArgs(
         formatter,
         "|%6.6s",
         getTime.getCount(),
         TimeRecorder.formatTime(getTime.getAverage()),
         TimeRecorder.formatTime(getTime.getMinValue()),
         TimeRecorder.formatTime(getTime.getMaxValue()),
         count.getAverage(),
         count.getMaxValue(),
         distanceMaxValueSorted == distanceMinValueSorted ? distanceMaxValueSorted : distanceMinValueSorted + "-" + distanceMaxValueSorted,
         distanceMaxValue == distanceMinValue ? distanceMaxValue : distanceMinValue + "-" + distanceMaxValue,
         distanceMaxValueAvoidance == distanceMinValueAvoidance ? distanceMaxValueAvoidance : distanceMinValueAvoidance + "-" + distanceMaxValueAvoidance
      );
   }

   public boolean haveUpdateTimes() {
      return this.playerGetTime.getCount() > 0L || this.entityGetTime.getCount() > 0L;
   }

   public void formatHeaderLoS(@Nonnull Formatter formatter) {
      FormatUtil.formatArray(formatter, "|%-6.6s", DEFAULT_COLUMNS_LOS);
   }

   public boolean formatValuesLoS(@Nonnull Formatter formatter) {
      if (this.losTest.getMaxValue() == 0L && this.inverseLosTest.getMaxValue() == 0L && this.friendlyBlockingTest.getMaxValue() == 0L) {
         return false;
      } else {
         FormatUtil.formatArgs(
            formatter,
            "|%6.6s",
            this.losTest.getCount(),
            this.losTest.getAverage(),
            this.losTest.getMaxValue(),
            (int)(this.losCacheHit.getAverage() * 100.0),
            this.losTestTime.getCount(),
            TimeRecorder.formatTime(this.losTestTime.getAverage()),
            TimeRecorder.formatTime(this.losTestTime.getMinValue()),
            TimeRecorder.formatTime(this.losTestTime.getMaxValue()),
            this.inverseLosTest.getCount(),
            this.inverseLosTest.getAverage(),
            this.inverseLosTest.getMaxValue(),
            (int)(this.inverseLosCacheHit.getAverage() * 100.0),
            this.friendlyBlockingTest.getCount(),
            this.friendlyBlockingTest.getAverage(),
            this.friendlyBlockingTest.getMaxValue(),
            (int)(this.friendlyBlockingCacheHit.getAverage() * 100.0)
         );
         return true;
      }
   }
}
