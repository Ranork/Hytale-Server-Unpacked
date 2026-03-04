package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MovementForceRotationType {
   AttachedToHead(0),
   CameraRotation(1),
   Custom(2);

   public static final MovementForceRotationType[] VALUES = values();
   private final int value;

   private MovementForceRotationType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MovementForceRotationType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MovementForceRotationType", value);
      }
   }
}
