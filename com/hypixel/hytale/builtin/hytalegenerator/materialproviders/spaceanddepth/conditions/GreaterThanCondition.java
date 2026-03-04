package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import javax.annotation.Nonnull;

public class GreaterThanCondition implements SpaceAndDepthMaterialProvider.Condition {
   private final int threshold;
   @Nonnull
   private final ConditionParameter parameter;

   public GreaterThanCondition(int threshold, @Nonnull ConditionParameter parameter) {
      this.threshold = threshold;
      this.parameter = parameter;
   }

   @Override
   public boolean qualifies(int x, int y, int z, int depthIntoFloor, int depthIntoCeiling, int spaceAboveFloor, int spaceBelowCeiling) {
      int contextValue = switch (this.parameter) {
         case SPACE_ABOVE_FLOOR -> spaceAboveFloor;
         case SPACE_BELOW_CEILING -> spaceBelowCeiling;
      };
      return contextValue > this.threshold;
   }
}
