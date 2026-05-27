package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum RandomMode {
   Shuffle(0),
   Random(1);

   public static final RandomMode[] VALUES = values();
   private final int value;

   private RandomMode(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static RandomMode fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("RandomMode", value);
      }
   }
}
