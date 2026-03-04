package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions.AlwaysTrueCondition;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class AlwaysTrueConditionAsset extends ConditionAsset {
   @Nonnull
   public static final BuilderCodec<AlwaysTrueConditionAsset> CODEC = BuilderCodec.builder(
         AlwaysTrueConditionAsset.class, AlwaysTrueConditionAsset::new, ConditionAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public SpaceAndDepthMaterialProvider.Condition build() {
      return AlwaysTrueCondition.INSTANCE;
   }
}
