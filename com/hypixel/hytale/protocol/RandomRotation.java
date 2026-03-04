package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum RandomRotation {
   None(0),
   YawPitchRollStep1(1),
   YawStep1(2),
   YawStep1XZ(3),
   YawStep90(4);

   public static final RandomRotation[] VALUES = values();
   private final int value;

   private RandomRotation(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static RandomRotation fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("RandomRotation", value);
      }
   }
}
