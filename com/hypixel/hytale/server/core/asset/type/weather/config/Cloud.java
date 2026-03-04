package com.hypixel.hytale.server.core.asset.type.weather.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class Cloud implements NetworkSerializable<com.hypixel.hytale.protocol.Cloud> {
   public static final BuilderCodec<Cloud> CODEC = BuilderCodec.builder(Cloud.class, Cloud::new)
      .append(new KeyedCodec<>("Texture", Codec.STRING), (cloud, s) -> cloud.texture = s, Cloud::getTexture)
      .addValidator(CommonAssetValidator.TEXTURE_SKY)
      .add()
      .<TimeColorAlpha[]>append(
         new KeyedCodec<>("Colors", new ArrayCodec<>(TimeColorAlpha.CODEC, TimeColorAlpha[]::new)), (cloud, s) -> cloud.colors = s, Cloud::getColors
      )
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .add()
      .<TimeFloat[]>append(new KeyedCodec<>("Speeds", new ArrayCodec<>(TimeFloat.CODEC, TimeFloat[]::new)), (cloud, s) -> cloud.speeds = s, Cloud::getSpeeds)
      .metadata(new UIEditor(UIEditor.TIMELINE))
      .add()
      .build();
   protected String texture;
   protected TimeColorAlpha[] colors;
   protected TimeFloat[] speeds;

   public Cloud(String texture, TimeColorAlpha[] colors, TimeFloat[] speeds) {
      this.texture = texture;
      this.colors = colors;
      this.speeds = speeds;
   }

   protected Cloud() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.Cloud toPacket() {
      com.hypixel.hytale.protocol.Cloud packet = new com.hypixel.hytale.protocol.Cloud();
      packet.texture = this.texture;
      packet.colors = Weather.toColorAlphaMap(this.colors);
      packet.speeds = Weather.toFloatMap(this.speeds);
      return packet;
   }

   public String getTexture() {
      return this.texture;
   }

   public TimeColorAlpha[] getColors() {
      return this.colors;
   }

   public TimeFloat[] getSpeeds() {
      return this.speeds;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Cloud{texture='"
         + this.texture
         + "', colors="
         + Arrays.toString((Object[])this.colors)
         + ", speeds="
         + Arrays.toString((Object[])this.speeds)
         + "}";
   }
}
