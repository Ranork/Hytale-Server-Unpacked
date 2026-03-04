package com.hypixel.hytale.server.npc.asset.builder.providerevaluators;

import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class UnconditionalFeatureProviderEvaluator implements FeatureProviderEvaluator {
   @Nonnull
   private final Feature feature;
   private final String description;

   public UnconditionalFeatureProviderEvaluator(@Nonnull Feature feature) {
      this.feature = feature;
      this.description = feature.get();
   }

   @Override
   public boolean provides(@Nonnull EnumSet<Feature> feature) {
      return feature.contains(this.feature);
   }

   @Override
   public void resolveReferences(BuilderManager manager) {
   }
}
