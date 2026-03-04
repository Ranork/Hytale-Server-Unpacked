package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BrushOrigin {
   Center(0),
   Bottom(1),
   Top(2);

   public static final BrushOrigin[] VALUES = values();
   private final int value;

   private BrushOrigin(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BrushOrigin fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BrushOrigin", value);
      }
   }
}
