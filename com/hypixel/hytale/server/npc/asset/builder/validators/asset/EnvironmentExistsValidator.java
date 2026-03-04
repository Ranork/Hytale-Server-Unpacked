package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class EnvironmentExistsValidator extends AssetValidator {
   public static final EnvironmentExistsValidator DEFAULT_INSTANCE = new EnvironmentExistsValidator();

   private EnvironmentExistsValidator() {
   }

   private EnvironmentExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "Environment";
   }

   @Override
   public boolean test(String envName) {
      return Environment.getAssetMap().getAsset(envName) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String envName, String attribute) {
      return "The environment with the file name \"" + envName + "\" does not exist in attribute \"" + attribute + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return Environment.class.getSimpleName();
   }

   public static EnvironmentExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static EnvironmentExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new EnvironmentExistsValidator(config);
   }
}
