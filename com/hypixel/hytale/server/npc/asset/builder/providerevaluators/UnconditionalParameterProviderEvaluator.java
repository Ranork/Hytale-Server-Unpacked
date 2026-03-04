package com.hypixel.hytale.server.npc.asset.builder.providerevaluators;

import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class UnconditionalParameterProviderEvaluator implements ParameterProviderEvaluator {
   private final Map<String, ParameterType> parameters = new HashMap<>();

   public UnconditionalParameterProviderEvaluator(@Nonnull String[] parameters, @Nonnull ParameterType[] types) {
      if (parameters.length != types.length) {
         throw new IllegalArgumentException("Different number of parameters to types");
      } else {
         for (int i = 0; i < parameters.length; i++) {
            this.parameters.put(parameters[i], types[i]);
         }
      }
   }

   @Override
   public boolean hasParameter(String parameter, ParameterType type) {
      return this.parameters.get(parameter) == type;
   }

   @Override
   public void resolveReferences(BuilderManager builderManager) {
   }
}
