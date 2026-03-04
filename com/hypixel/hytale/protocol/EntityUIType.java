package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum EntityUIType {
   EntityStat(0),
   CombatText(1);

   public static final EntityUIType[] VALUES = values();
   private final int value;

   private EntityUIType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static EntityUIType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("EntityUIType", value);
      }
   }
}
