package com.hypixel.hytale.server.core.asset.type.weather.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import javax.annotation.Nonnull;

public class DayTexture {
   public static final BuilderCodec<DayTexture> CODEC = BuilderCodec.builder(DayTexture.class, DayTexture::new)
      .addField(new KeyedCodec<>("Day", Codec.INTEGER), (dayTexture, i) -> dayTexture.day = i, DayTexture::getDay)
      .<String>append(new KeyedCodec<>("Texture", Codec.STRING), (dayTexture, s) -> dayTexture.texture = s, DayTexture::getTexture)
      .addValidator(CommonAssetValidator.TEXTURE_SKY)
      .add()
      .build();
   protected int day;
   protected String texture;

   public DayTexture(int day, String texture) {
      this.day = day;
      this.texture = texture;
   }

   protected DayTexture() {
   }

   public int getDay() {
      return this.day;
   }

   public String getTexture() {
      return this.texture;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DayTexture{day=" + this.day + ", texture='" + this.texture + "'}";
   }
}
