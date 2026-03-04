package com.hypixel.hytale.server.worldgen.loader;

import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.Loader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.context.FileContext;
import com.hypixel.hytale.server.worldgen.loader.context.FileLoadingContext;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.zone.ZoneJsonLoader;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import java.nio.file.Path;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ZonesJsonLoader extends Loader<SeedStringResource, Zone[]> {
   protected final FileLoadingContext loadingContext;

   public ZonesJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, FileLoadingContext loadingContext) {
      super(seed.append(".Zones"), dataFolder);
      this.loadingContext = loadingContext;
   }

   @Nonnull
   public Zone[] load() {
      FileContext.Registry<ZoneFileContext> zoneRegistry = this.loadingContext.getZones();
      int index = 0;
      Zone[] zones = new Zone[zoneRegistry.size()];

      for (Entry<String, ZoneFileContext> zoneEntry : zoneRegistry) {
         ZoneFileContext zoneContext = zoneEntry.getValue();

         try {
            JsonObject zoneJson = FileIO.load(zoneContext.getPath().resolve("Zone.json"), JsonLoader.JSON_OBJ_LOADER);
            Zone zone = new ZoneJsonLoader(this.seed, this.dataFolder, zoneJson, zoneContext).load();
            zones[index++] = zone;
         } catch (Throwable var9) {
            throw new Error(String.format("Error while loading zone \"%s\" for world generator from file.", zoneContext.getPath().toString()), var9);
         }
      }

      return zones;
   }

   public interface Constants {
      String PATH_ZONES = "Zones";
      String FILE_ZONE_MAIN_FILE = "Zone.json";
      String ERROR_LOADING_ZONE = "Error while loading zone \"%s\" for world generator from file.";
   }
}
