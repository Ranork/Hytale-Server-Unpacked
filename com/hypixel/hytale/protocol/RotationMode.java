package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum RotationMode {
   None(0),
   Velocity(1),
   VelocityDamped(2),
   VelocityRoll(3);

   public static final RotationMode[] VALUES = values();
   private final int value;

   private RotationMode(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static RotationMode fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("RotationMode", value);
      }
   }
}
