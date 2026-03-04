package com.hypixel.hytale.server.core.asset.type.environment.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import javax.annotation.Nonnull;

public class WeatherForecast implements IWeightedElement {
   public static final BuilderCodec<WeatherForecast> CODEC = BuilderCodec.builder(WeatherForecast.class, WeatherForecast::new)
      .append(new KeyedCodec<>("WeatherId", Codec.STRING), (weatherForecast, s) -> weatherForecast.weatherId = s, weatherForecast -> weatherForecast.weatherId)
      .addValidator(Validators.nonNull())
      .addValidator(Weather.VALIDATOR_CACHE.getValidator())
      .add()
      .addField(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (spawn, s) -> spawn.weight = s, spawn -> spawn.weight)
      .afterDecode(WeatherForecast::processConfig)
      .build();
   public static final WeatherForecast[] EMPTY_ARRAY = new WeatherForecast[0];
   protected String weatherId;
   protected transient int weatherIndex;
   protected double weight;

   public WeatherForecast(String weatherId, double weight) {
      this.weatherId = weatherId;
      this.weight = weight;
   }

   protected WeatherForecast() {
   }

   public String getWeatherId() {
      return this.weatherId;
   }

   public int getWeatherIndex() {
      return this.weatherIndex;
   }

   protected void processConfig() {
      this.weatherIndex = Weather.getAssetMap().getIndex(this.weatherId);
   }

   @Nonnull
   @Override
   public String toString() {
      return "WeatherForecast{weatherId='" + this.weatherId + "', weatherIndex=" + this.weatherIndex + ", weight=" + this.weight + "}";
   }

   @Override
   public double getWeight() {
      return this.weight;
   }
}
