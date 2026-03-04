package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

import javax.annotation.Nullable;

public interface StringParameterProvider extends ParameterProvider {
   @Nullable
   String getStringParameter();
}
