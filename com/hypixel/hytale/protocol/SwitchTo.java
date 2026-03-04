package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum SwitchTo {
   Disappear(0),
   PostColor(1),
   Distortion(2),
   Transparency(3);

   public static final SwitchTo[] VALUES = values();
   private final int value;

   private SwitchTo(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static SwitchTo fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("SwitchTo", value);
      }
   }
}
