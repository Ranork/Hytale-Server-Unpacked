package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum RotationDirection {
   Positive(0),
   Negative(1);

   public static final RotationDirection[] VALUES = values();
   private final int value;

   private RotationDirection(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static RotationDirection fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("RotationDirection", value);
      }
   }
}
