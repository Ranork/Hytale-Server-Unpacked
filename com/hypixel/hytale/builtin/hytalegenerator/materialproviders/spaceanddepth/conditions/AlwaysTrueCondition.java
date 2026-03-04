package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import javax.annotation.Nonnull;

public class AlwaysTrueCondition implements SpaceAndDepthMaterialProvider.Condition {
   @Nonnull
   public static final AlwaysTrueCondition INSTANCE = new AlwaysTrueCondition();

   private AlwaysTrueCondition() {
   }

   @Override
   public boolean qualifies(int x, int y, int z, int depthIntoFloor, int depthIntoCeiling, int spaceAboveFloor, int spaceBelowCeiling) {
      return true;
   }
}
