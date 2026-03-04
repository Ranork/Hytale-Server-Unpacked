package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ShadingMode {
   Standard(0),
   Flat(1),
   Fullbright(2),
   Reflective(3);

   public static final ShadingMode[] VALUES = values();
   private final int value;

   private ShadingMode(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ShadingMode fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ShadingMode", value);
      }
   }
}
