package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum WindowType {
   Container(0),
   PocketCrafting(1),
   BasicCrafting(2),
   DiagramCrafting(3),
   StructuralCrafting(4),
   Processing(5),
   Memories(6);

   public static final WindowType[] VALUES = values();
   private final int value;

   private WindowType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static WindowType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("WindowType", value);
      }
   }
}
