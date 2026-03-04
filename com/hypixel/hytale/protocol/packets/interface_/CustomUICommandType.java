package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CustomUICommandType {
   Append(0),
   AppendInline(1),
   InsertBefore(2),
   InsertBeforeInline(3),
   Remove(4),
   Set(5),
   Clear(6);

   public static final CustomUICommandType[] VALUES = values();
   private final int value;

   private CustomUICommandType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CustomUICommandType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CustomUICommandType", value);
      }
   }
}
