package com.hypixel.hytale.server.worldgen.loader.container;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import com.hypixel.hytale.server.worldgen.loader.context.FileLoadingContext;
import com.hypixel.hytale.server.worldgen.loader.prefab.PrefabPatternGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.prefab.WeightedPrefabMapJsonLoader;
import com.hypixel.hytale.server.worldgen.prefab.PrefabPatternGenerator;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class PrefabContainerJsonLoader extends JsonLoader<SeedStringResource, PrefabContainer> {
   @Nonnull
   protected final BiomeFileContext biomeContext;
   @Nonnull
   protected final FileLoadingContext fileContext;

   public PrefabContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, @Nonnull BiomeFileContext biomeContext) {
      super(seed.append(".PrefabContainer"), dataFolder, json);
      this.biomeContext = biomeContext;
      this.fileContext = biomeContext.getParentContext().getParentContext();
   }

   @Nonnull
   public PrefabContainer load() {
      return new PrefabContainer(this.loadEntries());
   }

   @Nonnull
   protected PrefabContainer.PrefabContainerEntry[] loadEntries() {
      JsonArray prefabArray = this.mustGetArray("Entries", EMPTY_ARRAY);

      PrefabContainer.PrefabContainerEntry[] e;
      try (ListPool.Resource<PrefabContainer.PrefabContainerEntry> entries = PrefabContainer.ENTRY_POOL.acquire()) {
         for (int i = 0; i < prefabArray.size(); i++) {
            try {
               entries.add(
                  new PrefabContainerJsonLoader.PrefabContainerEntryJsonLoader(this.seed.append("-" + i), this.dataFolder, prefabArray.get(i), this.fileContext)
                     .load()
               );
            } catch (Throwable var6) {
               throw new Error(String.format("Failed to load prefab container entry #%s.", i), var6);
            }
         }

         ModifyEvent.SeedGenerator<SeedStringResource> seed = new ModifyEvent.SeedGenerator<>(this.seed);
         ModifyEvent.dispatch(
            ModifyEvents.BiomePrefabs.class,
            new ModifyEvents.BiomePrefabs(
               this.biomeContext,
               entries,
               content -> new PrefabContainerJsonLoader.PrefabContainerEntryJsonLoader(seed.next(), this.dataFolder, this.getOrLoad(content), this.fileContext)
                  .load()
            )
         );
         e = entries.toArray();
      }

      return e;
   }

   public interface Constants {
      String KEY_ENTRIES = "Entries";
      String KEY_ENTRY_PREFAB = "Prefab";
      String KEY_ENTRY_WEIGHT = "Weight";
      String KEY_ENTRY_PATTERN = "Pattern";
      String KEY_ENVIRONMENT = "Environment";
      String ERROR_FAIL_ENTRY = "Failed to load prefab container entry #%s.";
      String ERROR_LOADING_ENVIRONMENT = "Error while looking up environment \"%s\"!";
      String ERROR_ENTRY_NO_PATTERN = "Could not find prefab pattern. Keyword: Pattern";
   }

   public static class PrefabContainerEntryJsonLoader extends JsonLoader<SeedStringResource, PrefabContainer.PrefabContainerEntry> {
      private final FileLoadingContext context;

      public PrefabContainerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, FileLoadingContext context) {
         super(seed.append(".PrefabContainerEntry"), dataFolder, json);
         this.context = context;
      }

      @Nonnull
      public PrefabContainer.PrefabContainerEntry load() {
         IWeightedMap<WorldGenPrefabSupplier> prefabs = new WeightedPrefabMapJsonLoader(this.seed, this.dataFolder, this.json, "Prefab", "Weight").load();
         if (!this.has("Pattern")) {
            throw new IllegalArgumentException("Could not find prefab pattern. Keyword: Pattern");
         } else {
            PrefabPatternGenerator prefabPatternGenerator = new PrefabPatternGeneratorJsonLoader(this.seed, this.dataFolder, this.get("Pattern"), this.context)
               .load();
            return new PrefabContainer.PrefabContainerEntry(prefabs, prefabPatternGenerator, this.loadEnvironment());
         }
      }

      protected int loadEnvironment() {
         int environment = Integer.MIN_VALUE;
         if (this.has("Environment")) {
            String environmentId = this.get("Environment").getAsString();
            environment = Environment.getAssetMap().getIndex(environmentId);
            if (environment == Integer.MIN_VALUE) {
               throw new Error(String.format("Error while looking up environment \"%s\"!", environmentId));
            }
         }

         return environment;
      }
   }
}
