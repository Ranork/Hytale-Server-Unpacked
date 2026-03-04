package com.hypixel.hytale.builtin.beds.sleep.resources;

public sealed interface WorldSleep permits WorldSleep.Awake, WorldSlumber {
   public static enum Awake implements WorldSleep {
      INSTANCE;
   }
}
