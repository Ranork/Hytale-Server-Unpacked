package com.hypixel.hytale.server.worldgen.container;

import com.hypixel.hytale.server.worldgen.zone.ZoneGeneratorResult;
import javax.annotation.Nonnull;

public class FadeContainer {
   public static final double NO_FADE_HEIGHTMAP = Double.NEGATIVE_INFINITY;
   protected final double maskFadeStart;
   protected final double maskFadeLength;
   protected final double maskFadeSum;
   protected final double terrainFadeStart;
   protected final double terrainFadeLength;
   protected final double terrainFadeSum;
   protected final double fadeHeightmap;

   public FadeContainer(double maskFadeStart, double maskFadeLength, double terrainFadeStart, double terrainFadeLength, double fadeHeightmap) {
      this.maskFadeStart = maskFadeStart;
      this.maskFadeLength = maskFadeLength;
      this.maskFadeSum = maskFadeStart + maskFadeLength;
      this.terrainFadeStart = terrainFadeStart;
      this.terrainFadeLength = terrainFadeLength;
      this.terrainFadeSum = terrainFadeStart + terrainFadeLength;
      this.fadeHeightmap = fadeHeightmap;
   }

   public double getMaskFadeStart() {
      return this.maskFadeStart;
   }

   public double getMaskFadeLength() {
      return this.maskFadeLength;
   }

   public double getMaskFadeSum() {
      return this.maskFadeSum;
   }

   public double getHeightFadeStart() {
      return this.terrainFadeStart;
   }

   public double getHeightFadeLength() {
      return this.terrainFadeLength;
   }

   public double getHeightFadeSum() {
      return this.terrainFadeSum;
   }

   public double getFadeHeightmap() {
      return this.fadeHeightmap;
   }

   public double getMaskFactor(@Nonnull ZoneGeneratorResult result) {
      return this.getFactor(result, this.maskFadeStart, this.maskFadeLength);
   }

   public double getTerrainFactor(@Nonnull ZoneGeneratorResult result) {
      return this.getFactor(result, this.terrainFadeStart, this.terrainFadeLength);
   }

   public double getFactor(@Nonnull ZoneGeneratorResult result, double distanceFromBorder, double gradientWidth) {
      return 1.0 - limit((result.getBorderDistance() - distanceFromBorder) / gradientWidth);
   }

   public boolean shouldFade() {
      return this.fadeHeightmap != Double.NEGATIVE_INFINITY;
   }

   @Nonnull
   @Override
   public String toString() {
      return "FadeContainer{maskFadeStart="
         + this.maskFadeStart
         + ", maskFadeLength="
         + this.maskFadeLength
         + ", maskFadeSum="
         + this.maskFadeSum
         + ", terrainFadeStart="
         + this.terrainFadeStart
         + ", terrainFadeLength="
         + this.terrainFadeLength
         + ", terrainFadeSum="
         + this.terrainFadeSum
         + ", fadeHeightmap="
         + this.fadeHeightmap
         + "}";
   }

   private static double limit(double d) {
      if (d < 0.0) {
         return 0.0;
      } else {
         return d > 1.0 ? 1.0 : d;
      }
   }
}
