package com.hypixel.hytale.server.npc.sensorinfo;

import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.ParameterProvider;
import javax.annotation.Nullable;

public interface InfoProvider {
   @Nullable
   IPositionProvider getPositionProvider();

   @Nullable
   ParameterProvider getParameterProvider(int var1);

   @Nullable
   <E extends ExtraInfoProvider> E getExtraInfo(Class<E> var1);

   <E extends ExtraInfoProvider> void passExtraInfo(E var1);

   @Nullable
   <E extends ExtraInfoProvider> E getPassedExtraInfo(Class<E> var1);

   boolean hasPosition();
}
