package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AmbienceTransitionSpeed {
   Default(0),
   Fast(1),
   Instant(2);

   public static final AmbienceTransitionSpeed[] VALUES = values();
   private final int value;

   private AmbienceTransitionSpeed(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AmbienceTransitionSpeed fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AmbienceTransitionSpeed", value);
      }
   }
}
