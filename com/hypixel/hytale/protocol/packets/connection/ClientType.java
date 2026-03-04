package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ClientType {
   Game(0),
   Editor(1);

   public static final ClientType[] VALUES = values();
   private final int value;

   private ClientType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ClientType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ClientType", value);
      }
   }
}
