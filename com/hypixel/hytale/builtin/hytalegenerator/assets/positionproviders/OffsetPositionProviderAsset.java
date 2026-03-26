package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.OffsetPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class OffsetPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<OffsetPositionProviderAsset> CODEC = BuilderCodec.builder(
         OffsetPositionProviderAsset.class, OffsetPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true),
         (asset, value) -> asset.positionProviderAsset = value,
         asset -> asset.positionProviderAsset
      )
      .add()
      .append(new KeyedCodec<>("Offset", Vector3d.CODEC, true), (asset, value) -> asset.offset = value, asset -> asset.offset)
      .add()
      .append(new KeyedCodec<>("OffsetX", Codec.INTEGER, true), (asset, value) -> asset.offsetX = value, asset -> asset.offsetX)
      .add()
      .append(new KeyedCodec<>("OffsetY", Codec.INTEGER, true), (asset, value) -> asset.offsetY = value, asset -> asset.offsetY)
      .add()
      .append(new KeyedCodec<>("OffsetZ", Codec.INTEGER, true), (asset, value) -> asset.offsetZ = value, asset -> asset.offsetZ)
      .add()
      .build();
   private static final Vector3d DEFAULT_OFFSET = new Vector3d();
   @Nonnull
   private Vector3d offset = DEFAULT_OFFSET;
   @Nonnull
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
   private int offsetX = 0;
   private int offsetY = 0;
   private int offsetZ = 0;

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return EmptyPositionProvider.INSTANCE;
      } else if (DEFAULT_OFFSET == this.offset) {
         PositionProvider positionProvider = this.positionProviderAsset.build(argument);
         return new OffsetPositionProvider(new Vector3d(this.offsetX, this.offsetY, this.offsetZ), positionProvider);
      } else {
         PositionProvider positionProvider = this.positionProviderAsset.build(argument);
         return new OffsetPositionProvider(this.offset, positionProvider);
      }
   }

   @Override
   public void cleanUp() {
      this.positionProviderAsset.cleanUp();
   }
}
