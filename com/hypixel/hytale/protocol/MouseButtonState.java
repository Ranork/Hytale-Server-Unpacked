package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MouseButtonState {
   Pressed(0),
   Released(1);

   public static final MouseButtonState[] VALUES = values();
   private final int value;

   private MouseButtonState(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MouseButtonState fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MouseButtonState", value);
      }
   }
}
