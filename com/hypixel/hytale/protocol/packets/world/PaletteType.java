package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum PaletteType {
   Empty(0),
   HalfByte(1),
   Byte(2),
   Short(3);

   public static final PaletteType[] VALUES = values();
   private final int value;

   private PaletteType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static PaletteType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("PaletteType", value);
      }
   }
}
