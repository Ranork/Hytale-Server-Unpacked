package com.hypixel.hytale.server.worldgen.loader.cave;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.loader.context.CaveFileContext;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class CaveTypesJsonLoader extends JsonLoader<SeedStringResource, CaveType[]> {
   protected final Path caveFolder;
   protected final ZoneFileContext zoneContext;
   protected final CaveFileContext caveContext;

   public CaveTypesJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, Path caveFolder, ZoneFileContext zoneContext) {
      super(seed, dataFolder, json);
      this.caveFolder = caveFolder;
      this.zoneContext = zoneContext;
      this.caveContext = new CaveFileContext("Caves", zoneContext);
   }

   @Nonnull
   public CaveType[] load() {
      if (this.json != null && this.json.isJsonArray()) {
         JsonArray typesArray = this.json.getAsJsonArray();

         CaveType[] var7;
         try (ListPool.Resource<CaveType> entries = CaveType.ENTRY_POOL.acquire(typesArray.size())) {
            for (int i = 0; i < typesArray.size(); i++) {
               JsonElement entry = this.getOrLoad(typesArray.get(i));
               if (!entry.isJsonObject()) {
                  throw error("Expected CaveType entry to be a JsonObject at index: %d", i);
               }

               entries.add(this.loadCaveType(entry.getAsJsonObject()));
            }

            ModifyEvent.dispatch(
               ModifyEvents.CaveTypes.class,
               new ModifyEvents.CaveTypes(this.caveContext, entries, content -> this.loadCaveType(this.getOrLoad(content).getAsJsonObject()))
            );
            var7 = entries.toArray();
         }

         return var7;
      } else {
         throw new IllegalArgumentException("CaveTypes must be a JSON array.");
      }
   }

   @Nonnull
   protected CaveType loadCaveType(JsonObject json) {
      JsonElement name = json.get("Name");
      if (name == null) {
         throw error("CaveType is missing the 'Name' property");
      } else if (name.isJsonPrimitive() && name.getAsJsonPrimitive().isString()) {
         return new CaveTypeJsonLoader(
               this.seed.append(String.format("-%s", name)), this.dataFolder, json, this.caveFolder, name.getAsString(), this.zoneContext
            )
            .load();
      } else {
         throw error("CaveType 'Name' property is not a string");
      }
   }

   public interface Constants {
      String KEY_NAME = "Name";
      String SEED_CAVE_TYPE_SUFFIX = "-%s";
      String ERROR_NOT_AN_ARRAY = "CaveTypes must be a JSON array.";
   }
}
