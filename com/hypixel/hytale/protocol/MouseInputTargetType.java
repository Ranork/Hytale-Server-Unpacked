package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MouseInputTargetType {
   Any(0),
   Block(1),
   Entity(2),
   None(3);

   public static final MouseInputTargetType[] VALUES = values();
   private final int value;

   private MouseInputTargetType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MouseInputTargetType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MouseInputTargetType", value);
      }
   }
}
