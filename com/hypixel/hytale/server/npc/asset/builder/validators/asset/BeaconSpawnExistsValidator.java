package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BeaconSpawnExistsValidator extends AssetValidator {
   private static final BeaconSpawnExistsValidator DEFAULT_INSTANCE = new BeaconSpawnExistsValidator();

   private BeaconSpawnExistsValidator() {
   }

   private BeaconSpawnExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "BeaconNPCSpawn";
   }

   @Override
   public boolean test(String beacon) {
      return BeaconNPCSpawn.getAssetMap().getAsset(beacon) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String beacon, String attributeName) {
      return "The beacon spawn with the name \"" + beacon + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return BeaconNPCSpawn.class.getSimpleName();
   }

   public static BeaconSpawnExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static BeaconSpawnExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new BeaconSpawnExistsValidator(config);
   }
}
