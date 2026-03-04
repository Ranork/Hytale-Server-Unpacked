package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ConnectedBlockRuleSetType {
   Stair(0),
   Roof(1);

   public static final ConnectedBlockRuleSetType[] VALUES = values();
   private final int value;

   private ConnectedBlockRuleSetType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ConnectedBlockRuleSetType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ConnectedBlockRuleSetType", value);
      }
   }
}
