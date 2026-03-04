package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BlockSupportsRequiredForType {
   Any(0),
   All(1);

   public static final BlockSupportsRequiredForType[] VALUES = values();
   private final int value;

   private BlockSupportsRequiredForType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BlockSupportsRequiredForType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BlockSupportsRequiredForType", value);
      }
   }
}
