package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class WeatherExistsValidator extends AssetValidator {
   private static final WeatherExistsValidator DEFAULT_INSTANCE = new WeatherExistsValidator();

   private WeatherExistsValidator() {
   }

   private WeatherExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "Weather";
   }

   @Override
   public boolean test(String value) {
      return Weather.getAssetMap().getAsset(value) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String value, String attribute) {
      return "The weather with the name \"" + value + "\" does not exist for attribute \"" + attribute + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return Weather.class.getSimpleName();
   }

   public static WeatherExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static WeatherExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new WeatherExistsValidator(config);
   }
}
