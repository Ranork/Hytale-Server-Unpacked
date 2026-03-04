package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

public interface DoubleParameterProvider extends ParameterProvider {
   double NOT_PROVIDED = -Double.MAX_VALUE;

   double getDoubleParameter();
}
