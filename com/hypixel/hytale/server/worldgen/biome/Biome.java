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
import javax.annotation.Nullable;

public abstract class Biome {
   protected final int id;
   protected final String name;
   protected final BiomeInterpolation interpolation;
   @Nonnull
   protected final IHeightThresholdInterpreter heightmapInterpreter;
   protected final CoverContainer coverContainer;
   protected final LayerContainer layerContainer;
   protected final PrefabContainer prefabContainer;
   protected final TintContainer tintContainer;
   protected final EnvironmentContainer environmentContainer;
   protected final WaterContainer waterContainer;
   protected final FadeContainer fadeContainer;
   protected final NoiseProperty heightmapNoise;
   protected final int mapColor;

   public Biome(
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
      int mapColor
   ) {
      this.id = id;
      this.name = name;
      this.interpolation = interpolation;
      this.heightmapInterpreter = heightmapInterpreter;
      this.coverContainer = coverContainer;
      this.layerContainer = layerContainer;
      this.prefabContainer = prefabContainer;
      this.tintContainer = tintContainer;
      this.environmentContainer = environmentContainer;
      this.waterContainer = waterContainer;
      this.fadeContainer = fadeContainer;
      this.heightmapNoise = heightmapNoise;
      this.mapColor = mapColor;
   }

   public String getName() {
      return this.name;
   }

   public BiomeInterpolation getInterpolation() {
      return this.interpolation;
   }

   public IHeightThresholdInterpreter getHeightmapInterpreter() {
      return this.heightmapInterpreter;
   }

   public CoverContainer getCoverContainer() {
      return this.coverContainer;
   }

   public LayerContainer getLayerContainer() {
      return this.layerContainer;
   }

   @Nullable
   public PrefabContainer getPrefabContainer() {
      return this.prefabContainer;
   }

   public TintContainer getTintContainer() {
      return this.tintContainer;
   }

   public EnvironmentContainer getEnvironmentContainer() {
      return this.environmentContainer;
   }

   public WaterContainer getWaterContainer() {
      return this.waterContainer;
   }

   public FadeContainer getFadeContainer() {
      return this.fadeContainer;
   }

   public NoiseProperty getHeightmapNoise() {
      return this.heightmapNoise;
   }

   public int getId() {
      return this.id;
   }

   public int getMapColor() {
      return this.mapColor;
   }

   @Override
   public int hashCode() {
      return this.id;
   }
}
