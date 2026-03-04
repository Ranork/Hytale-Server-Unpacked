package com.hypixel.hytale.server.npc.util;

public interface IComponentExecutionControl {
   boolean processDelay(float var1);

   void clearOnce();

   void setOnce();

   boolean isTriggered();
}
