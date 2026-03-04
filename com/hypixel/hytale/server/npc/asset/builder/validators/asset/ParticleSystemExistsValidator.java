package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class ParticleSystemExistsValidator extends AssetValidator {
   private static final ParticleSystemExistsValidator DEFAULT_INSTANCE = new ParticleSystemExistsValidator();

   private ParticleSystemExistsValidator() {
   }

   private ParticleSystemExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "ParticleSystem";
   }

   @Override
   public boolean test(String particleSystem) {
      return ParticleSystem.getAssetMap().getAsset(particleSystem) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String particleSystem, String attributeName) {
      return "The particle system with the name \"" + particleSystem + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return ParticleSystem.class.getSimpleName();
   }

   public static ParticleSystemExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static ParticleSystemExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new ParticleSystemExistsValidator(config);
   }
}
