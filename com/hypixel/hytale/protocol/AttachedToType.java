package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AttachedToType {
   LocalPlayer(0),
   EntityId(1),
   None(2);

   public static final AttachedToType[] VALUES = values();
   private final int value;

   private AttachedToType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AttachedToType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AttachedToType", value);
      }
   }
}
