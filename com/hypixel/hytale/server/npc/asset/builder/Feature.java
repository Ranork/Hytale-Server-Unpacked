package com.hypixel.hytale.server.npc.asset.builder;

import java.util.EnumSet;
import java.util.function.Supplier;

public enum Feature implements Supplier<String> {
   Player("player target"),
   NPC("NPC target"),
   Drop("dropped item target"),
   Position("vector position"),
   Path("path");

   private final String description;
   public static final EnumSet<Feature> AnyPosition = EnumSet.of(Player, NPC, Drop, Position);
   public static final EnumSet<Feature> AnyEntity = EnumSet.of(Player, NPC, Drop);
   public static final EnumSet<Feature> LiveEntity = EnumSet.of(Player, NPC);

   private Feature(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
