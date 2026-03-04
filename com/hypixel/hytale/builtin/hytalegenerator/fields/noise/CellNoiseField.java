package com.hypixel.hytale.builtin.hytalegenerator.fields.noise;

import com.hypixel.hytale.builtin.hytalegenerator.fields.FastNoiseLite;
import javax.annotation.Nonnull;

public class CellNoiseField extends NoiseField {
   private FastNoiseLite cellNoise;
   private int seed;
   private boolean doDomainWarp;
   private double scaleX;
   private double scaleY;
   private double scaleZ;

   public CellNoiseField(
      int seed,
      double scaleX,
      double scaleY,
      double scaleZ,
      double jitter,
      int octaves,
      @Nonnull FastNoiseLite.CellularReturnType cellType,
      @Nonnull FastNoiseLite.DomainWarpType domainWarpType,
      double warpAmount,
      double warpScale
   ) {
      if (octaves >= 1 && !(warpAmount <= 0.0) && !(warpScale <= 0.0)) {
         this.seed = seed;
         this.scaleX = scaleX;
         this.scaleY = scaleY;
         this.scaleZ = scaleZ;
         this.cellNoise = new FastNoiseLite();
         float frequency = 1.0F;
         float warpFrequency = 1.0F / (float)warpScale;
         this.doDomainWarp = true;
         jitter *= 2.0;
         this.cellNoise.setNoiseType(FastNoiseLite.NoiseType.Cellular);
         this.cellNoise.setCellularReturnType(cellType);
         this.cellNoise.setFractalOctaves(octaves);
         this.cellNoise.setFractalType(FastNoiseLite.FractalType.FBm);
         this.cellNoise.setCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.Euclidean);
         this.cellNoise.setSeed(seed);
         this.cellNoise.setFrequency(frequency);
         this.cellNoise.setDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
         this.cellNoise.setDomainWarpAmp((float)warpAmount);
         this.cellNoise.setDomainWarpFreq(warpFrequency);
         this.cellNoise.setCellularJitter((float)jitter);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public CellNoiseField(int seed, double scaleX, double scaleY, double scaleZ, double jitter, int octaves, @Nonnull FastNoiseLite.CellularReturnType cellType) {
      if (octaves < 1) {
         throw new IllegalArgumentException();
      } else {
         this.seed = seed;
         this.scaleX = scaleX;
         this.scaleY = scaleY;
         this.scaleZ = scaleZ;
         this.cellNoise = new FastNoiseLite();
         float frequency = 1.0F;
         this.doDomainWarp = false;
         jitter *= 2.0;
         this.cellNoise.setNoiseType(FastNoiseLite.NoiseType.Cellular);
         this.cellNoise.setCellularReturnType(cellType);
         this.cellNoise.setFractalOctaves(octaves);
         this.cellNoise.setFractalType(FastNoiseLite.FractalType.FBm);
         this.cellNoise.setCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.Euclidean);
         this.cellNoise.setSeed(seed);
         this.cellNoise.setFrequency(frequency);
         this.cellNoise.setCellularJitter((float)jitter);
      }
   }

   @Override
   public double valueAt(double x, double y, double z, double w) {
      x /= this.scaleX;
      y /= this.scaleY;
      z /= this.scaleZ;
      if (this.doDomainWarp) {
         FastNoiseLite.Vector3 point = new FastNoiseLite.Vector3((float)x, (float)y, (float)z);
         this.cellNoise.DomainWarp(point);
         return this.cellNoise.getNoise(point.x, point.y, point.z);
      } else {
         return this.cellNoise.getNoise(x, y, z);
      }
   }

   @Override
   public double valueAt(double x, double y, double z) {
      x /= this.scaleX;
      y /= this.scaleY;
      z /= this.scaleZ;
      if (this.doDomainWarp) {
         FastNoiseLite.Vector3 point = new FastNoiseLite.Vector3((float)x, (float)y, (float)z);
         this.cellNoise.DomainWarp(point);
         return this.cellNoise.getNoise(point.x, point.y, point.z);
      } else {
         return this.cellNoise.getNoise(x, y, z);
      }
   }

   @Override
   public double valueAt(double x, double z) {
      x /= this.scaleX;
      z /= this.scaleZ;
      if (this.doDomainWarp) {
         FastNoiseLite.Vector2 point = new FastNoiseLite.Vector2((float)x, (float)z);
         this.cellNoise.DomainWarp(point);
         return this.cellNoise.getNoise(point.x, point.y);
      } else {
         return this.cellNoise.getNoise(x, z);
      }
   }

   @Override
   public double valueAt(double x) {
      x /= this.scaleX;
      return this.cellNoise.getNoise((float)x, 0.0);
   }
}
