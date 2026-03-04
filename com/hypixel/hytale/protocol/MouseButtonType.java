package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MouseButtonType {
   Left(0),
   Middle(1),
   Right(2),
   X1(3),
   X2(4);

   public static final MouseButtonType[] VALUES = values();
   private final int value;

   private MouseButtonType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MouseButtonType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MouseButtonType", value);
      }
   }
}
