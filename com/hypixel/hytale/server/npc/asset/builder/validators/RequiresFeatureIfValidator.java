package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.FeatureEvaluatorHelper;
import com.hypixel.hytale.server.npc.asset.builder.providerevaluators.FeatureProviderEvaluator;
import com.hypixel.hytale.server.npc.asset.builder.providerevaluators.ProviderEvaluator;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RequiresFeatureIfValidator extends RequiredFeatureValidator {
   @Nonnull
   private final String[] description;
   private final String attribute;
   private final boolean value;

   private RequiresFeatureIfValidator(String attribute, boolean value, @Nonnull EnumSet<Feature> feature) {
      this.attribute = attribute;
      this.description = BuilderBase.getDescriptionArray(feature);
      this.value = value;
   }

   @Override
   public boolean validate(FeatureEvaluatorHelper evaluatorHelper) {
      return false;
   }

   @Nullable
   @Override
   public String getErrorMessage(String context) {
      return null;
   }

   public static boolean staticValidate(@Nonnull FeatureEvaluatorHelper evaluatorHelper, EnumSet<Feature> requiredFeature, boolean requiredValue, boolean value) {
      if (requiredValue != value) {
         return true;
      } else {
         for (ProviderEvaluator providedFeature : evaluatorHelper.getProviders()) {
            if (providedFeature instanceof FeatureProviderEvaluator && ((FeatureProviderEvaluator)providedFeature).provides(requiredFeature)) {
               return true;
            }
         }

         return false;
      }
   }

   @Nonnull
   public static RequiresFeatureIfValidator withAttributes(String attribute, boolean value, @Nonnull EnumSet<Feature> feature) {
      return new RequiresFeatureIfValidator(attribute, value, feature);
   }
}
