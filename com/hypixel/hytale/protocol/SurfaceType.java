package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum SurfaceType {
   Any(0),
   Reflective(1),
   Mixed(2),
   Absorbent(3);

   public static final SurfaceType[] VALUES = values();
   private final int value;

   private SurfaceType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static SurfaceType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("SurfaceType", value);
      }
   }
}
