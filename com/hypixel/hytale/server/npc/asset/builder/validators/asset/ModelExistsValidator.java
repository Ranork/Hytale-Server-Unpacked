package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class ModelExistsValidator extends AssetValidator {
   private static final ModelExistsValidator DEFAULT_INSTANCE = new ModelExistsValidator();

   private ModelExistsValidator() {
   }

   private ModelExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "Model";
   }

   @Override
   public boolean test(String model) {
      return ModelAsset.getAssetMap().getAsset(model) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String model, String attributeName) {
      return "The model with the name \"" + model + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return ModelAsset.class.getSimpleName();
   }

   public static ModelExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static ModelExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new ModelExistsValidator(config);
   }
}
