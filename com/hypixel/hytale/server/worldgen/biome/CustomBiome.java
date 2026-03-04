package com.hypixel.hytale.server.worldgen.biome;

import com.hypixel.hytale.procedurallib.condition.IHeightThresholdInterpreter;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.server.worldgen.container.CoverContainer;
import com.hypixel.hytale.server.worldgen.container.EnvironmentContainer;
import com.hypixel.hytale.server.worldgen.container.FadeContainer;
import com.hypixel.hytale.server.worldgen.container.LayerContainer;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.container.TintContainer;
import com.hypixel.hytale.server.worldgen.container.WaterContainer;
import javax.annotation.Nonnull;

public class CustomBiome extends Biome {
   protected final CustomBiomeGenerator customBiomeGenerator;

   public CustomBiome(
      int id,
      String name,
      BiomeInterpolation interpolation,
      CustomBiomeGenerator customBiomeGenerator,
      @Nonnull IHeightThresholdInterpreter heightmapInterpreter,
      CoverContainer coverContainer,
      LayerContainer layerContainer,
      PrefabContainer prefabContainer,
      TintContainer tintContainer,
      EnvironmentContainer environmentContainer,
      WaterContainer waterContainer,
      FadeContainer fadeContainer,
      NoiseProperty heightmapNoise,
      int mapColor
   ) {
      super(
         id,
         name,
         interpolation,
         heightmapInterpreter,
         coverContainer,
         layerContainer,
         prefabContainer,
         tintContainer,
         environmentContainer,
         waterContainer,
         fadeContainer,
         heightmapNoise,
         mapColor
      );
      this.customBiomeGenerator = customBiomeGenerator;
   }

   public CustomBiomeGenerator getCustomBiomeGenerator() {
      return this.customBiomeGenerator;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CustomBiome{id="
         + this.id
         + ", name='"
         + this.name
         + "', interpolation="
         + this.interpolation
         + ", heightmapInterpreter="
         + this.heightmapInterpreter
         + ", coverContainer="
         + this.coverContainer
         + ", layerContainer="
         + this.layerContainer
         + ", prefabContainer="
         + this.prefabContainer
         + ", tintContainer="
         + this.tintContainer
         + ", environmentContainer="
         + this.environmentContainer
         + ", waterContainer="
         + this.waterContainer
         + ", fadeContainer="
         + this.fadeContainer
         + ", heightmapNoise="
         + this.heightmapNoise
         + ", mapColor="
         + this.mapColor
         + ", customBiomeGenerator="
         + this.customBiomeGenerator
         + "}";
   }
}
