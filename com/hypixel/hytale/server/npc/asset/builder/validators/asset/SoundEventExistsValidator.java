package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class SoundEventExistsValidator extends AssetValidator {
   private static final SoundEventExistsValidator DEFAULT_INSTANCE = new SoundEventExistsValidator();

   private SoundEventExistsValidator() {
   }

   private SoundEventExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "SoundEvent";
   }

   @Override
   public boolean test(String soundEvent) {
      return SoundEvent.getAssetMap().getAsset(soundEvent) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String soundEvent, String attributeName) {
      return "The sound event with the name \"" + soundEvent + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return SoundEvent.class.getSimpleName();
   }

   public static SoundEventExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static SoundEventExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new SoundEventExistsValidator(config);
   }
}
