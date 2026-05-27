package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum DurabilityOperator {
   LessThan(0),
   LessOrEqual(1),
   GreaterThan(2),
   GreaterOrEqual(3),
   Equal(4),
   NotEqual(5);

   public static final DurabilityOperator[] VALUES = values();
   private final int value;

   private DurabilityOperator(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static DurabilityOperator fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("DurabilityOperator", value);
      }
   }
}
