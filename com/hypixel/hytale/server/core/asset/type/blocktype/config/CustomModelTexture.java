package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.ModelTexture;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import javax.annotation.Nonnull;

public class CustomModelTexture {
   @Nonnull
   public static BuilderCodec<CustomModelTexture> CODEC = BuilderCodec.builder(CustomModelTexture.class, CustomModelTexture::new)
      .append(
         new KeyedCodec<>("Texture", Codec.STRING), (customModelTexture, s) -> customModelTexture.texture = s, customModelTexture -> customModelTexture.texture
      )
      .addValidator(CommonAssetValidator.TEXTURE_ITEM)
      .add()
      .append(
         new KeyedCodec<>("Weight", Codec.INTEGER), (customModelTexture, i) -> customModelTexture.weight = i, customModelTexture -> customModelTexture.weight
      )
      .add()
      .build();
   private String texture;
   private int weight;

   public CustomModelTexture() {
   }

   public CustomModelTexture(String texture, int weight) {
      this.texture = texture;
      this.weight = weight;
   }

   public String getTexture() {
      return this.texture;
   }

   public int getWeight() {
      return this.weight;
   }

   @Nonnull
   public ModelTexture toPacket(float totalWight) {
      return new ModelTexture(this.texture, this.weight / totalWight);
   }

   @Nonnull
   @Override
   public String toString() {
      return "CustomModelTexture{texture='" + this.texture + "', weight=" + this.weight + "}";
   }
}
