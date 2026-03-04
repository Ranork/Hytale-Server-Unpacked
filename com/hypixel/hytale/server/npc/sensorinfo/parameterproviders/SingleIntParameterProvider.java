package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

public class SingleIntParameterProvider extends SingleParameterProvider implements IntParameterProvider {
   private int value;

   public SingleIntParameterProvider(int parameter) {
      super(parameter);
   }

   @Override
   public int getIntParameter() {
      return this.value;
   }

   @Override
   public void clear() {
      this.value = Integer.MIN_VALUE;
   }

   public void overrideInt(int value) {
      this.value = value;
   }
}
