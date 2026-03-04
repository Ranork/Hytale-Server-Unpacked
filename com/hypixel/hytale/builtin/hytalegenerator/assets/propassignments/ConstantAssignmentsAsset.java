package com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments;

import com.hypixel.hytale.builtin.hytalegenerator.assets.props.NoPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.Assignments;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.ConstantAssignments;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ConstantAssignmentsAsset extends AssignmentsAsset {
   @Nonnull
   public static final BuilderCodec<ConstantAssignmentsAsset> CODEC = BuilderCodec.builder(
         ConstantAssignmentsAsset.class, ConstantAssignmentsAsset::new, AssignmentsAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true), (asset, v) -> asset.propAsset = v, asset -> asset.propAsset)
      .add()
      .build();
   private PropAsset propAsset = new NoPropAsset();

   @Nonnull
   @Override
   public Assignments build(@Nonnull AssignmentsAsset.Argument argument) {
      if (super.skip()) {
         return Assignments.noPropDistribution(argument.runtime);
      } else {
         Prop prop = this.propAsset.build(new PropAsset.Argument(argument.parentSeed, argument.materialCache, argument.referenceBundle, argument.workerId));
         return new ConstantAssignments(prop, argument.runtime);
      }
   }

   @Override
   public void cleanUp() {
      this.propAsset.cleanUp();
   }
}
