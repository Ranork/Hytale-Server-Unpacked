package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class EntityEffectExistsValidator extends AssetValidator {
   private static final EntityEffectExistsValidator DEFAULT_INSTANCE = new EntityEffectExistsValidator();

   private EntityEffectExistsValidator() {
   }

   private EntityEffectExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "EntityEffect";
   }

   @Override
   public boolean test(String effect) {
      return EntityEffect.getAssetMap().getAsset(effect) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String effect, String attributeName) {
      return "The entity effect with the name \"" + effect + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return EntityEffect.class.getSimpleName();
   }

   public static EntityEffectExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static EntityEffectExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new EntityEffectExistsValidator(config);
   }
}
