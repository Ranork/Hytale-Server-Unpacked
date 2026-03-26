package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.context.CaveFileContext;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class CavePrefabContainerJsonLoader extends JsonLoader<SeedStringResource, CavePrefabContainer> {
   protected final CaveFileContext context;

   public CavePrefabContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, CaveFileContext context) {
      super(seed.append(".CavePrefabContainer"), dataFolder, json);
      this.context = context;
   }

   @Nonnull
   public CavePrefabContainer load() {
      return new CavePrefabContainer(this.loadEntries());
   }

   @Nonnull
   protected CavePrefabContainer.CavePrefabEntry[] loadEntries() {
      CavePrefabContainer.CavePrefabEntry[] var7;
      try (ListPool.Resource<CavePrefabContainer.CavePrefabEntry> entries = CavePrefabContainer.ENTRY_POOL.acquire()) {
         if (this.json != null) {
            JsonArray prefabArray = this.mustGetArray("Entries", null);

            for (int i = 0; i < prefabArray.size(); i++) {
               entries.add(
                  new CavePrefabEntryJsonLoader(this.seed.append(String.format("-%s", i)), this.dataFolder, prefabArray.get(i), this.context.getParentContext())
                     .load()
               );
            }
         }

         ModifyEvent.SeedGenerator<SeedStringResource> seed = new ModifyEvent.SeedGenerator<>(this.seed);
         ModifyEvent.dispatch(
            ModifyEvents.CavePrefabs.class,
            new ModifyEvents.CavePrefabs(
               this.context,
               entries,
               content -> new CavePrefabEntryJsonLoader(seed.next(), this.dataFolder, this.getOrLoad(content), this.context.getParentContext()).load()
            )
         );
         var7 = entries.toArray();
      }

      return var7;
   }

   public interface Constants {
      String KEY_ENTRIES = "Entries";
      String SEED_ENTRY_SUFFIX = "-%s";
      String ERROR_NO_ENTRIES = "Could not find entries in prefab container. Keyword: Entries";
   }
}
