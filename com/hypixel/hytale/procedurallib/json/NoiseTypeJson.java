package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.NoiseType;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public enum NoiseTypeJson {
   CELL(NoiseType.CELL, CellNoiseJsonLoader.class),
   CONSTANT(NoiseType.CONSTANT, ConstantNoiseJsonLoader.class),
   DISTANCE(NoiseType.DISTANCE, DistanceNoiseJsonLoader.class),
   PERLIN(NoiseType.PERLIN, PerlinNoiseJsonLoader.class),
   SIMPLEX(NoiseType.SIMPLEX, SimplexNoiseJsonLoader.class),
   OLD_SIMPLEX(NoiseType.OLD_SIMPLEX, OldSimplexNoiseJsonLoader.class),
   VALUE(NoiseType.VALUE, ValueNoiseJsonLoader.class),
   MESH(NoiseType.MESH, MeshNoiseJsonLoader.class),
   GRID(NoiseType.GRID, GridNoiseJsonLoader.class),
   BRANCH(NoiseType.BRANCH, BranchNoiseJsonLoader.class),
   POINT(NoiseType.POINT, PointNoiseJsonLoader.class);

   private final NoiseType noiseType;
   @Nonnull
   private final Constructor constructor;

   private <T extends JsonLoader<?, NoiseFunction>> NoiseTypeJson(NoiseType noiseType, @Nonnull Class<T> loaderClass) {
      this.noiseType = noiseType;

      try {
         this.constructor = loaderClass.getConstructor(SeedString.class, Path.class, JsonElement.class);
         this.constructor.setAccessible(true);
      } catch (NoSuchMethodException var6) {
         throw new Error(String.format("Could not find loader constructor for %s. NoiseType: %s", loaderClass.getName(), noiseType), var6);
      }
   }

   @Nonnull
   public <K extends SeedResource> JsonLoader<K, NoiseFunction> newLoader(SeedString<K> seed, Path dataFolder, JsonElement json) {
      try {
         return (JsonLoader<K, NoiseFunction>)this.constructor.newInstance(seed, dataFolder, json);
      } catch (Exception var5) {
         throw new Error(String.format("Failed to execute loader constructor! NoiseType: %s", this.noiseType), var5);
      }
   }

   public interface Constants {
      String ERROR_NO_CONSTRUCTOR = "Could not find loader constructor for %s. NoiseType: %s";
      String ERROR_FAILED_CONSTRUCTOR = "Failed to execute loader constructor! NoiseType: %s";
   }
}
