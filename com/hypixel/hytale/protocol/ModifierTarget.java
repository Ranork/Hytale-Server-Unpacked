package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ModifierTarget {
   Min(0),
   Max(1);

   public static final ModifierTarget[] VALUES = values();
   private final int value;

   private ModifierTarget(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ModifierTarget fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ModifierTarget", value);
      }
   }
}
