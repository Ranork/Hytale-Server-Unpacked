package com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution;

import com.hypixel.hytale.builtin.hytalegenerator.assets.assignments.AssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.assignments.ConstantAssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assignments.Assignments;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.AssignedPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.NoPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class AssignedPropDistributionAsset extends PropDistributionAsset {
   @Nonnull
   public static final BuilderCodec<AssignedPropDistributionAsset> CODEC = BuilderCodec.builder(
         AssignedPropDistributionAsset.class, AssignedPropDistributionAsset::new, PropDistributionAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("PropDistribution", PropDistributionAsset.CODEC, true),
         (asset, value) -> asset.propDistributionAsset = value,
         asset -> asset.propDistributionAsset
      )
      .add()
      .append(new KeyedCodec<>("Assignments", AssignmentsAsset.CODEC, true), (asset, value) -> asset.assignmentsAsset = value, asset -> asset.assignmentsAsset)
      .add()
      .append(new KeyedCodec<>("OverrideAllProps", Codec.BOOLEAN, true), (asset, value) -> asset.isOverrideAllProps = value, asset -> asset.isOverrideAllProps)
      .add()
      .build();
   @Nonnull
   private PropDistributionAsset propDistributionAsset = NoPropDistributionAsset.INSTANCE;
   @Nonnull
   private AssignmentsAsset assignmentsAsset = new ConstantAssignmentsAsset();
   private boolean isOverrideAllProps = false;

   @Nonnull
   @Override
   public PropDistribution build(@Nonnull PropDistributionAsset.Argument argument) {
      if (super.isSkipped()) {
         return NoPropDistribution.INSTANCE;
      } else {
         PropDistribution propDistribution = this.propDistributionAsset.build(argument);
         Assignments assignments = this.assignmentsAsset.build(new AssignmentsAsset.Argument(argument));
         return new AssignedPropDistribution(propDistribution, assignments, this.isOverrideAllProps);
      }
   }
}
