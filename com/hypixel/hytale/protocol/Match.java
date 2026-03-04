package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum Match {
   All(0),
   None(1);

   public static final Match[] VALUES = values();
   private final int value;

   private Match(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static Match fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("Match", value);
      }
   }
}
