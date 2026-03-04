package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum PrioritySlot {
   Default(0),
   MainHand(1),
   OffHand(2);

   public static final PrioritySlot[] VALUES = values();
   private final int value;

   private PrioritySlot(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static PrioritySlot fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("PrioritySlot", value);
      }
   }
}
