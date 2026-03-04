package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class RootInteractionValidator extends AssetValidator {
   private RootInteractionValidator() {
   }

   private RootInteractionValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "Interaction";
   }

   @Override
   public boolean test(String value) {
      return RootInteraction.getAssetMap().getAsset(value) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String value, String attribute) {
      return "Interaction \"" + value + "\" does not exist for attribute \"" + attribute + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return RootInteraction.class.getSimpleName();
   }

   @Nonnull
   public static RootInteractionValidator required() {
      return new RootInteractionValidator();
   }

   @Nonnull
   public static RootInteractionValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new RootInteractionValidator(config);
   }
}
