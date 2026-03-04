package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class ManualSpawnMarkerExistsValidator extends AssetValidator {
   private static final ManualSpawnMarkerExistsValidator DEFAULT_INSTANCE = new ManualSpawnMarkerExistsValidator();

   private ManualSpawnMarkerExistsValidator() {
   }

   private ManualSpawnMarkerExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "SpawnMarker";
   }

   @Override
   public boolean test(String marker) {
      SpawnMarker spawner = SpawnMarker.getAssetMap().getAsset(marker);
      return spawner != null && spawner.isManualTrigger();
   }

   @Nonnull
   @Override
   public String errorMessage(String marker, String attributeName) {
      return "The spawn marker with the name \"" + marker + "\" does not exist or is not a manual spawn marker for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return SpawnMarker.class.getSimpleName();
   }

   public static ManualSpawnMarkerExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static ManualSpawnMarkerExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new ManualSpawnMarkerExistsValidator(config);
   }
}
