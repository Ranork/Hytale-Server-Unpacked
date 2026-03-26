package com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution;

import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.NoPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import javax.annotation.Nonnull;

public class NoPropDistributionAsset extends PropDistributionAsset {
   public static final NoPropDistributionAsset INSTANCE = new NoPropDistributionAsset();

   private NoPropDistributionAsset() {
   }

   @Nonnull
   @Override
   public PropDistribution build(@Nonnull PropDistributionAsset.Argument argument) {
      return NoPropDistribution.INSTANCE;
   }
}
