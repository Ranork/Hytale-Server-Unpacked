package com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.EmptyPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.ConstantPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.NoPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ConstantPropDistributionAsset extends PropDistributionAsset {
   @Nonnull
   public static final BuilderCodec<ConstantPropDistributionAsset> CODEC = BuilderCodec.builder(
         ConstantPropDistributionAsset.class, ConstantPropDistributionAsset::new, PropDistributionAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (t, k) -> t.positionProviderAsset = k, k -> k.positionProviderAsset)
      .add()
      .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (t, k) -> t.propAsset = k, k -> k.propAsset)
      .add()
      .build();
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
   private PropAsset propAsset = new EmptyPropAsset();

   @Nonnull
   @Override
   public PropDistribution build(@Nonnull PropDistributionAsset.Argument argument) {
      if (super.isSkipped()) {
         return NoPropDistribution.INSTANCE;
      } else {
         PositionProvider positionProvider = this.positionProviderAsset.build(new PositionProviderAsset.Argument(argument));
         Prop prop = this.propAsset.build(new PropAsset.Argument(argument));
         return new ConstantPropDistribution(positionProvider, prop);
      }
   }
}
