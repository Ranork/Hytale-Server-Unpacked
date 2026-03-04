package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CustomPageLifetime {
   CantClose(0),
   CanDismiss(1),
   CanDismissOrCloseThroughInteraction(2);

   public static final CustomPageLifetime[] VALUES = values();
   private final int value;

   private CustomPageLifetime(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CustomPageLifetime fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CustomPageLifetime", value);
      }
   }
}
