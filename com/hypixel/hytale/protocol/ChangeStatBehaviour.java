package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ChangeStatBehaviour {
   Add(0),
   Set(1);

   public static final ChangeStatBehaviour[] VALUES = values();
   private final int value;

   private ChangeStatBehaviour(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ChangeStatBehaviour fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ChangeStatBehaviour", value);
      }
   }
}
