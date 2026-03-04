package com.hypixel.hytale.server.npc.asset.builder.providerevaluators;

import java.util.function.Supplier;

public enum ParameterType implements Supplier<String> {
   DOUBLE("double"),
   STRING("string"),
   INTEGER("int");

   private final String description;

   private ParameterType(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
