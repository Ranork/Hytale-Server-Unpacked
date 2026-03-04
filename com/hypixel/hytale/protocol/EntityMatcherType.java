package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum EntityMatcherType {
   Server(0),
   VulnerableMatcher(1),
   Player(2);

   public static final EntityMatcherType[] VALUES = values();
   private final int value;

   private EntityMatcherType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static EntityMatcherType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("EntityMatcherType", value);
      }
   }
}
