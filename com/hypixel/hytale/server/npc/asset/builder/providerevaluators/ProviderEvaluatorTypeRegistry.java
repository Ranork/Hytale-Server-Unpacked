package com.hypixel.hytale.server.npc.asset.builder.providerevaluators;

import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.npc.asset.builder.validators.SubTypeTypeAdapterFactory;
import javax.annotation.Nonnull;

public class ProviderEvaluatorTypeRegistry {
   @Nonnull
   public static GsonBuilder registerTypes(@Nonnull GsonBuilder gsonBuilder) {
      SubTypeTypeAdapterFactory factory = SubTypeTypeAdapterFactory.of(ProviderEvaluator.class, "Type");
      factory.registerSubType(UnconditionalFeatureProviderEvaluator.class, "ProvidesFeatureUnconditionally");
      factory.registerSubType(UnconditionalParameterProviderEvaluator.class, "ProvidesParameterUnconditionally");
      gsonBuilder.registerTypeAdapterFactory(factory);
      return gsonBuilder;
   }
}
