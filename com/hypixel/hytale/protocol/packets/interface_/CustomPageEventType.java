package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CustomPageEventType {
   Acknowledge(0),
   Data(1),
   Dismiss(2);

   public static final CustomPageEventType[] VALUES = values();
   private final int value;

   private CustomPageEventType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CustomPageEventType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CustomPageEventType", value);
      }
   }
}
