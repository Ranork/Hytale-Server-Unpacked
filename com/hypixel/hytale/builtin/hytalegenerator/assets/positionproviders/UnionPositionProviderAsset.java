package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.UnionPositionProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class UnionPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<UnionPositionProviderAsset> CODEC = BuilderCodec.builder(
         UnionPositionProviderAsset.class, UnionPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Positions", new ArrayCodec<>(PositionProviderAsset.CODEC, PositionProviderAsset[]::new), true),
         (asset, v) -> asset.positionProviderAssets = v,
         asset -> asset.positionProviderAssets
      )
      .add()
      .build();
   private PositionProviderAsset[] positionProviderAssets = new PositionProviderAsset[0];

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return PositionProvider.noPositionProvider();
      } else {
         ArrayList<PositionProvider> list = new ArrayList<>();

         for (PositionProviderAsset asset : this.positionProviderAssets) {
            PositionProvider positionProvider = asset.build(argument);
            list.add(positionProvider);
         }

         return new UnionPositionProvider(list);
      }
   }

   @Override
   public void cleanUp() {
      for (PositionProviderAsset positionProviderAsset : this.positionProviderAssets) {
         positionProviderAsset.cleanUp();
      }
   }
}
