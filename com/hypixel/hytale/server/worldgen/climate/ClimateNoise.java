package com.hypixel.hytale.server.worldgen.climate;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import javax.annotation.Nonnull;

public class ClimateNoise {
   @Nonnull
   public final ClimateNoise.Grid grid;
   @Nonnull
   public final NoiseProperty continent;
   @Nonnull
   public final NoiseProperty temperature;
   @Nonnull
   public final NoiseProperty intensity;
   @Nonnull
   public final ClimateNoise.Thresholds thresholds;

   public ClimateNoise(
      @Nonnull ClimateNoise.Grid grid,
      @Nonnull NoiseProperty continent,
      @Nonnull NoiseProperty temperature,
      @Nonnull NoiseProperty intensity,
      @Nonnull ClimateNoise.Thresholds thresholds
   ) {
      this.grid = grid;
      this.temperature = temperature;
      this.intensity = intensity;
      this.continent = continent;
      this.thresholds = thresholds;
   }

   public int generate(int seed, double x, double y, @Nonnull ClimateNoise.Buffer buffer, @Nonnull ClimateGraph climate) {
      this.grid.eval(seed, x, y, buffer.pos);
      double c = this.continent.get(seed, x, y);
      double t = this.temperature.get(seed, buffer.pos.x, buffer.pos.y);
      double i = this.intensity.get(seed, buffer.pos.x, buffer.pos.y);
      int index = climate.indexOf(t, i);
      buffer.continent = c;
      buffer.temperature = t;
      buffer.intensity = i;
      buffer.fade = climate.getFade(index);
      int id = climate.getId(index);
      int flags = getContinentFlags(c, this.thresholds);
      return id | flags;
   }

   private static int getContinentFlags(double value, @Nonnull ClimateNoise.Thresholds thresholds) {
      boolean isLand = value <= thresholds.landShallowOceanOuter;
      boolean isIsland = value >= thresholds.islandShallowOceanOuter;
      boolean isOcean = value > thresholds.land && value < thresholds.island;
      boolean isShore = isLand && value > thresholds.landShoreInner || isIsland && value < thresholds.islandShoreInner;
      int flags = 0;
      if (isOcean) {
         flags |= Integer.MIN_VALUE;
      }

      if (isShore) {
         flags |= 1073741824;
      }

      if (isIsland) {
         flags |= 536870912;
      }

      return flags;
   }

   public static class Buffer {
      public double continent = 0.0;
      public double temperature = 0.0;
      public double intensity = 0.0;
      public double fade = 0.0;
      public final ResultBuffer.ResultBuffer2d pos = new ResultBuffer.ResultBuffer2d();
   }

   public static class Grid {
      public final int seedOffset;
      public final double scale;
      @Nonnull
      public final PointEvaluator evaluator;
      @Nonnull
      public final CellDistanceFunction grid;
      public final transient double invScale;

      public Grid(int seedOffset, double scale, @Nonnull CellDistanceFunction grid, @Nonnull PointEvaluator evaluator) {
         this.seedOffset = seedOffset;
         this.scale = scale;
         this.evaluator = evaluator;
         this.grid = grid;
         this.invScale = 1.0 / scale;
      }

      public void eval(int seed, double x, double y, ResultBuffer.ResultBuffer2d buffer) {
         x *= this.scale;
         y *= this.scale;
         buffer.distance = Double.MAX_VALUE;
         buffer.distance2 = Double.MAX_VALUE;
         this.grid.nearest2D(seed + this.seedOffset, x, y, MathUtil.floor(x), MathUtil.floor(y), buffer, this.evaluator);
         buffer.x = buffer.x * this.invScale;
         buffer.y = buffer.y * this.invScale;
      }
   }

   public static class Thresholds {
      public static final double LAND_DEFAULT = 0.5;
      public static final double ISLAND_DEFAULT = 0.8;
      public static final double BEACH_SIZE_DEFAULT = 0.05;
      public static final double SHALLOW_OCEAN_SIZE_DEFAULT = 0.15;
      public final double land;
      public final double island;
      public final double beachSize;
      public final double shallowOceanSize;
      public final transient double landShoreInner;
      public final transient double islandShoreInner;
      public final transient double landShallowOceanOuter;
      public final transient double islandShallowOceanOuter;

      public Thresholds(double land, double island, double beach, double shore) {
         this.land = land;
         this.island = island;
         this.beachSize = beach;
         this.shallowOceanSize = shore;
         this.landShoreInner = land - beach;
         this.islandShoreInner = island + beach;
         this.landShallowOceanOuter = land + shore;
         this.islandShallowOceanOuter = island - shore * 0.5;
      }
   }
}
