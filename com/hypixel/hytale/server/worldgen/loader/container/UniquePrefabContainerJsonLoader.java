package com.hypixel.hytale.server.worldgen.loader.container;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.prefab.WeightedPrefabMapJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.prefab.unique.UniquePrefabConfigurationJsonLoader;
import com.hypixel.hytale.server.worldgen.prefab.PrefabCategory;
import com.hypixel.hytale.server.worldgen.prefab.unique.UniquePrefabConfiguration;
import com.hypixel.hytale.server.worldgen.prefab.unique.UniquePrefabGenerator;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import java.nio.file.Path;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniquePrefabContainerJsonLoader extends JsonLoader<SeedStringResource, UniquePrefabContainer> {
   public static final UniquePrefabGenerator[] EMPTY_GENERATORS = new UniquePrefabGenerator[0];
   protected final ZoneFileContext zoneContext;

   public UniquePrefabContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, ZoneFileContext zoneContext) {
      super(seed.append(".UniquePrefabContainer"), dataFolder, json);
      this.zoneContext = zoneContext;
   }

   @Nonnull
   public UniquePrefabContainer load() {
      UniquePrefabGenerator[] generators;
      if (this.json != null && !this.json.isJsonNull()) {
         JsonArray jsonArray = this.get("Entries").getAsJsonArray();
         generators = new UniquePrefabGenerator[jsonArray.size()];

         for (int i = 0; i < jsonArray.size(); i++) {
            generators[i] = new UniquePrefabContainerJsonLoader.UniquePrefabGeneratorJsonLoader(
                  this.seed.append(String.format("-%s", i)), this.dataFolder, jsonArray.get(i), this.zoneContext
               )
               .load();
         }
      } else {
         generators = EMPTY_GENERATORS;
      }

      return new UniquePrefabContainer(this.seed.hashCode(), generators);
   }

   public interface Constants {
      String KEY_ENTRIES = "Entries";
      String KEY_CONFIG = "Config";
      String KEY_PREFAB = "Prefab";
      String KEY_WEIGHTS = "Weights";
      String KEY_ENTRY_NAME = "Name";
      String NO_NAME = "NO_NAME_GIVEN";
      String SEED_INDEX_SUFFIX = "-%s";
   }

   protected static class UniquePrefabGeneratorJsonLoader extends JsonLoader<SeedStringResource, UniquePrefabGenerator> {
      protected final ZoneFileContext zoneContext;

      public UniquePrefabGeneratorJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, ZoneFileContext zoneContext) {
         super(seed.append(".UniquePrefabGenerator"), dataFolder, json);
         this.zoneContext = zoneContext;
      }

      @Nonnull
      public UniquePrefabGenerator load() {
         return new UniquePrefabGenerator(this.loadName(), this.loadCategory(), this.loadPrefabs(), this.loadConfiguration(), this.zoneContext.getId());
      }

      public String loadName() {
         String name = "NO_NAME_GIVEN";
         if (this.has("Name")) {
            name = this.get("Name").getAsString();
         }

         return name;
      }

      protected PrefabCategory loadCategory() {
         String category = this.mustGetString("Category", "");
         if (category.isEmpty()) {
            return PrefabCategory.UNIQUE;
         } else if (!this.zoneContext.getParentContext().getPrefabCategories().contains(category)) {
            LogUtil.getLogger().at(Level.WARNING).log("Could not find prefab category: %s, defaulting to None", category);
            return PrefabCategory.UNIQUE;
         } else {
            return this.zoneContext.getParentContext().getPrefabCategories().get(category);
         }
      }

      @Nonnull
      public UniquePrefabConfiguration loadConfiguration() {
         return new UniquePrefabConfigurationJsonLoader(this.seed, this.dataFolder, this.get("Config"), this.zoneContext).load();
      }

      @Nullable
      public IWeightedMap<WorldGenPrefabSupplier> loadPrefabs() {
         return new WeightedPrefabMapJsonLoader(this.seed, this.dataFolder, this.json, "Prefab", "Weights").load();
      }
   }
}
