package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

public interface IntParameterProvider extends ParameterProvider {
   int NOT_PROVIDED = Integer.MIN_VALUE;

   int getIntParameter();
}
