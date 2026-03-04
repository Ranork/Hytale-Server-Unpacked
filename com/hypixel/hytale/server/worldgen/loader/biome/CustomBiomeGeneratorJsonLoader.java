package com.hypixel.hytale.server.worldgen.loader.biome;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.ConstantIntCondition;
import com.hypixel.hytale.procedurallib.condition.IDoubleThreshold;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.json.DoubleThresholdJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoisePropertyJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.biome.CustomBiomeGenerator;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import com.hypixel.hytale.server.worldgen.util.condition.HashSetIntCondition;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomBiomeGeneratorJsonLoader extends JsonLoader<SeedStringResource, CustomBiomeGenerator> {
   protected final BiomeFileContext biomeContext;
   protected final Biome[] tileBiomes;

   public CustomBiomeGeneratorJsonLoader(
      @Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, BiomeFileContext biomeContext, Biome[] tileBiomes
   ) {
      super(seed.append(".CustomBiomeGenerator"), dataFolder, json);
      this.biomeContext = biomeContext;
      this.tileBiomes = tileBiomes;
   }

   @Nonnull
   public CustomBiomeGenerator load() {
      return new CustomBiomeGenerator(this.loadNoiseProperty(), this.loadNoiseThreshold(), this.loadBiomeMask(), this.loadPriority());
   }

   @Nullable
   protected NoiseProperty loadNoiseProperty() {
      return new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
   }

   @Nonnull
   protected IDoubleThreshold loadNoiseThreshold() {
      return new DoubleThresholdJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask").getAsJsonObject().get("Threshold")).load();
   }

   @Nonnull
   protected IIntCondition loadBiomeMask() {
      IIntCondition biomeMask = ConstantIntCondition.DEFAULT_TRUE;
      if (this.has("BiomeMask")) {
         Map<String, Biome> nameBiomeMap = this.generateNameBiomeMapping();
         JsonArray biomeMaskArray = this.get("BiomeMask").getAsJsonArray();
         IntSet biomeSet = new IntOpenHashSet(biomeMaskArray.size());

         for (int i = 0; i < biomeMaskArray.size(); i++) {
            String biomeName = biomeMaskArray.get(i).getAsString();
            Biome biome = nameBiomeMap.get(biomeName);
            Objects.requireNonNull(biome, biomeName);
            biomeSet.add(biome.getId());
         }

         biomeMask = new HashSetIntCondition(biomeSet);
      }

      return biomeMask;
   }

   @Nonnull
   protected Map<String, Biome> generateNameBiomeMapping() {
      Map<String, Biome> map = new HashMap<>();

      for (Biome biome : this.tileBiomes) {
         map.put(biome.getName(), biome);
      }

      return map;
   }

   protected int loadPriority() {
      return this.has("Priority") ? this.get("Priority").getAsInt() : 0;
   }

   public interface Constants {
      String KEY_NOISE_MASK = "NoiseMask";
      String KEY_BIOME_MASK = "BiomeMask";
      String KEY_PRIORITY = "Priority";
      String ERROR_BIOME_ERROR_MASK = "Could not find tile biome \"%s\" for biome mask. Typo or disabled tile biome?";
   }
}
