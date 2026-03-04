package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.prefab.WeightedPrefabMapJsonLoader;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CavePrefabEntryJsonLoader extends JsonLoader<SeedStringResource, CavePrefabContainer.CavePrefabEntry> {
   private final ZoneFileContext zoneContext;

   public CavePrefabEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, ZoneFileContext zoneContext) {
      super(seed.append(".CavePrefabEntry"), dataFolder, json);
      this.zoneContext = zoneContext;
   }

   @Nonnull
   public CavePrefabContainer.CavePrefabEntry load() {
      return new CavePrefabContainer.CavePrefabEntry(this.loadPrefabs(), this.loadConfig());
   }

   @Nullable
   protected IWeightedMap<WorldGenPrefabSupplier> loadPrefabs() {
      return new WeightedPrefabMapJsonLoader(this.seed, this.dataFolder, this.json, "Prefab", "Weight").load();
   }

   @Nonnull
   protected CavePrefabContainer.CavePrefabEntry.CavePrefabConfig loadConfig() {
      return new CavePrefabConfigJsonLoader(this.seed, this.dataFolder, this.get("Config"), this.zoneContext).load();
   }

   public interface Constants {
      String KEY_PREFAB = "Prefab";
      String KEY_WEIGHT = "Weight";
      String KEY_CONFIG = "Config";
   }
}
