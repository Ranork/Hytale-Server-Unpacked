package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum RotationType {
   AttachedToPlusOffset(0),
   Custom(1);

   public static final RotationType[] VALUES = values();
   private final int value;

   private RotationType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static RotationType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("RotationType", value);
      }
   }
}
