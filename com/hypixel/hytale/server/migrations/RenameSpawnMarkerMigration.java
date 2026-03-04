package com.hypixel.hytale.server.migrations;

import com.hypixel.hytale.assetstore.AssetValidationResults;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.migrations.EntityMigration;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class RenameSpawnMarkerMigration extends EntityMigration<SpawnMarkerEntity> {
   @Nonnull
   public static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private Map<String, SpawnMarker> idMigrations = new HashMap<>();

   public RenameSpawnMarkerMigration(@Nonnull Path filePath) {
      super(SpawnMarkerEntity.class, version -> {
         ExtraInfo extraInfo = new ExtraInfo(version, AssetValidationResults::new);
         ((AssetValidationResults)extraInfo.getValidationResults()).disableMissingAssetFor(SpawnMarker.class);
         return extraInfo;
      });

      List<String> lines;
      try {
         lines = Files.readAllLines(filePath);
      } catch (IOException var8) {
         throw SneakyThrow.sneakyThrow(var8);
      }

      for (String line : lines) {
         String[] split = line.split(":");
         if (split.length == 2) {
            String spawnMarkerMigrationId = split[1];
            SpawnMarker spawnMarker = SpawnMarker.getAssetMap().getAsset(spawnMarkerMigrationId);
            if (spawnMarker == null) {
               LOGGER.at(Level.WARNING).log("SpawnMarker '%s' does not exist!", spawnMarkerMigrationId);
            } else {
               this.idMigrations.put(split[0], spawnMarker);
            }
         }
      }
   }

   protected boolean migrate(@Nonnull SpawnMarkerEntity entity) {
      String spawnMarkerId = entity.getSpawnMarkerId();
      SpawnMarker spawnMarker = this.idMigrations.get(spawnMarkerId);
      if (spawnMarker == null) {
         return false;
      } else {
         entity.setSpawnMarker(spawnMarker);
         return true;
      }
   }
}
