package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.framework.DecimalConstantsFrameworkAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.BaseHeightPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class BaseHeightPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<BaseHeightPositionProviderAsset> CODEC = BuilderCodec.builder(
         BaseHeightPositionProviderAsset.class, BaseHeightPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("MinYRead", Codec.DOUBLE, false), (asset, v) -> asset.minYRead = v, asset -> asset.minYRead)
      .add()
      .append(new KeyedCodec<>("MaxYRead", Codec.DOUBLE, false), (asset, v) -> asset.maxYRead = v, asset -> asset.maxYRead)
      .add()
      .append(new KeyedCodec<>("BedName", Codec.STRING, false), (asset, v) -> asset.baseHeightName = v, asset -> asset.baseHeightName)
      .add()
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset
      )
      .add()
      .build();
   private double minYRead = -1.0;
   private double maxYRead = 1.0;
   private String baseHeightName = "";
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return PositionProvider.noPositionProvider();
      } else {
         PositionProvider positionProvider = this.positionProviderAsset.build(argument);
         Double baseHeight = DecimalConstantsFrameworkAsset.Entries.get(this.baseHeightName, argument.referenceBundle);
         if (baseHeight == null) {
            baseHeight = 0.0;
         }

         return new BaseHeightPositionProvider(baseHeight, positionProvider, this.minYRead, this.maxYRead);
      }
   }

   @Override
   public void cleanUp() {
      this.positionProviderAsset.cleanUp();
   }
}
