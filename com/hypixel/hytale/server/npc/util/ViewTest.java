package com.hypixel.hytale.server.npc.util;

import java.util.function.Supplier;

public enum ViewTest implements Supplier<String> {
   NONE("None"),
   VIEW_SECTOR("View_Sector"),
   VIEW_CONE("View_Cone");

   private final String name;

   private ViewTest(String name) {
      this.name = name;
   }

   public String get() {
      return this.name;
   }
}
