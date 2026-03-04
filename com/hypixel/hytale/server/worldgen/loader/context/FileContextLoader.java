package com.hypixel.hytale.server.worldgen.loader.context;

import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.procedurallib.file.AssetPath;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.server.worldgen.prefab.PrefabCategory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class FileContextLoader {
   private static final Comparator<AssetPath> ZONES_ORDER = AssetPath.COMPARATOR;
   private static final Comparator<AssetPath> BIOME_ORDER = Comparator.comparing(BiomeFileContext::getBiomeType).thenComparing(AssetPath.COMPARATOR);
   private static final UnaryOperator<AssetPath> DISABLED_FILE = FileContextLoader::getDisabledFilePath;
   private static final Predicate<AssetPath> ZONE_FILE_MATCHER = FileContextLoader::isValidZoneFile;
   private static final Predicate<AssetPath> BIOME_FILE_MATCHER = FileContextLoader::isValidBiomeFile;
   private final Path dataFolder;
   private final Set<String> zoneRequirement;

   public FileContextLoader(Path dataFolder, Set<String> zoneRequirement) {
      this.dataFolder = dataFolder;
      this.zoneRequirement = zoneRequirement;
   }

   @Nonnull
   public FileLoadingContext load() {
      FileLoadingContext context = new FileLoadingContext(this.dataFolder);
      Path zonesFolder = this.dataFolder.resolve("Zones");

      try {
         List<AssetPath> files = FileIO.list(zonesFolder, ZONE_FILE_MATCHER, DISABLED_FILE);
         files.sort(ZONES_ORDER);

         for (AssetPath path : files) {
            String zoneName = path.getFileName();
            if (this.zoneRequirement.contains(zoneName)) {
               ZoneFileContext zone = loadZoneContext(zoneName, path.filepath(), context);
               context.getZones().register(zoneName, zone);
            }
         }
      } catch (IOException var9) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var9)).log("Failed to load zones");
      }

      try {
         validateZones(context, this.zoneRequirement);
      } catch (Error var8) {
         throw new Error("Failed to validate zones!", var8);
      }

      loadPrefabCategories(this.dataFolder, context);
      return context;
   }

   protected static void loadPrefabCategories(@Nonnull Path folder, @Nonnull FileLoadingContext context) {
      AssetPath path = FileIO.resolve(folder.resolve("PrefabCategories.json"));
      if (FileIO.exists(path)) {
         try {
            JsonObject json = FileIO.load(path, JsonLoader.JSON_OBJ_LOADER);
            PrefabCategory.parse(json, context.getPrefabCategories()::register);
         } catch (IOException var4) {
            throw new Error("Failed to open Categories.json", var4);
         }
      }
   }

   @Nonnull
   protected static ZoneFileContext loadZoneContext(String name, @Nonnull Path folder, @Nonnull FileLoadingContext context) {
      try {
         ZoneFileContext zone = context.createZone(name, folder);
         List<AssetPath> files = FileIO.list(folder, BIOME_FILE_MATCHER, DISABLED_FILE);
         files.sort(BIOME_ORDER);

         for (AssetPath path : files) {
            BiomeFileContext.Type type = BiomeFileContext.getBiomeType(path);
            String biomeName = parseName(path, type);
            BiomeFileContext biome = zone.createBiome(biomeName, path.filepath(), type);
            zone.getBiomes(type).register(biomeName, biome);
         }

         return zone;
      } catch (IOException var10) {
         throw new Error(String.format("Failed to list files in: %s", folder), var10);
      }
   }

   @Nonnull
   protected static AssetPath getDisabledFilePath(@Nonnull AssetPath path) {
      String filename = path.getFileName();
      return filename.startsWith("!") ? path.rename(filename.substring(1)) : path;
   }

   protected static boolean isValidZoneFile(@Nonnull AssetPath path) {
      return Files.isDirectory(path.filepath()) && Files.exists(path.filepath().resolve("Zone.json"));
   }

   protected static boolean isValidBiomeFile(@Nonnull AssetPath path) {
      if (Files.isDirectory(path.filepath())) {
         return false;
      } else {
         String filename = path.getFileName();

         for (BiomeFileContext.Type type : BiomeFileContext.Type.values()) {
            if (filename.endsWith(type.getSuffix()) && filename.startsWith(type.getPrefix())) {
               return true;
            }
         }

         return false;
      }
   }

   protected static void validateZones(@Nonnull FileLoadingContext context, @Nonnull Set<String> zoneRequirement) throws Error {
      for (String key : zoneRequirement) {
         context.getZones().get(key);
      }
   }

   @Nonnull
   private static String parseName(@Nonnull AssetPath path, @Nonnull BiomeFileContext.Type type) {
      String filename = path.getFileName();
      int start = type.getPrefix().length();
      int end = filename.length() - type.getSuffix().length();
      return filename.substring(start, end);
   }

   public interface Constants {
      int ZONE_SEARCH_DEPTH = 1;
      int BIOME_SEARCH_DEPTH = 1;
      String IDENTIFIER_DISABLE = "!";
      String INFO_ZONE_IS_DISABLED = "Zone \"%s\" is disabled. Remove \"!\" from folder name to enable it.";
      String ERROR_LIST_FILES = "Failed to list files in: %s";
      String ERROR_ZONE_VALIDATION = "Failed to validate zones!";
   }
}
