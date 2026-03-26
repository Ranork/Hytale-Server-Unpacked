package com.hypixel.hytale.server.npc.asset.builder;

import java.util.function.Supplier;

public enum FeatureOverride implements Supplier<String> {
   On("Feature always enabled"),
   Off("Feature always disabled"),
   Default("Default behaviour");

   private final String description;

   private FeatureOverride(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }

   public boolean evaluate(boolean defaultValue) {
      return switch (this) {
         case On -> true;
         case Off -> false;
         case Default -> defaultValue;
      };
   }
}
