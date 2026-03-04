package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class MultipleParameterProvider implements ParameterProvider {
   private final Int2ObjectMap<ParameterProvider> providers = new Int2ObjectOpenHashMap();

   @Override
   public ParameterProvider getParameterProvider(int parameter) {
      return (ParameterProvider)this.providers.get(parameter);
   }

   @Override
   public void clear() {
      this.providers.values().forEach(ParameterProvider::clear);
   }

   public void addParameterProvider(int parameter, ParameterProvider provider) {
      this.providers.put(parameter, provider);
   }
}
