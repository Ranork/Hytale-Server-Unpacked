package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum PhysicsType {
   Standard(0);

   public static final PhysicsType[] VALUES = values();
   private final int value;

   private PhysicsType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static PhysicsType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("PhysicsType", value);
      }
   }
}
