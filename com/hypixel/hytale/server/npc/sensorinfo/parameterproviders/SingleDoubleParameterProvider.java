package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

public class SingleDoubleParameterProvider extends SingleParameterProvider implements DoubleParameterProvider {
   private double value;

   public SingleDoubleParameterProvider(int parameter) {
      super(parameter);
   }

   @Override
   public double getDoubleParameter() {
      return this.value;
   }

   @Override
   public void clear() {
      this.value = -Double.MAX_VALUE;
   }

   public void overrideDouble(double value) {
      this.value = value;
   }
}
