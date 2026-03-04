package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ShaderType {
   None(0),
   Wind(1),
   WindAttached(2),
   WindRandom(3),
   WindFractal(4),
   Ice(5),
   Water(6),
   Lava(7),
   Slime(8),
   Ripple(9);

   public static final ShaderType[] VALUES = values();
   private final int value;

   private ShaderType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ShaderType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ShaderType", value);
      }
   }
}
