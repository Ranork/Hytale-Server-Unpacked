package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ValueType {
   Percent(0),
   Absolute(1);

   public static final ValueType[] VALUES = values();
   private final int value;

   private ValueType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ValueType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ValueType", value);
      }
   }
}
