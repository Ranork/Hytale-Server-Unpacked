package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum GameMode {
   Adventure(0),
   Creative(1);

   public static final GameMode[] VALUES = values();
   private final int value;

   private GameMode(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static GameMode fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("GameMode", value);
      }
   }
}
