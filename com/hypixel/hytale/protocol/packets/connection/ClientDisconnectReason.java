package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ClientDisconnectReason {
   PlayerLeave(0),
   PlayerAbort(1),
   UserLeave(2),
   Crash(3);

   public static final ClientDisconnectReason[] VALUES = values();
   private final int value;

   private ClientDisconnectReason(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ClientDisconnectReason fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ClientDisconnectReason", value);
      }
   }
}
