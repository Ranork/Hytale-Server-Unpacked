package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ClickType {
   None(0),
   Left(1),
   Right(2),
   Middle(3);

   public static final ClickType[] VALUES = values();
   private final int value;

   private ClickType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ClickType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ClickType", value);
      }
   }
}
