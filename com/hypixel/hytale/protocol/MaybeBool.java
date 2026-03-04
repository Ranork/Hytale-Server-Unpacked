package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MaybeBool {
   Null(0),
   False(1),
   True(2);

   public static final MaybeBool[] VALUES = values();
   private final int value;

   private MaybeBool(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MaybeBool fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MaybeBool", value);
      }
   }
}
