package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum FluidDrawType {
   None(0),
   Liquid(1);

   public static final FluidDrawType[] VALUES = values();
   private final int value;

   private FluidDrawType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static FluidDrawType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("FluidDrawType", value);
      }
   }
}
