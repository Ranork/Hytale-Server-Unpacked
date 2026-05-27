package com.hypixel.hytale.server.npc.asset.builder.providerevaluators;

import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import javax.annotation.Nonnull;

public class PrecedingProviderEvaluator implements ProviderEvaluator {
   private final String className;

   public PrecedingProviderEvaluator(@Nonnull Class<?> clazz) {
      this.className = clazz.getSimpleName();
   }

   public String getClassName() {
      return this.className;
   }

   @Override
   public void resolveReferences(BuilderManager builderManager) {
   }
}
