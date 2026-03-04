package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ApplyLookType {
   LocalPlayerLookOrientation(0),
   Rotation(1);

   public static final ApplyLookType[] VALUES = values();
   private final int value;

   private ApplyLookType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ApplyLookType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ApplyLookType", value);
      }
   }
}
