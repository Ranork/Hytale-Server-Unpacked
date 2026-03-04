package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MovementType {
   None(0),
   Idle(1),
   Crouching(2),
   Walking(3),
   Running(4),
   Sprinting(5),
   Climbing(6),
   Swimming(7),
   Flying(8),
   Sliding(9),
   Rolling(10),
   Mounting(11),
   SprintMounting(12);

   public static final MovementType[] VALUES = values();
   private final int value;

   private MovementType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MovementType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MovementType", value);
      }
   }
}
