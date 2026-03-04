package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MovementDirection {
   None(0),
   Forward(1),
   Back(2),
   Left(3),
   Right(4),
   ForwardLeft(5),
   ForwardRight(6),
   BackLeft(7),
   BackRight(8);

   public static final MovementDirection[] VALUES = values();
   private final int value;

   private MovementDirection(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MovementDirection fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MovementDirection", value);
      }
   }
}
