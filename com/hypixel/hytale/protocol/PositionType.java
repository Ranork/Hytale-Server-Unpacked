package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum PositionType {
   AttachedToPlusOffset(0),
   Custom(1);

   public static final PositionType[] VALUES = values();
   private final int value;

   private PositionType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static PositionType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("PositionType", value);
      }
   }
}
