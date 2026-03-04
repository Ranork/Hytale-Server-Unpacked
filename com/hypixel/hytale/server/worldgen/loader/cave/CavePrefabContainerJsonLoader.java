package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class CavePrefabContainerJsonLoader extends JsonLoader<SeedStringResource, CavePrefabContainer> {
   private final ZoneFileContext zoneContext;

   public CavePrefabContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, ZoneFileContext zoneContext) {
      super(seed.append(".CavePrefabContainer"), dataFolder, json);
      this.zoneContext = zoneContext;
   }

   @Nonnull
   public CavePrefabContainer load() {
      return new CavePrefabContainer(this.loadEntries());
   }

   @Nonnull
   protected CavePrefabContainer.CavePrefabEntry[] loadEntries() {
      if (this.json == null || this.json.isJsonNull()) {
         return new CavePrefabContainer.CavePrefabEntry[0];
      } else if (!this.has("Entries")) {
         throw new IllegalArgumentException("Could not find entries in prefab container. Keyword: Entries");
      } else {
         JsonArray array = this.get("Entries").getAsJsonArray();
         CavePrefabContainer.CavePrefabEntry[] entries = new CavePrefabContainer.CavePrefabEntry[array.size()];

         for (int i = 0; i < entries.length; i++) {
            entries[i] = new CavePrefabEntryJsonLoader(this.seed.append(String.format("-%s", i)), this.dataFolder, array.get(i), this.zoneContext).load();
         }

         return entries;
      }
   }

   public interface Constants {
      String KEY_ENTRIES = "Entries";
      String SEED_ENTRY_SUFFIX = "-%s";
      String ERROR_NO_ENTRIES = "Could not find entries in prefab container. Keyword: Entries";
   }
}
