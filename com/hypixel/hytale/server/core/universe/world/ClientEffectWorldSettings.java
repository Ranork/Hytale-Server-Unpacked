package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.world.UpdatePostFxSettings;
import com.hypixel.hytale.protocol.packets.world.UpdateSunSettings;

public class ClientEffectWorldSettings {
   public static BuilderCodec<ClientEffectWorldSettings> CODEC = BuilderCodec.builder(ClientEffectWorldSettings.class, ClientEffectWorldSettings::new)
      .append(new KeyedCodec<>("SunHeightPercent", Codec.FLOAT), (settings, o) -> settings.sunHeightPercent = o, settings -> settings.sunHeightPercent)
      .add()
      .append(
         new KeyedCodec<>("SunAngleDegrees", Codec.FLOAT),
         (settings, o) -> settings.sunAngleRadians = (float)Math.toRadians(o.floatValue()),
         settings -> (float)Math.toDegrees(settings.sunAngleRadians)
      )
      .add()
      .append(new KeyedCodec<>("BloomIntensity", Codec.FLOAT), (settings, o) -> settings.bloomIntensity = o, settings -> settings.bloomIntensity)
      .add()
      .append(new KeyedCodec<>("BloomPower", Codec.FLOAT), (settings, o) -> settings.bloomPower = o, settings -> settings.bloomPower)
      .add()
      .append(new KeyedCodec<>("SunIntensity", Codec.FLOAT), (settings, o) -> settings.sunIntensity = o, settings -> settings.sunIntensity)
      .add()
      .append(new KeyedCodec<>("SunshaftIntensity", Codec.FLOAT), (settings, o) -> settings.sunshaftIntensity = o, settings -> settings.sunshaftIntensity)
      .add()
      .append(new KeyedCodec<>("SunshaftScaleFactor", Codec.FLOAT), (settings, o) -> settings.sunshaftScaleFactor = o, settings -> settings.sunshaftScaleFactor)
      .add()
      .build();
   private float sunHeightPercent = 100.0F;
   private float sunAngleRadians = 0.0F;
   private float bloomIntensity = 0.3F;
   private float bloomPower = 8.0F;
   private float sunIntensity = 0.25F;
   private float sunshaftIntensity = 0.3F;
   private float sunshaftScaleFactor = 4.0F;

   public float getSunHeightPercent() {
      return this.sunHeightPercent;
   }

   public void setSunHeightPercent(float sunHeightPercent) {
      this.sunHeightPercent = sunHeightPercent;
   }

   public float getSunAngleRadians() {
      return this.sunAngleRadians;
   }

   public void setSunAngleRadians(float sunAngleRadians) {
      this.sunAngleRadians = sunAngleRadians;
   }

   public float getBloomIntensity() {
      return this.bloomIntensity;
   }

   public void setBloomIntensity(float bloomIntensity) {
      this.bloomIntensity = bloomIntensity;
   }

   public float getBloomPower() {
      return this.bloomPower;
   }

   public void setBloomPower(float bloomPower) {
      this.bloomPower = bloomPower;
   }

   public float getSunIntensity() {
      return this.sunIntensity;
   }

   public void setSunIntensity(float sunIntensity) {
      this.sunIntensity = sunIntensity;
   }

   public float getSunshaftIntensity() {
      return this.sunshaftIntensity;
   }

   public void setSunshaftIntensity(float sunshaftIntensity) {
      this.sunshaftIntensity = sunshaftIntensity;
   }

   public float getSunshaftScaleFactor() {
      return this.sunshaftScaleFactor;
   }

   public void setSunshaftScaleFactor(float sunshaftScaleFactor) {
      this.sunshaftScaleFactor = sunshaftScaleFactor;
   }

   public UpdateSunSettings createSunSettingsPacket() {
      return new UpdateSunSettings(this.sunHeightPercent, this.sunAngleRadians);
   }

   public UpdatePostFxSettings createPostFxSettingsPacket() {
      return new UpdatePostFxSettings(this.bloomIntensity, this.bloomPower, this.sunshaftScaleFactor, this.sunIntensity, this.sunshaftIntensity);
   }
}
