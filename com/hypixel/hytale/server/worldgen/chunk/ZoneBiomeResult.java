package com.hypixel.hytale.server.worldgen.chunk;

import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.zone.ZoneGeneratorResult;

public class ZoneBiomeResult {
   public ZoneGeneratorResult zoneResult;
   public Biome biome;
   public double heightThresholdContext;
   public double heightmapNoise;

   public ZoneBiomeResult() {
   }

   public ZoneBiomeResult(ZoneGeneratorResult zoneResult, Biome biome, double heightThresholdContext, double heightmapNoise) {
      this.zoneResult = zoneResult;
      this.biome = biome;
      this.heightThresholdContext = heightThresholdContext;
      this.heightmapNoise = heightmapNoise;
   }

   public ZoneGeneratorResult getZoneResult() {
      return this.zoneResult;
   }

   public void setZoneResult(ZoneGeneratorResult zoneResult) {
      this.zoneResult = zoneResult;
   }

   public Biome getBiome() {
      return this.biome;
   }

   public void setBiome(Biome biome) {
      this.biome = biome;
   }

   public double getHeightThresholdContext() {
      return this.heightThresholdContext;
   }

   public void setHeightThresholdContext(double heightThresholdContext) {
      this.heightThresholdContext = heightThresholdContext;
   }

   public double getHeightmapNoise() {
      return this.heightmapNoise;
   }

   public void setHeightmapNoise(double heightmapNoise) {
      this.heightmapNoise = heightmapNoise;
   }
}
