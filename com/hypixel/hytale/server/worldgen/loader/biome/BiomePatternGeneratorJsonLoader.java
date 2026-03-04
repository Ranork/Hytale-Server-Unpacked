package com.hypixel.hytale.server.worldgen.loader.biome;

import com.google.gson.JsonElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.PointGeneratorJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.logic.cell.PointDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.biome.BiomePatternGenerator;
import com.hypixel.hytale.server.worldgen.biome.CustomBiome;
import com.hypixel.hytale.server.worldgen.biome.TileBiome;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BiomePatternGeneratorJsonLoader extends JsonLoader<SeedStringResource, BiomePatternGenerator> {
   protected final IWeightedMap<TileBiome> tileBiomes;
   protected final CustomBiome[] customBiomes;

   public BiomePatternGeneratorJsonLoader(
      @Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, IWeightedMap<TileBiome> tileBiomes, CustomBiome[] customBiomes
   ) {
      super(seed.append(".BiomePatternGenerator"), dataFolder, json);
      this.tileBiomes = tileBiomes;
      this.customBiomes = customBiomes;
   }

   @Nonnull
   public BiomePatternGenerator load() {
      BiomePatternGeneratorJsonLoader.BiomePatternGeneratorSizeModifierProvider sizeModifierProvider = new BiomePatternGeneratorJsonLoader.BiomePatternGeneratorSizeModifierProvider();
      BiomePatternGenerator biomePatternGenerator = new BiomePatternGenerator(this.loadPointGenerator(sizeModifierProvider), this.tileBiomes, this.customBiomes);
      sizeModifierProvider.setGenerator(biomePatternGenerator);
      return biomePatternGenerator;
   }

   @Nullable
   protected IPointGenerator loadPointGenerator(final BiomePatternGeneratorJsonLoader.ISizeModifierProvider sizeModifierProvider) {
      return (new PointGeneratorJsonLoader<SeedStringResource>(this.seed, this.dataFolder, this.get("GridGenerator")) {
         @Nonnull
         @Override
         protected PointDistanceFunction loadPointDistanceFunction() {
            PointDistanceFunction distanceFunction = super.loadPointDistanceFunction();
            return new BiomePatternGeneratorJsonLoader.LoadedPointGeneratorDistanceFunction(sizeModifierProvider, distanceFunction);
         }
      }).load();
   }

   protected static class BiomePatternGeneratorSizeModifierProvider implements BiomePatternGeneratorJsonLoader.ISizeModifierProvider {
      private BiomePatternGenerator generator;

      public BiomePatternGeneratorSizeModifierProvider() {
      }

      public BiomePatternGeneratorSizeModifierProvider(BiomePatternGenerator generator) {
         this.generator = generator;
      }

      public BiomePatternGenerator getGenerator() {
         return this.generator;
      }

      public void setGenerator(BiomePatternGenerator generator) {
         this.generator = generator;
      }

      @Override
      public double get(int seed, int x, int y) {
         return this.generator.getBiomeDirect(seed, x, y).getSizeModifier();
      }
   }

   public interface Constants {
      String KEY_GRID_GENERATOR = "GridGenerator";
   }

   public interface ISizeModifierProvider {
      double get(int var1, int var2, int var3);
   }

   private static class LoadedPointGeneratorDistanceFunction implements PointDistanceFunction {
      private final BiomePatternGeneratorJsonLoader.ISizeModifierProvider sizeModifierProvider;
      private final PointDistanceFunction distanceFunction;

      public LoadedPointGeneratorDistanceFunction(
         BiomePatternGeneratorJsonLoader.ISizeModifierProvider sizeModifierProvider, PointDistanceFunction distanceFunction
      ) {
         this.sizeModifierProvider = sizeModifierProvider;
         this.distanceFunction = distanceFunction;
      }

      @Override
      public double distance2D(double deltaX, double deltaY) {
         return this.distanceFunction.distance2D(deltaX, deltaY);
      }

      @Override
      public double distance3D(double deltaX, double deltaY, double deltaZ) {
         return this.distanceFunction.distance3D(deltaX, deltaY, deltaZ);
      }

      @Override
      public double distance2D(int seed, int cellX, int cellY, double cellCentreX, double cellCentreY, double deltaX, double deltaY) {
         return this.distanceFunction.distance2D(seed, cellX, cellY, cellCentreX, cellCentreY, deltaX, deltaY)
            * this.sizeModifierProvider.get(seed, cellX, cellY);
      }

      @Override
      public double distance3D(
         int seed, int cellX, int cellY, int cellZ, double cellCentreX, double cellCentreY, double cellCentreZ, double deltaX, double deltaY, double deltaZ
      ) {
         return this.distanceFunction.distance3D(seed, cellX, cellY, cellZ, cellCentreX, cellCentreY, cellCentreZ, deltaX, deltaY, deltaZ);
      }
   }
}
