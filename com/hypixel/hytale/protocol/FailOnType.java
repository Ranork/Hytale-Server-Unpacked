package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum FailOnType {
   Neither(0),
   Entity(1),
   Block(2),
   Either(3);

   public static final FailOnType[] VALUES = values();
   private final int value;

   private FailOnType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static FailOnType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("FailOnType", value);
      }
   }
}
