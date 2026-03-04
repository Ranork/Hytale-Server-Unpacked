package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

import javax.annotation.Nonnull;

public abstract class SingleParameterProvider implements ParameterProvider {
   private final int parameter;

   public SingleParameterProvider(int parameter) {
      this.parameter = parameter;
   }

   @Nonnull
   @Override
   public ParameterProvider getParameterProvider(int parameter) {
      if (this.parameter != parameter) {
         throw new IllegalStateException("Parameter does not match!");
      } else {
         return this;
      }
   }
}
