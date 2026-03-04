package com.hypixel.hytale.server.npc.asset.builder.providerevaluators;

import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.FeatureEvaluatorHelper;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class ReferenceProviderEvaluator implements FeatureProviderEvaluator, ParameterProviderEvaluator {
   private final int referenceIndex;
   private final Class<?> classType;
   private FeatureEvaluatorHelper resolvedProviderSet;

   public ReferenceProviderEvaluator(int referenceIndex, Class<?> classType) {
      this.referenceIndex = referenceIndex;
      this.classType = classType;
   }

   @Override
   public boolean provides(EnumSet<Feature> feature) {
      if (this.resolvedProviderSet == null) {
         return false;
      } else {
         for (ProviderEvaluator evaluator : this.resolvedProviderSet.getProviders()) {
            if (evaluator instanceof FeatureProviderEvaluator && ((FeatureProviderEvaluator)evaluator).provides(feature)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean hasParameter(String parameter, ParameterType type) {
      if (this.resolvedProviderSet == null) {
         return false;
      } else {
         for (ProviderEvaluator evaluator : this.resolvedProviderSet.getProviders()) {
            if (evaluator instanceof ParameterProviderEvaluator && ((ParameterProviderEvaluator)evaluator).hasParameter(parameter, type)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void resolveReferences(@Nonnull BuilderManager manager) {
      Builder<Object> referencedBuilder = manager.getCachedBuilder(this.referenceIndex, this.classType);
      this.resolvedProviderSet = referencedBuilder.getEvaluatorHelper();
   }
}
