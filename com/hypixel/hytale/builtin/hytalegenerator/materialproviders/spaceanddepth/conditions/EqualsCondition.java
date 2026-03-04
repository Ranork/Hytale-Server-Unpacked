package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import javax.annotation.Nonnull;

public class EqualsCondition implements SpaceAndDepthMaterialProvider.Condition {
   private final int value;
   @Nonnull
   private final ConditionParameter parameter;

   public EqualsCondition(int value, @Nonnull ConditionParameter parameter) {
      this.value = value;
      this.parameter = parameter;
   }

   @Override
   public boolean qualifies(int x, int y, int z, int depthIntoFloor, int depthIntoCeiling, int spaceAboveFloor, int spaceBelowCeiling) {
      int contextValue = switch (this.parameter) {
         case SPACE_ABOVE_FLOOR -> spaceAboveFloor;
         case SPACE_BELOW_CEILING -> spaceBelowCeiling;
      };
      return contextValue == this.value;
   }
}
