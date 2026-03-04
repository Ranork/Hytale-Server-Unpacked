package com.hypixel.hytale.server.worldgen.loader.context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ZoneFileContext extends FileContext<FileLoadingContext> {
   private final FileContext.Registry<BiomeFileContext> tileBiomes = new FileContext.Registry<>("TileBiome");
   private final FileContext.Registry<BiomeFileContext> customBiomes = new FileContext.Registry<>("CustomBiome");

   public ZoneFileContext(int id, String name, Path filepath, FileLoadingContext context) {
      super(id, name, filepath, context);
   }

   @Nonnull
   public FileContext.Registry<BiomeFileContext> getTileBiomes() {
      return this.tileBiomes;
   }

   @Nonnull
   public FileContext.Registry<BiomeFileContext> getCustomBiomes() {
      return this.customBiomes;
   }

   @Nonnull
   public FileContext.Registry<BiomeFileContext> getBiomes(@Nonnull BiomeFileContext.Type type) {
      return switch (type) {
         case Tile -> this.getTileBiomes();
         case Custom -> this.getCustomBiomes();
      };
   }

   @Nonnull
   public ZoneFileContext matchContext(@Nullable JsonElement json, String key) {
      if (json != null && json.isJsonObject()) {
         JsonElement element = json.getAsJsonObject().get(key);
         if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (!object.has("File")) {
               return this;
            } else {
               String filePath = object.get("File").getAsString();
               return this.matchContext(filePath);
            }
         } else {
            return this;
         }
      } else {
         return this;
      }
   }

   @Nonnull
   public ZoneFileContext matchContext(@Nonnull String filePath) {
      if (!filePath.startsWith("Zones.")) {
         return this;
      } else {
         int nameStart = "Zones.".length();
         int nameEnd = filePath.indexOf(46, nameStart);
         if (nameEnd < nameStart) {
            return this;
         } else if (filePath.regionMatches(nameStart, this.getName(), 0, nameEnd - nameStart)) {
            return this;
         } else {
            String zoneName = filePath.substring(nameStart, nameEnd);
            FileContext.Registry<ZoneFileContext> zoneRegistry = this.getParentContext().getZones();
            return !zoneRegistry.contains(zoneName) ? this : zoneRegistry.get(zoneName);
         }
      }
   }

   @Nonnull
   protected BiomeFileContext createBiome(String name, Path path, BiomeFileContext.Type type) {
      return this.createBiome(this.getParentContext().nextBiomeId(), name, path, type);
   }

   @Nonnull
   protected BiomeFileContext createBiome(int id, String name, Path path, BiomeFileContext.Type type) {
      return new BiomeFileContext(this.getParentContext().updateBiomeId(id), name, path, type, this);
   }

   public interface Constants {
      String ZONE_PREFIX = "Zones.";
   }
}
