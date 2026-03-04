package com.hypixel.hytale.builtin.adventure.npcobjectives.npc.validators;

import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class ObjectiveExistsValidator extends AssetValidator {
   @Nonnull
   private static final ObjectiveExistsValidator DEFAULT_INSTANCE = new ObjectiveExistsValidator();

   private ObjectiveExistsValidator() {
   }

   private ObjectiveExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "Objective";
   }

   @Override
   public boolean test(String objective) {
      return ObjectiveAsset.getAssetMap().getAsset(objective) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String objective, String attributeName) {
      return "The objective with the name \"" + objective + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return ObjectiveAsset.class.getSimpleName();
   }

   @Nonnull
   public static ObjectiveExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static ObjectiveExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new ObjectiveExistsValidator(config);
   }
}
