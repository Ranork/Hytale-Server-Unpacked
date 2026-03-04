package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum FluidFog {
   Color(0),
   ColorLight(1),
   EnvironmentTint(2);

   public static final FluidFog[] VALUES = values();
   private final int value;

   private FluidFog(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static FluidFog fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("FluidFog", value);
      }
   }
}
