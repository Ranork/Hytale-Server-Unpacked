package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.config.ItemAttitudeGroup;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class ItemAttitudeGroupExistsValidator extends AssetValidator {
   private static final ItemAttitudeGroupExistsValidator DEFAULT_INSTANCE = new ItemAttitudeGroupExistsValidator();

   private ItemAttitudeGroupExistsValidator() {
   }

   private ItemAttitudeGroupExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "ItemAttitudeGroup";
   }

   @Override
   public boolean test(String attitudeGroup) {
      return ItemAttitudeGroup.getAssetMap().getAsset(attitudeGroup) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String attitudeGroup, String attributeName) {
      return "The item attitude group with the name \"" + attitudeGroup + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return ItemAttitudeGroup.class.getSimpleName();
   }

   public static ItemAttitudeGroupExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static ItemAttitudeGroupExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new ItemAttitudeGroupExistsValidator(config);
   }
}
