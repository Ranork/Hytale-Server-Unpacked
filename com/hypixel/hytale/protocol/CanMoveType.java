package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CanMoveType {
   AttachedToLocalPlayer(0),
   Always(1);

   public static final CanMoveType[] VALUES = values();
   private final int value;

   private CanMoveType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CanMoveType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CanMoveType", value);
      }
   }
}
