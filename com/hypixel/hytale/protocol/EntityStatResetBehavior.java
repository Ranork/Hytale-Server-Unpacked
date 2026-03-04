package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum EntityStatResetBehavior {
   InitialValue(0),
   MaxValue(1);

   public static final EntityStatResetBehavior[] VALUES = values();
   private final int value;

   private EntityStatResetBehavior(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static EntityStatResetBehavior fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("EntityStatResetBehavior", value);
      }
   }
}
