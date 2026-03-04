package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.OffsetPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class OffsetPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<OffsetPositionProviderAsset> CODEC = BuilderCodec.builder(
         OffsetPositionProviderAsset.class, OffsetPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("OffsetX", Codec.INTEGER, true), (asset, v) -> asset.offsetX = v, asset -> asset.offsetX)
      .add()
      .append(new KeyedCodec<>("OffsetY", Codec.INTEGER, true), (asset, v) -> asset.offsetY = v, asset -> asset.offsetY)
      .add()
      .append(new KeyedCodec<>("OffsetZ", Codec.INTEGER, true), (asset, v) -> asset.offsetZ = v, asset -> asset.offsetZ)
      .add()
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset
      )
      .add()
      .build();
   private int offsetX;
   private int offsetY;
   private int offsetZ;
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return PositionProvider.noPositionProvider();
      } else {
         PositionProvider positionProvider = this.positionProviderAsset.build(argument);
         return new OffsetPositionProvider(new Vector3i(this.offsetX, this.offsetY, this.offsetZ), positionProvider);
      }
   }

   @Override
   public void cleanUp() {
      this.positionProviderAsset.cleanUp();
   }
}
