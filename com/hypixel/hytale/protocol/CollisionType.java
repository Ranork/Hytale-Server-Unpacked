package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CollisionType {
   Hard(0),
   Soft(1);

   public static final CollisionType[] VALUES = values();
   private final int value;

   private CollisionType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CollisionType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CollisionType", value);
      }
   }
}
