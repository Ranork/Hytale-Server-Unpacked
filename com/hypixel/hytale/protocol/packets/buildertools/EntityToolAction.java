package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum EntityToolAction {
   Remove(0),
   Clone(1),
   Freeze(2);

   public static final EntityToolAction[] VALUES = values();
   private final int value;

   private EntityToolAction(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static EntityToolAction fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("EntityToolAction", value);
      }
   }
}
