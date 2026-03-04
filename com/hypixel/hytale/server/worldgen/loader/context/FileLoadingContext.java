package com.hypixel.hytale.server.worldgen.loader.context;

import com.hypixel.hytale.server.worldgen.prefab.PrefabCategory;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class FileLoadingContext extends FileContext<FileLoadingContext> {
   private final FileContext.Registry<ZoneFileContext> zones = new FileContext.Registry<>("Zone");
   private final FileContext.Registry<PrefabCategory> prefabCategories = new FileContext.Registry<>("Category");
   private int zoneIdCounter = -1;
   private int biomeIdCounter = -1;

   public FileLoadingContext(@Nonnull Path filepath) {
      super(-1, filepath.getFileName().toString(), filepath, null);
   }

   @Nonnull
   public FileLoadingContext getParentContext() {
      return this;
   }

   @Nonnull
   public FileContext.Registry<ZoneFileContext> getZones() {
      return this.zones;
   }

   @Nonnull
   public FileContext.Registry<PrefabCategory> getPrefabCategories() {
      return this.prefabCategories;
   }

   @Nonnull
   protected ZoneFileContext createZone(String name, Path path) {
      return this.createZone(this.nextZoneId(), name, path);
   }

   @Nonnull
   protected ZoneFileContext createZone(int id, String name, Path path) {
      return new ZoneFileContext(this.updateZoneId(id), name, path, this);
   }

   protected int nextZoneId() {
      return this.zoneIdCounter + 1;
   }

   protected int nextBiomeId() {
      return this.biomeIdCounter + 1;
   }

   protected int updateZoneId(int id) {
      validateId(id, this.zoneIdCounter, "Zone");
      this.zoneIdCounter = id;
      return id;
   }

   protected int updateBiomeId(int id) {
      validateId(id, this.biomeIdCounter, "Biome");
      this.biomeIdCounter = id;
      return id;
   }

   protected static void validateId(int id, int currentId, String type) {
      if (id < 0 || id <= currentId) {
         throw new Error(String.format("Invalid ID '%s' registered for type %s. Current ID counter: %s", id, type, currentId));
      }
   }

   public interface Constants {
      String ID_TYPE_ZONE = "Zone";
      String ID_TYPE_BIOME = "Biome";
      String ERROR_INVALID_ID = "Invalid ID '%s' registered for type %s. Current ID counter: %s";
   }
}
