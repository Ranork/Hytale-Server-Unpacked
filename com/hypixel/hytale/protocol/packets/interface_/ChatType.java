package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ChatType {
   Chat(0);

   public static final ChatType[] VALUES = values();
   private final int value;

   private ChatType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ChatType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ChatType", value);
      }
   }
}
