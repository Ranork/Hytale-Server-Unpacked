package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BrushAxis {
   None(0),
   Auto(1),
   X(2),
   Y(3),
   Z(4);

   public static final BrushAxis[] VALUES = values();
   private final int value;

   private BrushAxis(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BrushAxis fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BrushAxis", value);
      }
   }
}
