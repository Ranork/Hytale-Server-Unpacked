package com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.Assignments;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ImportedAssignmentsAsset extends AssignmentsAsset {
   @Nonnull
   public static final BuilderCodec<ImportedAssignmentsAsset> CODEC = BuilderCodec.builder(
         ImportedAssignmentsAsset.class, ImportedAssignmentsAsset::new, AssignmentsAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Name", Codec.STRING, true), (asset, v) -> asset.name = v, asset -> asset.name)
      .add()
      .build();
   private String name = "";

   @Override
   public Assignments build(@Nonnull AssignmentsAsset.Argument argument) {
      if (super.skip()) {
         return Assignments.noPropDistribution(argument.runtime);
      } else {
         AssignmentsAsset asset = getExportedAsset(this.name);
         if (asset == null) {
            LoggerUtil.getLogger().warning("Couldn't find Assignments asset exported with name: '" + this.name + "'.");
            return Assignments.noPropDistribution(argument.runtime);
         } else {
            return asset.build(argument);
         }
      }
   }
}
