package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import javax.annotation.Nonnull;

public class NotCondition implements SpaceAndDepthMaterialProvider.Condition {
   @Nonnull
   private final SpaceAndDepthMaterialProvider.Condition condition;

   public NotCondition(@Nonnull SpaceAndDepthMaterialProvider.Condition condition) {
      this.condition = condition;
   }

   @Override
   public boolean qualifies(int x, int y, int z, int depthIntoFloor, int depthIntoCeiling, int spaceAboveFloor, int spaceBelowCeiling) {
      return !this.condition.qualifies(x, y, z, depthIntoFloor, depthIntoCeiling, spaceAboveFloor, spaceBelowCeiling);
   }
}
