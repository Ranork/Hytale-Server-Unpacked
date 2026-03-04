package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ChangeVelocityType {
   Add(0),
   Set(1);

   public static final ChangeVelocityType[] VALUES = values();
   private final int value;

   private ChangeVelocityType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ChangeVelocityType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ChangeVelocityType", value);
      }
   }
}
