package com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.NoPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ImportedPropDistributionAsset extends PropDistributionAsset {
   @Nonnull
   public static final BuilderCodec<ImportedPropDistributionAsset> CODEC = BuilderCodec.builder(
         ImportedPropDistributionAsset.class, ImportedPropDistributionAsset::new, PropDistributionAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (asset, v) -> asset.name = v, asset -> asset.name)
      .add()
      .build();
   private String name = "";

   @Nonnull
   @Override
   public PropDistribution build(@Nonnull PropDistributionAsset.Argument argument) {
      if (super.isSkipped()) {
         return NoPropDistribution.INSTANCE;
      } else {
         PropDistributionAsset asset = getExportedAsset(this.name);
         if (asset == null) {
            LoggerUtil.getLogger().warning("Couldn't find Positions asset exported with name: '" + this.name + "'.");
            return NoPropDistribution.INSTANCE;
         } else {
            return asset.build(argument);
         }
      }
   }
}
