package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AmbienceFXAltitude {
   Normal(0),
   Lowest(1),
   Highest(2),
   Random(3);

   public static final AmbienceFXAltitude[] VALUES = values();
   private final int value;

   private AmbienceFXAltitude(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AmbienceFXAltitude fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AmbienceFXAltitude", value);
      }
   }
}
