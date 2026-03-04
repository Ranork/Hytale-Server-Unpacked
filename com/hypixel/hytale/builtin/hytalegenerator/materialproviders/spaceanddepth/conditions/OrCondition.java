package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import java.util.List;
import javax.annotation.Nonnull;

public class OrCondition implements SpaceAndDepthMaterialProvider.Condition {
   @Nonnull
   private final SpaceAndDepthMaterialProvider.Condition[] conditions;

   public OrCondition(@Nonnull List<SpaceAndDepthMaterialProvider.Condition> conditions) {
      this.conditions = new SpaceAndDepthMaterialProvider.Condition[conditions.size()];

      for (int i = 0; i < conditions.size(); i++) {
         this.conditions[i] = conditions.get(i);
         if (this.conditions[i] == null) {
            throw new IllegalArgumentException("conditions contains null element");
         }
      }
   }

   @Override
   public boolean qualifies(int x, int y, int z, int depthIntoFloor, int depthIntoCeiling, int spaceAboveFloor, int spaceBelowCeiling) {
      for (SpaceAndDepthMaterialProvider.Condition c : this.conditions) {
         if (c.qualifies(x, y, z, depthIntoFloor, depthIntoCeiling, spaceAboveFloor, spaceBelowCeiling)) {
            return true;
         }
      }

      return false;
   }
}
