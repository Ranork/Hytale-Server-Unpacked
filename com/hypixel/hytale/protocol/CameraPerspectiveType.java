package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CameraPerspectiveType {
   First(0),
   Third(1);

   public static final CameraPerspectiveType[] VALUES = values();
   private final int value;

   private CameraPerspectiveType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CameraPerspectiveType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CameraPerspectiveType", value);
      }
   }
}
