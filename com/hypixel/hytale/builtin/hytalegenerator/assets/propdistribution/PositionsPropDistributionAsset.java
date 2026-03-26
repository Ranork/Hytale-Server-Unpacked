package com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.EmptyPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.NoPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PositionsPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class PositionsPropDistributionAsset extends PropDistributionAsset {
   @Nonnull
   public static final BuilderCodec<PositionsPropDistributionAsset> CODEC = BuilderCodec.builder(
         PositionsPropDistributionAsset.class, PositionsPropDistributionAsset::new, PropDistributionAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true),
         (asset, value) -> asset.positionProviderAsset = value,
         asset -> asset.positionProviderAsset
      )
      .add()
      .build();
   @Nonnull
   private PositionProviderAsset positionProviderAsset = EmptyPositionProviderAsset.INSTANCE;

   @Nonnull
   @Override
   public PropDistribution build(@Nonnull PropDistributionAsset.Argument argument) {
      if (super.isSkipped()) {
         return NoPropDistribution.INSTANCE;
      } else {
         PositionProvider positionProvider = this.positionProviderAsset.build(new PositionProviderAsset.Argument(argument));
         return new PositionsPropDistribution(positionProvider);
      }
   }

   @Override
   public void cleanUp() {
      this.positionProviderAsset.cleanUp();
   }
}
