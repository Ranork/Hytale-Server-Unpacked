package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.config.AttitudeGroup;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class AttitudeGroupExistsValidator extends AssetValidator {
   private static final AttitudeGroupExistsValidator DEFAULT_INSTANCE = new AttitudeGroupExistsValidator();

   private AttitudeGroupExistsValidator() {
   }

   private AttitudeGroupExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "AttitudeGroup";
   }

   @Override
   public boolean test(String attitudeGroup) {
      return AttitudeGroup.getAssetMap().getAsset(attitudeGroup) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String attitudeGroup, String attributeName) {
      return "The attitude group with the name \"" + attitudeGroup + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return AttitudeGroup.class.getSimpleName();
   }

   public static AttitudeGroupExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static AttitudeGroupExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new AttitudeGroupExistsValidator(config);
   }
}
