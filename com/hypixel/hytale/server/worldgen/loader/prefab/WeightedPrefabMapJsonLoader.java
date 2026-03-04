package com.hypixel.hytale.server.worldgen.loader.prefab;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabLoader;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class WeightedPrefabMapJsonLoader extends JsonLoader<SeedStringResource, IWeightedMap<WorldGenPrefabSupplier>> {
   protected final String prefabsKey;
   protected final String weightsKey;

   public WeightedPrefabMapJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, String prefabsKey, String weightsKey) {
      super(seed.append(".WeightedPrefabMap"), dataFolder, json);
      this.prefabsKey = prefabsKey;
      this.weightsKey = weightsKey;
   }

   public IWeightedMap<WorldGenPrefabSupplier> load() {
      WorldGenPrefabLoader prefabLoader = this.seed.get().getLoader();
      WeightedMap.Builder<WorldGenPrefabSupplier> builder = WeightedMap.builder(WorldGenPrefabSupplier.EMPTY_ARRAY);
      if (!this.has(this.prefabsKey)) {
         throw new IllegalArgumentException(this.prefabsKey);
      } else {
         JsonElement prefabElement = this.get(this.prefabsKey);
         if (prefabElement.isJsonArray()) {
            JsonArray prefabArray = prefabElement.getAsJsonArray();
            JsonArray weightArray = this.has(this.weightsKey) ? this.get(this.weightsKey).getAsJsonArray() : null;
            if (weightArray != null && prefabArray.size() != weightArray.size()) {
               throw new IllegalArgumentException("Weight array size is different from prefab name array.");
            }

            for (int i = 0; i < prefabArray.size(); i++) {
               JsonElement prefabArrayElement = prefabArray.get(i);
               String prefabString = prefabArrayElement.getAsString();
               WorldGenPrefabSupplier[] suppliers = prefabLoader.get(prefabString);
               double weight = weightArray != null ? weightArray.get(i).getAsDouble() / suppliers.length : 1.0;

               for (WorldGenPrefabSupplier supplier : suppliers) {
                  builder.put(supplier, weight);
               }
            }
         } else {
            String prefabString = prefabElement.getAsString();
            WorldGenPrefabSupplier[] suppliers = prefabLoader.get(prefabString);

            for (WorldGenPrefabSupplier supplier : suppliers) {
               builder.put(supplier, 1.0);
            }
         }

         if (builder.size() <= 0) {
            throw new IllegalArgumentException("Prefabs are defined but could not find a valid entry!");
         } else {
            return builder.build();
         }
      }
   }

   public interface Constants {
      String ERROR_ENTRY_NO_PREFAB = "Could not find prefab names. Keyword: %s";
      String ERROR_ENTRY_WEIGHT_SIZE = "Weight array size is different from prefab name array.";
      String ERROR_NO_PREFABS = "Prefabs are defined but could not find a valid entry!";
   }
}
