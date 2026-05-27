package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum SpaceSize {
   Any(0),
   Tiny(1),
   Small(2),
   Medium(3),
   Large(4),
   Unbounded(5);

   public static final SpaceSize[] VALUES = values();
   private final int value;

   private SpaceSize(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static SpaceSize fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("SpaceSize", value);
      }
   }
}
