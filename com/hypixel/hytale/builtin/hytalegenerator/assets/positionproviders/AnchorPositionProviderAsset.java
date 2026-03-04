package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.AnchorPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class AnchorPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<AnchorPositionProviderAsset> CODEC = BuilderCodec.builder(
         AnchorPositionProviderAsset.class, AnchorPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Reversed", Codec.BOOLEAN, false), (t, k) -> t.isReversed = k, k -> k.isReversed)
      .add()
      .append(new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (t, k) -> t.positionProviderAsset = k, k -> k.positionProviderAsset)
      .add()
      .build();
   private boolean isReversed = false;
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return PositionProvider.noPositionProvider();
      } else {
         PositionProvider positionProvider = this.positionProviderAsset == null
            ? PositionProvider.noPositionProvider()
            : this.positionProviderAsset.build(argument);
         return new AnchorPositionProvider(positionProvider, this.isReversed);
      }
   }

   @Override
   public void cleanUp() {
      this.positionProviderAsset.cleanUp();
   }
}
