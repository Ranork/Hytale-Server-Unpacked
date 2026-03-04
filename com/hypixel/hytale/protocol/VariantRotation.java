package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum VariantRotation {
   None(0),
   Wall(1),
   UpDown(2),
   Pipe(3),
   DoublePipe(4),
   NESW(5),
   UpDownNESW(6),
   All(7);

   public static final VariantRotation[] VALUES = values();
   private final int value;

   private VariantRotation(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static VariantRotation fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("VariantRotation", value);
      }
   }
}
