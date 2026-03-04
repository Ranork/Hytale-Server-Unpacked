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

public class TileBiome extends Biome {
   public static final TileBiome[] EMPTY_ARRAY = new TileBiome[0];
   protected final double weight;
   protected final double sizeModifier;

   public TileBiome(
      int id,
      String name,
      BiomeInterpolation interpolation,
      @Nonnull IHeightThresholdInterpreter heightmapInterpreter,
      CoverContainer coverContainer,
      LayerContainer layerContainer,
      PrefabContainer prefabContainer,
      TintContainer tintContainer,
      EnvironmentContainer environmentContainer,
      WaterContainer waterContainer,
      FadeContainer fadeContainer,
      NoiseProperty heightmapNoise,
      double weight,
      double sizeModifier,
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
      this.weight = weight;
      this.sizeModifier = sizeModifier;
   }

   public double getWeight() {
      return this.weight;
   }

   public double getSizeModifier() {
      return this.sizeModifier;
   }

   @Nonnull
   @Override
   public String toString() {
      return "TileBiome{id="
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
         + ", weight="
         + this.weight
         + ", sizeModifier="
         + this.sizeModifier
         + "}";
   }
}
